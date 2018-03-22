/**
 * Created by dmadden on 2/20/18.
 */

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

class Job4 {
  static class Job4Mapper extends Mapper<LongWritable, Text, IntWritable, Text> {
    private final IntWritable docId = new IntWritable();
    private final Text compValue = new Text();

    private long someCount;

    @Override
    protected void setup(Context context) throws IOException,
            InterruptedException {
      super.setup(context);
      this.someCount  = context.getConfiguration().getLong(PA2.CountersClass.N_COUNTERS.SOMECOUNT.name(), 0);
    }

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      double idf, N, tfidf;
      String tempValue;

      String[] values = value.toString().split("\t");
      String unigram = values[0];
      String id = values[1];
      double tf = Double.parseDouble(values[2]);
      double ni = Double.parseDouble(values[3]);

      N = this.someCount;
      idf = Math.log10(N / ni);
      tfidf = tf * idf;

      tempValue = unigram + "\t" + tf + "\t" + tfidf;
      docId.set(Integer.parseInt(id));
      compValue.set(tempValue);
      context.write(docId, compValue);
    }
  }

  static class Job4Reducer extends Reducer<IntWritable, Text, IntWritable, Text> {
    private final IntWritable docId = new IntWritable();
    private final Text compValue = new Text();

    public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      docId.set(Integer.parseInt(key.toString()));

      for (Text val : values) {
        compValue.set(val);
        context.write(docId, compValue);
      }
    }
  }
}
