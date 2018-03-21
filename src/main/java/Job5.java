/**
 * Created by dmadden on 2/20/18.
 */

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Job5 {
  private static HashMap<String, String> hmap = new HashMap<String, String>();

  static class Job5Mapper extends Mapper<LongWritable, Text, PA2.DocIdUniComKey, Text> {
    private PA2.DocIdUniComKey comKey = new PA2.DocIdUniComKey();
    private final Text compValue = new Text();
    private final Text word = new Text();
    private final IntWritable docID = new IntWritable();
    private final static IntWritable one = new IntWritable(1);

    @Override
    public void setup(Context context) throws IOException {
      URI[] cacheFiles = context.getCacheFiles();
      if (cacheFiles != null && cacheFiles.length > 0) {
        try {
          BufferedReader reader = null;
          for (int i = 0; i < cacheFiles.length; i++) {
            try {
              File cacheFile = new File(cacheFiles[i].getPath());
              reader = new BufferedReader(new FileReader(cacheFile));
              String line;
              while ((line = reader.readLine()) != null) {
                System.out.println(line);
                String[] lineInfo = line.split("\t");
                String docId = lineInfo[0];
                String unigram = lineInfo[1];
                String tf = lineInfo[2];
                String tfid = lineInfo[3];

                String key = docId + "\t" + unigram;
                String value = tf + "\t" + tfid;

                hmap.put(key, value);
              }
            } catch (IOException e) {
              e.printStackTrace();
            } finally {
              try {
                reader.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      //TODO: Code to read text input, then: split on period, tokenize to get unigram.
      String values[] = value.toString().split("<====>");
      if (values.length >= 3) {
        String id = values[1];
        String article = values[2];
        //TODO: Check if needed to split on sentences first
        String unigram = article.toLowerCase().replaceAll("[^a-z0-9. ]", "");
        StringTokenizer itr = new StringTokenizer(unigram, ".");
        while (itr.hasMoreTokens()) {
          unigram = itr.nextToken();
          if (!unigram.equals("")) {
//            word.set(unigram);
//            docID.set(Integer.parseInt(id));
//            comKey = new PA2.DocIdUniComKey(docID, word);
//            context.write(comKey, compValue);
          }
        }
      }

      //TODO: Join from distributed cache on <compKey, value> pair
      //TODO: Get TFIDF for each unigram, select top 5 TFIDF values, calculate Sentence-TFIDF for each sentence

      //TODO: Write output: <docID, {sentence, Sentence-TFIDF}>
    }
  }

  static class Job5Reducer extends Reducer<IntWritable, Text, IntWritable, Text> {
    private final IntWritable docId = new IntWritable();
    private final Text compValue = new Text();

    public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      //TODO: Code to select top 3 sentences

      //TODO: Write output: <docID, (top 3 sentences)>
    }
  }
}
