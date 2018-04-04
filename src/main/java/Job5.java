
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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

class Job5 {
  private static HashMap<String, String> idUniToValue = new HashMap<String, String>();

  static class Job5Mapper extends Mapper<LongWritable, Text, IntWritable, Text> {
    private final Text compValue = new Text();
    private final IntWritable docID = new IntWritable();

    @Override
    public void setup(Context context) throws IOException {
      URI[] cacheFiles = context.getCacheFiles();
      if (cacheFiles != null && cacheFiles.length > 0) {
        try {
          BufferedReader reader = null;
          for (URI cacheFile1 : cacheFiles) {
            try {
              File cacheFile = new File(cacheFile1.getPath());
              reader = new BufferedReader(new FileReader(cacheFile));
              String line;
              while ((line = reader.readLine()) != null) {
                String[] lineInfo = line.split("\t");
                String docId = lineInfo[0];
                String unigram = lineInfo[1];
                String tf = lineInfo[2];
                String tfid = lineInfo[3];

                String key = docId + "\t" + unigram;
                String value = tf + "\t" + tfid;

                idUniToValue.put(key, value);
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
      String values[] = value.toString().split("<====>");

      if (values.length >= 3) {
        String id = values[1];
        String article = values[2];
        String[] sentences = article.split("\\.");

        for (int i = 0; i < sentences.length; i++) {
          String sentence = sentences[i];
          TreeMap<String, Double> top5Words = new TreeMap<String, Double>();
          TreeMap<Double, String> top5WordsTFIDF = new TreeMap<Double, String>();

          if (!sentence.equals("")) {
            StringTokenizer itrWord = new StringTokenizer(sentence);

            while (itrWord.hasMoreTokens()) {
              String originalWord = itrWord.nextToken();
              String stripWord = originalWord.toLowerCase().replaceAll("[^a-z0-9 ]", "");
              String lookup = id + "\t" + stripWord;
              String comKey = id + "\t" + originalWord;

              if (!stripWord.equals("") && !top5Words.containsKey(comKey)) {
                try {
                  double tfidf = Double.parseDouble(idUniToValue.get(lookup).split("\t")[1]);
                  top5Words.put(comKey, tfidf);
                  top5WordsTFIDF.put(tfidf, comKey);

                  if (top5Words.size() > 5) {
                    String firstKey = top5WordsTFIDF.get(top5WordsTFIDF.firstKey());
                    top5Words.remove(firstKey);
                    top5WordsTFIDF.remove(top5WordsTFIDF.firstKey());
                  }
                } catch (NullPointerException e) {

                }
              }
            }
          }

          double sentenceTFIDF = 0;
          for (double f : top5Words.values()) {
            sentenceTFIDF += f;
          }

          String tab = "\t";
          if (sentence.contains(tab)) {
            sentence = sentence.replaceAll(tab, " ");
          }

          sentence = sentence.replaceAll("\\s+", " ");
          String tempValue = i + "\t" + sentence + "\t" + sentenceTFIDF;
          docID.set(Integer.parseInt(id));
          compValue.set(tempValue);
          context.write(docID, compValue);
        }
      }
    }
  }

  static class Job5Reducer extends Reducer<IntWritable, Text, IntWritable, Text> {
    private static TreeMap<String, Double> top3 = new TreeMap<String, Double>();
    private static TreeMap<Double, String> top3TFIDF = new TreeMap<Double, String>();
    private final IntWritable docId = new IntWritable();
    private final Text top3Sentences = new Text();

    public void reduce(IntWritable key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {
      top3 = new TreeMap<String, Double>();
      top3TFIDF = new TreeMap<Double, String>();

      for (Text sentenceAndTFIDF : values) {

        if (!top3.containsKey(sentenceAndTFIDF.toString())) {
          String[] sentenceInfo = sentenceAndTFIDF.toString().split("\t");
          String sentenceOrder = sentenceInfo[0];
          String sentence = sentenceInfo[1];
          double tfidf = Double.parseDouble(sentenceInfo[2]);
          top3.put(sentenceOrder + "\t" + sentence, tfidf);
          top3TFIDF.put(tfidf, sentenceOrder + "\t" + sentence);

          if (top3.size() > 3) {
            Double lowestValue = top3TFIDF.firstKey();
            String mapKey = null;
            Set<String> keys = top3.keySet();

            for (String k : keys) {
              if (top3.get(k).equals(lowestValue)) {
                top3TFIDF.remove(lowestValue);
                mapKey = k;
                break;
              }
            }

            top3.remove(mapKey);
          }
        }
      }

      String tempValue = "";
      Set<String> keys = top3.keySet();
      for (String k : keys) {
        String[] kContent = k.split("\t");
        if (kContent.length == 2) {
          tempValue += kContent[1] + ".";
        }
      }

      docId.set(Integer.parseInt(key.toString()));
      top3Sentences.set(tempValue);
      context.write(docId, top3Sentences);
    }
  }
}
