/**
 * Created by dmadden on 2/20/18.
 */

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.StringTokenizer;

class Job1 {
  static class Job1Mapper extends Mapper<Object, Text, PA2.DocIdUniComKey, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private final Text word = new Text();
    private final IntWritable docID = new IntWritable();
    private PA2.DocIdUniComKey comKey = new PA2.DocIdUniComKey();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String values[] = value.toString().split("<====>");

      if (values.length >= 3) {
        String id = values[1];
        String article = values[2];
        String[] sentences = article.split("\\.");

        for (String sentence : sentences) {
          if (!sentence.equals("")) {
            StringTokenizer itrWord = new StringTokenizer(sentence);

            while (itrWord.hasMoreTokens()) {
              String unigram = itrWord.nextToken().toLowerCase().replaceAll("[^a-z0-9. ]", "");
              word.set(unigram);
              docID.set(Integer.parseInt(id));
              comKey = new PA2.DocIdUniComKey(docID, word);
              context.write(comKey, one);
            }

          }
        }
      }
    }
  }

  static class Job1Reducer extends Reducer<PA2.DocIdUniComKey, IntWritable, PA2.DocIdUniComKey, IntWritable> {
    private final IntWritable result = new IntWritable();

    public void reduce(PA2.DocIdUniComKey key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
      int sum = 0;

      for (IntWritable val : values) {
        sum += val.get();
      }

      result.set(sum);
      context.write(key, result);
    }
  }
}
