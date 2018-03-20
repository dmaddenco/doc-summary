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
import java.util.StringTokenizer;

public class Profile2 {

  private static class profile2PartitionerInitial extends Partitioner<CompositeGroupKey, IntWritable> {
    public int getPartition(CompositeGroupKey key, IntWritable value, int numReduceTasks) {
      return Math.abs(key.getDocID().hashCode() % numReduceTasks);
    }
  }

  private static class profile2PartitionerFinal extends Partitioner<CompositeGroupKeyFreq, Text> {
    public int getPartition(CompositeGroupKeyFreq key, Text value, int numReduceTasks) {
      return Math.abs(key.getDocID().hashCode() % numReduceTasks);
    }
  }

  private static class CompositeGroupKey implements Writable, WritableComparable<CompositeGroupKey> {
    private IntWritable docID = new IntWritable();
    private Text unigram = new Text();

    CompositeGroupKey() {
      this.docID = new IntWritable();
      this.unigram = new Text();
    }

    CompositeGroupKey(IntWritable id, Text uni) {
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

    public int compareTo(CompositeGroupKey pair) {
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

  private static class CompositeGroupKeyFreq implements Writable, WritableComparable<CompositeGroupKeyFreq> {
    private IntWritable docID = new IntWritable();
    private IntWritable frequency = new IntWritable();

    CompositeGroupKeyFreq() {
      this.docID = new IntWritable();
      this.frequency = new IntWritable();
    }

    CompositeGroupKeyFreq(IntWritable id, IntWritable freq) {
      this.docID.set(Integer.parseInt(id.toString()));
      this.frequency.set(Integer.parseInt(freq.toString()));
    }

    public void write(DataOutput out) throws IOException {
      this.docID.write(out);
      this.frequency.write(out);
    }

    public void readFields(DataInput in) throws IOException {
      this.docID = new IntWritable();
      this.frequency = new IntWritable();
      this.docID.readFields(in);
      this.frequency.readFields(in);
    }

    IntWritable getDocID() {
      return this.docID;
    }

    IntWritable getFrequency() {
      return this.frequency;
    }

    public int compareTo(CompositeGroupKeyFreq pair) {
      int compareValue = this.docID.compareTo(pair.getDocID());
      if (compareValue == 0) {
        compareValue = frequency.compareTo(pair.getFrequency());
      }
      return -1 * compareValue; //descending order
    }

    @Override
    public String toString() {
      return docID.toString() + "\t" + frequency.toString();
    }
  }

  static class TokenizerMapper extends Mapper<Object, Text, CompositeGroupKey, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private final Text word = new Text();
    private final IntWritable docID = new IntWritable();
    private CompositeGroupKey cntry = new CompositeGroupKey();

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
            cntry = new CompositeGroupKey(docID, word);
            context.write(cntry, one);
          }
        }
      }
    }
  }

  static class IntSumReducer extends Reducer<CompositeGroupKey, IntWritable, CompositeGroupKey, IntWritable> {
    private final IntWritable result = new IntWritable();

    public void reduce(CompositeGroupKey key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      System.out.println(key.toString());
      context.write(key, result);
    }
  }

  static class FrequencyMapper extends Mapper<LongWritable, Text, CompositeGroupKeyFreq, Text> {
    private final IntWritable docID = new IntWritable();
    private final IntWritable sum = new IntWritable();
    private final Text word = new Text();
    private CompositeGroupKeyFreq cntry = new CompositeGroupKeyFreq();

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      String[] valueSplit = value.toString().split("\\t");
      docID.set(Integer.parseInt(valueSplit[0]));
      word.set(valueSplit[1]);
      sum.set(Integer.parseInt(valueSplit[2]));
      cntry = new CompositeGroupKeyFreq(docID, sum);
      context.write(cntry, word);
    }
  }

  static class FrequencyReducer extends Reducer<CompositeGroupKeyFreq, Text, CompositeGroupKey, IntWritable> {
    private final IntWritable docID = new IntWritable();
    private final Text word = new Text();
    private CompositeGroupKey cntry = new CompositeGroupKey();
    private final IntWritable freq = new IntWritable();

    public void reduce(CompositeGroupKeyFreq key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      for (Text value : values) {
        word.set(value.toString());
        docID.set(Integer.parseInt(key.getDocID().toString()));
        freq.set(Integer.parseInt(key.getFrequency().toString()));
        cntry = new CompositeGroupKey(docID, word);
        context.write(cntry, freq);
      }
    }
  }

  static class DocIDComparable extends WritableComparator {
    DocIDComparable() {
      super(CompositeGroupKey.class, true);
    }

    @Override
    public int compare(WritableComparable w1, WritableComparable w2) {
      CompositeGroupKey key1 = (CompositeGroupKey) w1;
      CompositeGroupKey key2 = (CompositeGroupKey) w2;
      return key1.docID.compareTo(key2.docID);
    }
  }

  static class DescendingIntComparable extends WritableComparator {
    DescendingIntComparable() {
      super(CompositeGroupKeyFreq.class, true);
    }

    @Override
    public int compare(WritableComparable w1, WritableComparable w2) {
      CompositeGroupKeyFreq key1 = (CompositeGroupKeyFreq) w1;
      CompositeGroupKeyFreq key2 = (CompositeGroupKeyFreq) w2;
      return key1.compareTo(key2);
    }
  }

  public static void main(String[] args) throws Exception {
    /*
    Configuration conf = new Configuration();
    conf.set("mapred.textoutputformat.separator", "\t");
    int numReduceTask = 320;

    Path inputPath = new Path(args[0]);
    Path outputPathTemp = new Path(args[1] + "Temp");
    Path outputPath = new Path(args[1]);

    Job job1 = Job.getInstance(conf, "profile2_job1");
    Job job2 = Job.getInstance(conf, "profile2_job2");

    job1.setJarByClass(Profile2.class);
    job1.setNumReduceTasks(numReduceTask);
    job1.setPartitionerClass(profile2PartitionerInitial.class);

    job1.setMapperClass(Profile2.TokenizerMapper.class);
    job1.setReducerClass(Profile2.IntSumReducer.class);
    job1.setOutputKeyClass(CompositeGroupKey.class);
    job1.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job1, inputPath);
    FileOutputFormat.setOutputPath(job1, outputPathTemp);
    if (job1.waitForCompletion(true)) {
      job2.setJarByClass(Profile2.class);
      job2.setNumReduceTasks(numReduceTask);
      job2.setPartitionerClass(profile2PartitionerFinal.class);
      job2.setGroupingComparatorClass(DocIDComparable.class);
      job2.setSortComparatorClass(DescendingIntComparable.class);

      job2.setMapperClass(Profile2.FrequencyMapper.class);
      job2.setReducerClass(Profile2.FrequencyReducer.class);

      job2.setMapOutputKeyClass(CompositeGroupKeyFreq.class);
      job2.setMapOutputValueClass(Text.class);

      job2.setOutputKeyClass(CompositeGroupKey.class);
      job2.setOutputValueClass(IntWritable.class);

      FileInputFormat.addInputPath(job2, outputPathTemp);
      FileOutputFormat.setOutputPath(job2, outputPath);
      System.exit(job2.waitForCompletion(true) ? 0 : 1);
    }
    */
  }
}
