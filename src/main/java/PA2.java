/**
 * Created by dmadden on 2/20/18.
 */

import com.sun.tools.javac.api.ClientCodeWrapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import sun.security.util.DisabledAlgorithmConstraints;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;

public class PA2 {

  public static class CountersClass {
    public static enum N_COUNTERS {
      SOMECOUNT
    }
  }

  private static class PartitionerInitial extends Partitioner<DocIdUniComKey, IntWritable> {
    public int getPartition(DocIdUniComKey key, IntWritable value, int numReduceTasks) {
      return Math.abs(key.getDocID().hashCode() % numReduceTasks);
    }
  }

  public static class DocIdUniComKey implements Writable, WritableComparable<DocIdUniComKey> {
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

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    conf.set("mapred.textoutputformat.separator", "\t");
    int numReduceTask = 28;

    Path inputPath = new Path(args[0]);
//    Path cachePath = new Path(args[1] + "Cache");
    Path cachePath;
    Path outputPathTemp1 = new Path(args[1] + "Temp1");
    Path outputPathTemp2 = new Path(args[1] + "Temp2");
    Path outputPathTemp3 = new Path(args[1] + "Temp3");
    Path outputPathTemp4 = new Path(args[1] + "Temp4");
    Path outputPath = new Path(args[1]);

    Job job1 = Job.getInstance(conf, "pa2_job1");
    Job job2 = Job.getInstance(conf, "pa2_job2");
    Job job3 = Job.getInstance(conf, "pa2_job3");
    Job job4 = Job.getInstance(conf, "pa2_job4");
    Job job5 = Job.getInstance(conf, "pa2_job5");

    job1.setJarByClass(PA2.class);
    job1.setNumReduceTasks(numReduceTask);
    job1.setPartitionerClass(PartitionerInitial.class);

    job1.setMapperClass(Job1.Job1Mapper.class);
    job1.setReducerClass(Job1.Job1Reducer.class);
    job1.setOutputKeyClass(DocIdUniComKey.class);
    job1.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job1, inputPath);
    FileOutputFormat.setOutputPath(job1, outputPathTemp1);
    if (job1.waitForCompletion(true)) {
      job2.setJarByClass(PA2.class);
      job2.setNumReduceTasks(numReduceTask);
//      job2.setPartitionerClass(PartitionerInitial.class);

      job2.setMapperClass(Job2.Job2Mapper.class);
      job2.setReducerClass(Job2.Job2Reducer.class);

      job2.setMapOutputKeyClass(IntWritable.class);
      job2.setMapOutputValueClass(Text.class);
      job2.setOutputKeyClass(IntWritable.class);
      job2.setOutputValueClass(Text.class);

      FileInputFormat.addInputPath(job2, outputPathTemp1);
      FileOutputFormat.setOutputPath(job2, outputPathTemp2);

//      System.exit(job2.waitForCompletion(true) ? 0 : 1);
      if (job2.waitForCompletion(true)) {
        job3.setJarByClass(PA2.class);
        job3.setNumReduceTasks(numReduceTask);
//        job3.setPartitionerClass(PartitionerInitial.class);

        job3.setMapperClass(Job3.Job3Mapper.class);
        job3.setReducerClass(Job3.Job3Reducer.class);

        job3.setMapOutputKeyClass(Text.class);
        job3.setMapOutputValueClass(Text.class);
        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job3, outputPathTemp2);
        FileOutputFormat.setOutputPath(job3, outputPathTemp3);
//        System.exit(job3.waitForCompletion(true) ? 0 : 1);
        if (job3.waitForCompletion(true)) {
          Counter someCount = job2.getCounters().findCounter(CountersClass.N_COUNTERS.SOMECOUNT);
          job4.getConfiguration().setLong(CountersClass.N_COUNTERS.SOMECOUNT.name(), someCount.getValue());

          job4.setJarByClass(PA2.class);
          job4.setNumReduceTasks(numReduceTask);
//          job4.setPartitionerClass(PartitionerInitial.class);

          job4.setMapperClass(Job4.Job4Mapper.class);
          job4.setReducerClass(Job4.Job4Reducer.class);

          job4.setMapOutputKeyClass(IntWritable.class);
          job4.setMapOutputValueClass(Text.class);
          job4.setOutputKeyClass(IntWritable.class);
          job4.setOutputValueClass(Text.class);

          FileInputFormat.addInputPath(job4, outputPathTemp3);
          FileOutputFormat.setOutputPath(job4, outputPathTemp4);
//          System.exit(job4.waitForCompletion(true) ? 0 : 1);
          if (job4.waitForCompletion(true)) {
            FileSystem fs = FileSystem.get(conf);
            FileStatus[] fileList = fs.listStatus((outputPathTemp4),
                    new PathFilter() {
                      public boolean accept(Path path) {
                        return path.getName().startsWith("part-");
                      }
                    });
            for(int i=0; i < fileList.length;i++){
              job5.addCacheFile((fileList[i].getPath().toUri()));
            }

//            job5.addCacheFile(outputPathTemp4.toUri());

            job5.setJarByClass(PA2.class);
            job5.setNumReduceTasks(numReduceTask);
//            job5.setPartitionerClass(PartitionerInitial.class);

            job5.setMapperClass(Job5.Job5Mapper.class);
            job5.setReducerClass(Job5.Job5Reducer.class);

            job5.setMapOutputKeyClass(IntWritable.class);
            job5.setMapOutputValueClass(Text.class);
            job5.setOutputKeyClass(IntWritable.class);
            job5.setOutputValueClass(Text.class);

            FileInputFormat.addInputPath(job5, inputPath);
            FileOutputFormat.setOutputPath(job5, outputPath);
            System.exit(job5.waitForCompletion(true) ? 0 : 1);
          }
        }
      }
    }
  }
}
