/**
 * Created by dmadden on 2/20/18.
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class PA2 {

  private static class PartitionerInitial extends Partitioner<DocIdUniComKey, IntWritable> {
    public int getPartition(DocIdUniComKey key, IntWritable value, int numReduceTasks) {
      return Math.abs(key.getDocID().hashCode() % numReduceTasks);
    }
  }

  private static class DocIdUniComKey implements Writable, WritableComparable<DocIdUniComKey> {
    private IntWritable docID = new IntWritable();
    private Text unigram = new Text();

    DocIdUniComKey() {
      this.docID = new IntWritable();
      this.unigram = new Text();
    }

    DocIdUniComKey(IntWritable id, Text uni) {
      this.docID.set(Integer.parseInt(id.toString()));
      this.unigram.set(uni);
    }

    public void write(DataOutput out) throws IOException {
      this.docID.write(out);
      this.unigram.write(out);
    }

    public void readFields(DataInput in) throws IOException {
      this.docID = new IntWritable();
      this.unigram = new Text();
      this.docID.readFields(in);
      this.unigram.readFields(in);
    }

    IntWritable getDocID() {
      return this.docID;
    }

    Text getUnigram() {
      return this.unigram;
    }

    public int compareTo(DocIdUniComKey pair) {
      int compareValue = this.docID.compareTo(pair.getDocID());
      if (compareValue == 0) {
        compareValue = unigram.compareTo(pair.getUnigram());
      }
      return -1 * compareValue; //descending order
    }

    @Override
    public String toString() {
      return docID.toString() + "\t" + unigram.toString();
    }
  }

  static class Job1Mapper extends Mapper<Object, Text, DocIdUniComKey, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private final Text word = new Text();
    private final IntWritable docID = new IntWritable();
    private DocIdUniComKey comKey = new DocIdUniComKey();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String values[] = value.toString().split("<====>");
      if (values.length >= 3) {
        String id = values[1];
        String article = values[2];
        String unigram = article.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        StringTokenizer itr = new StringTokenizer(unigram);
        while (itr.hasMoreTokens()) {
          unigram = itr.nextToken();
          if (!unigram.equals("")) {
            word.set(unigram);
            docID.set(Integer.parseInt(id));
            comKey = new DocIdUniComKey(docID, word);
            context.write(comKey, one);
          }
        }
      }
    }
  }

  static class Job1Reducer extends Reducer<DocIdUniComKey, IntWritable, DocIdUniComKey, IntWritable> {
    private final IntWritable result = new IntWritable();

    public void reduce(DocIdUniComKey key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      System.out.println(key.toString());
      context.write(key, result);
    }
  }

  static class Job2Mapper extends Mapper<LongWritable, Text, IntWritable, Text> {
    private final IntWritable docId = new IntWritable();
    private final Text compValue = new Text();

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      String[] inputArray = value.toString().split("\t");
      String id = inputArray[0];
      String uni = inputArray[1];
      String freq = inputArray[2];
      docId.set(Integer.parseInt(id));
      compValue.set(uni + "\t" + freq);
      context.write(docId, compValue);
    }
  }

  static class Job2Reducer extends Reducer<IntWritable, Text, IntWritable, Text> {
    private final IntWritable docId = new IntWritable();
    private final Text compValue = new Text();

    public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      ArrayList<String> valuesCopy = new ArrayList<String>();
      double maxFreq = 0;
      double tf;
      String tempValue;

      for (Text val : values) {
        valuesCopy.add(val.toString());
      }

      for (String val : valuesCopy) {
        String[] valuesSplit = val.toString().split("\t");
        int frequency = Integer.parseInt(valuesSplit[1]);
        if (frequency > maxFreq) {
          maxFreq = frequency;
        }
      }

      for (String val : valuesCopy) {
        String[] valuesSplit = val.toString().split("\t");
        String unigram = valuesSplit[0];
        int frequency = Integer.parseInt(valuesSplit[1]);
        tf = 0.5 + 0.5 * (frequency / maxFreq);
        tempValue = unigram + "\t" + frequency + "\t" + tf;
        compValue.set(tempValue);
        docId.set(Integer.parseInt(key.toString()));
        context.write(docId, compValue);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    conf.set("mapred.textoutputformat.separator", "\t");
    int numReduceTask = 28;

    Path inputPath = new Path(args[0]);
    Path outputPathTemp1 = new Path(args[1] + "Temp1");
    Path outputPathTemp2 = new Path(args[1] + "Temp2");
    Path outputPath = new Path(args[1]);

    Job job1 = Job.getInstance(conf, "pa2_job1");
    Job job2 = Job.getInstance(conf, "pa2_job2");

    job1.setJarByClass(PA2.class);
    job1.setNumReduceTasks(numReduceTask);
    job1.setPartitionerClass(PartitionerInitial.class);

    job1.setMapperClass(PA2.Job1Mapper.class);
    job1.setReducerClass(PA2.Job1Reducer.class);
    job1.setOutputKeyClass(DocIdUniComKey.class);
    job1.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job1, inputPath);
    FileOutputFormat.setOutputPath(job1, outputPathTemp1);
    if (job1.waitForCompletion(true)) {
      job2.setJarByClass(PA2.class);
      job2.setNumReduceTasks(numReduceTask);
//      job2.setPartitionerClass(PartitionerInitial.class);

      job2.setMapperClass(PA2.Job2Mapper.class);
      job2.setReducerClass(PA2.Job2Reducer.class);

      job2.setMapOutputKeyClass(IntWritable.class);
      job2.setMapOutputValueClass(Text.class);
      job2.setOutputKeyClass(IntWritable.class);
      job2.setOutputValueClass(Text.class);

      FileInputFormat.addInputPath(job2, outputPathTemp1);
      FileOutputFormat.setOutputPath(job2, outputPathTemp2);

      System.exit(job2.waitForCompletion(true) ? 0 : 1);
    }
  }
}
