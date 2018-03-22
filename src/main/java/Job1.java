Job2.java                                                                                           0000600 0005127 0003025 00000004536 13254700526 012155  0                                                                                                    ustar   maddendb                        under                                                                                                                                                                                                                  /**
 * Created by dmadden on 2/20/18.
 */

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class Job2 {
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
      Set<String> uniqueIDs = new HashSet<String>();
      double maxFreq = 0;
      double tf;
      String tempValue;

      for (Text val : values) {
        valuesCopy.add(val.toString());
      }

      for (String val : valuesCopy) {
        String[] valuesSplit = val.split("\t");
        int frequency = Integer.parseInt(valuesSplit[1]);
        if (frequency > maxFreq) {
          maxFreq = frequency;
        }
      }

      for (String val : valuesCopy) {
        String[] valuesSplit = val.split("\t");
        String unigram = valuesSplit[0];
        int frequency = Integer.parseInt(valuesSplit[1]);
        tf = 0.5 + 0.5 * (frequency / maxFreq);
        tempValue = unigram + "\t" + frequency + "\t" + tf;
        compValue.set(tempValue);
        docId.set(Integer.parseInt(key.toString()));
        context.write(docId, compValue);
      }

      if (!uniqueIDs.contains(key.toString())){
        uniqueIDs.add(key.toString());
        context.getCounter(PA2.CountersClass.N_COUNTERS.SOMECOUNT).increment(1); //Increment the counter
      }
    }
  }
}
                                                                                                                                                                  Job3.java                                                                                           0000600 0005127 0003025 00000003273 13254700526 012153  0                                                                                                    ustar   maddendb                        under                                                                                                                                                                                                                  /**
 * Created by dmadden on 2/20/18.
 */

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;

class Job3 {
  static class Job3Mapper extends Mapper<LongWritable, Text, Text, Text> {
    private final Text unigramKey = new Text();
    private final Text compValue = new Text();

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      String[] inputArray = value.toString().split("\t");
      String docId = inputArray[0];
      String uni = inputArray[1];
      String freq = inputArray[2];
      String tf = inputArray[3];

      unigramKey.set(uni);
      compValue.set(docId + "\t" + tf);
      context.write(unigramKey, compValue);
    }
  }

  static class Job3Reducer extends Reducer<Text, Text, Text, Text> {
    private final Text unigramKey = new Text();
    private final Text compValue = new Text();

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      ArrayList<String> valuesCopy = new ArrayList<String>();
      double ni;
      String tempValue;

      for (Text val : values) {
        valuesCopy.add(val.toString());
      }

      ni = valuesCopy.size();

      for (String val : valuesCopy) {
        String[] inputArray = val.split("\t");
        String docId = inputArray[0];
        String tf = inputArray[1];
        tempValue = docId + "\t" + tf + "\t" + ni;
        unigramKey.set(key.toString());
        compValue.set(tempValue);
        context.write(unigramKey, compValue);
      }
    }
  }
}
                                                                                                                                                                                                                                                                                                                                     Job4.java                                                                                           0000600 0005127 0003025 00000003533 13254700526 012153  0                                                                                                    ustar   maddendb                        under                                                                                                                                                                                                                  /**
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
                                                                                                                                                                     Job5.java                                                                                           0000600 0005127 0003025 00000013264 13254773066 012167  0                                                                                                    ustar   maddendb                        under                                                                                                                                                                                                                  /**
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

          if (!sentence.equals("")) {
            StringTokenizer itrWord = new StringTokenizer(sentence);

            while (itrWord.hasMoreTokens()) {
              String originalWord = itrWord.nextToken();
              String stripWord = originalWord.toLowerCase().replaceAll("[^a-z0-9. ]", "");
              String lookup = id + "\t" + stripWord;
              String comKey = id + "\t" + originalWord;

              if (!stripWord.equals("") && !top5Words.containsKey(comKey)) {
            	try {
                  double tfidf = Double.parseDouble(idUniToValue.get(lookup).split("\t")[1]);
                  top5Words.put(comKey, tfidf);
                } catch (NullPointerException e) {
                  
                }
            	
                if (top5Words.size() > 5) {
                  top5Words.remove(top5Words.firstKey());
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

    public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
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
                                                                                                                                                                                                                                                                                                                                            PA2.java                                                                                            0000600 0005127 0003025 00000014723 13254767166 011757  0                                                                                                    ustar   maddendb                        under                                                                                                                                                                                                                  /**
 * Created by dmadden on 2/20/18.
 */

import org.apache.hadoop.conf.Configuration;
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PA2 {

  public static class CountersClass {
    public enum N_COUNTERS {
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
    int numReduceTask = 32;

    Path inputPath = new Path(args[0]);
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

      job2.setMapperClass(Job2.Job2Mapper.class);
      job2.setReducerClass(Job2.Job2Reducer.class);

      job2.setMapOutputKeyClass(IntWritable.class);
      job2.setMapOutputValueClass(Text.class);
      job2.setOutputKeyClass(IntWritable.class);
      job2.setOutputValueClass(Text.class);

      FileInputFormat.addInputPath(job2, outputPathTemp1);
      FileOutputFormat.setOutputPath(job2, outputPathTemp2);

      if (job2.waitForCompletion(true)) {
    	Counter count = job2.getCounters().findCounter(CountersClass.N_COUNTERS.SOMECOUNT);
          
        job3.setJarByClass(PA2.class);
        job3.setNumReduceTasks(numReduceTask);

        job3.setMapperClass(Job3.Job3Mapper.class);
        job3.setReducerClass(Job3.Job3Reducer.class);

        job3.setMapOutputKeyClass(Text.class);
        job3.setMapOutputValueClass(Text.class);
        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job3, outputPathTemp2);
        FileOutputFormat.setOutputPath(job3, outputPathTemp3);

        if (job3.waitForCompletion(true)) {
          job4.getConfiguration().setLong(CountersClass.N_COUNTERS.SOMECOUNT.name(), count.getValue());

          job4.setJarByClass(PA2.class);
          job4.setNumReduceTasks(numReduceTask);

          job4.setMapperClass(Job4.Job4Mapper.class);
          job4.setReducerClass(Job4.Job4Reducer.class);

          job4.setMapOutputKeyClass(IntWritable.class);
          job4.setMapOutputValueClass(Text.class);
          job4.setOutputKeyClass(IntWritable.class);
          job4.setOutputValueClass(Text.class);

          FileInputFormat.addInputPath(job4, outputPathTemp3);
          FileOutputFormat.setOutputPath(job4, outputPathTemp4);

          if (job4.waitForCompletion(true)) {
            FileSystem fs = FileSystem.get(conf);
            FileStatus[] fileList = fs.listStatus((outputPathTemp4),
                    new PathFilter() {
                      public boolean accept(Path path) {
                        return path.getName().startsWith("part-");
                      }
                    });
            for (FileStatus aFileList : fileList) {
              job5.addCacheFile((aFileList.getPath().toUri()));
            }

            job5.setJarByClass(PA2.class);
            job5.setNumReduceTasks(numReduceTask);

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
                                             PA2.jar                                                                                             0000600 0005127 0003025 00000051250 13254773104 011573  0                                                                                                    ustar   maddendb                        under                                                                                                                                                                                                                  PK  �cvL            	  META-INF/��   PK           PK  �cvL               META-INF/MANIFEST.MF�M��LK-.�K-*��ϳR0�3��r.JM,IM�u�	X���*h�%&�*8��%� �k�r�r PKUz�CD   E   PK  �cvL            
   Job1.classU��J�@ƿi�n�MMm}�
�C���R�@��7� )qS����T��C�3
��߷�ow���� \�Ģ�ؠopl0 t��j��:��z��<wUŕ��`5p����i���<�X�y�bB|�y~��._\���E]�|�顣�k��"t`�B_�$w�u�9�
����D��#4��HJ�vŝ�*i]�@?q$���@��z�8B[t .�j!�~PKc�C"�   '  PK  �cvL               Job1$Job1Mapper.class�V�SW�.$�����h��h��D���JQj$�5�U�vIn�B؍����e[����i�/u��C�b��Sg����QZz�0���C����s�~���s/����
���L�!��VQ�3A4���*�k�$ǈ
em��6�
Ϋ�o*�(�4Ǹ�]�$�qY��RpE�W�k�*�pM�^�xoK�wT���4-2�#�Pm[��9e;��Q023">cdm�7�x�r/;�kL�E�o�v��+ێ�E���Y;�<����#�C�iRd/Y�кBKZ�p��F�(�{R�[7N�ߴLw���O��Cv���K��8_��θ����-�|`���f��F<oX�����ȸ}���a:"[ʈ��Q(�u�O�R�v��-xx<��Sr�c�c�Ġ�Ÿ�k�V�#�1�L�9�pKy���b������%0е��t��ǉg���g�����6����KNF������c]rcG���o���V��д�&���b?���6�4�(Ⱦ8I��37�0TMu1@CT���F�5���Vnܞ�y[8�Ӓ��Xа���[-4y��}��DW�5���Q��@Ç�HN��?�Ќ��SqtK��%jHIE�|�僘�8i�9���/����f�=������d��#CO%���=>1���)\��`PM��t��8{��v�y��i�>��^�ǀ�Q2�tF*�R��-��P��1���#���C���Q�(�����5�T��#
y##�y��
����puQP��63��IT
�S4���$��Ѥ��Tt�{FK��-Lo{y���J����|h�MRX��7��U��<��C'I�PMp8�{��+˨~�~�ITH>�z�,�<T�SK�z��U�@&���]����,~�(#�ʨI�t�J�[|��|�	���w'z \+׸������{Ԉ	ޅ�+�;�C�2�ƵG�uE-#��tB����qO���ho�&� >���}$p��3�2��B�.c?E�I��#�8po�����~q�cIF7L�����G_)�64b0B�	b�:qnߋ����%1~�����'�_&����8��
Gǫ�'�	��2�'��y��_%��[�	����a?Nb�����A��I2�-���P�Hǯ��,�����4�nQ(�ǅ�J��M&�Hz�'4n}��l�6~C�x�46�^�R�"H���1���](PK˔\��  "
  PK  �cvL               Job1$Job1Reducer.class�U]Sa~^XY�ɯ���J�MST$�P%m���vpw�e)��'t�/躙±f��]����+�NZ�E����<�9{���� �X���AC80�F^#�Ea~��~���%c��,bB�$��T˕��З1̂���ܺ*�+y�(ɚ!�u랩YJ��ΐu\�5+���1)#�24g4]]�lfU�7�Q�)�9#���յ����>c�Һ����R.�eSb��-u�(�dn%���d6�'�\T􂜶T�fr�M��T�ms��;Ù6�ZJ��M�d1����ʩ%K3t�A�W���X��>)���K��p�Ȁǂ��W"R_��@]b+�5g�������>{V���S4>��E#;��J8����0$4�/".�
4K�bVB�$�0'a�ㆈ4C�a��qV�"���G��O�'�C��B���V,���Y� �V��'���a0t�y�f�d��:���S�Ш��v�3��Q�u��lӡ]�v�n?����E�)�{�TPg��B ���)5�B�~�k7��I� w^e��*��"�<xOIsҏ��nZI�3#�n����`η^�>mt��
D @�j+yp��$E�IgT!��ٰ�KtqMd؅#�j:]U4>�
��
���.T�	q�
�Cuۀc<�^L�r�R�)Lb�������
/��}8O7.���J�����D��4N��h$���.��*�_����#@�5�~��"N��9{1���/ƇN�.����PK����  �  PK  �cvL            
   Job2.classU��J�@ƿi�n��&�>���`A<��������II���4}/O>@J��P���7��f������[�$�!��X���5W���{�ƅ�k��V�g�X�ݽ�}��#��/��`\Θ?�_�ϔ�w�B�[�T����鄫�[�=XiS@H�
�?F�霳� u��6�w�	��%_�C2P�/٩�������#���	�m��-�+:�,�� ��PK�.O��   '  PK  �cvL               Job2$Job2Mapper.class�U�NA��n;��P�Պ( B[(�zo-P�m;)K��f�U����L�I�}�x���D�̙�s��sf�ۏ/_�ª�n�9�U4!@�Ux�7����J��U��]�x�㾊�吒CR*�U�c��J���8��E��)2f-���m��&kzѲ�a%2���1\=_i�@�ڰW�rU0�66X�.i�&�p'<��*�2c�&�5L�X��gY�����a�3��-��1M�̔�JET6"�e-��+�3ripD1Q�DbA�m���N�T��͂�]�2+�9f	v�(��[u(���>����X!�0�?�E.�]��ꉲn�K�uQ �'w����;ԿjΪ:1g�&�[��ĭ����s��§��Esx��O5d0��LC���s@u�u�4]5�E�hx�R祆��%K�\�X�;(��a�i�zx1x~mb�_q��:
��ү��p� �&�j��xH��Z{�:#��q*�Fm���5��e�b�DND�4r:�GI��ۺS�d�j�(C�VEЩ�Ȇ�I�&=��F�'����>�;9�����c0N��V֐��.�a����Ij�}F��>j]0����5�;�m���^oó�D/��-��tLU���n�B΂�]j%ړ����yu�{�5��O2�+J2�+�d��>A	{?���d�������F�0����Bq�,�jh?�a���+D�zV���Z��i��5c�NB�m�r��$s&���E��.��:b�ꔄ����{�a�>�Q&��&��
�1�OPK��%  �  PK  �cvL               Job2$Job2Reducer.class�V[wU�Nn'�L���H[J�\��B)�C���
N�!����Ʉ�����wE��Z>��X�]�œ�	}��g�>ӦM� �`W��m�����}��������NBz8���!�C�IpOb�%x�F�	=~����q��/�	q�c��A	UxA�q������I	5��E!^���ؾ_��%l���B�!	U̝�#�a���8�r�2��F"�d�3�TX�(�a5<�$#֌pT�M�R��j�/a�f�tNeX��A�z�"MO��kV�3�0���2�dS�t�77:�����L5�K�:�2t��䨮�fWZ�f�,ÙУ���(�pZ�Sᨥ��s�F����vc�Om�&�,�[J��a%c{gS�0�;�!G����BB�X��g9Lr��R�b�L:Q�ɜm_�Ύ�:�"��]|�eܱ��ŁP����FԄ�߄���;��R�ș	��&��w�j�*�.c+�����ш&�࢛�؂f�e6�9KK�;MS�Ӳto*gf{��p\�({eX�Q��8��2.ࢌKxU�k��P1��ejz���d\��o�xW����?����)�-�-�;B�+�������2�G2>�g2>�_��
7e�`'����^�� 2�g��#�M� ���#�������=��,�,/��7]֖�����Xj�D��M#�5��#S��a%�k�GE�M�,/��'B��b�fkLΒ�SIRa�^�6
tw6��~sh�m�Ʌ�*g2%h�f3��p�FE�(�x%�Qu�y!�ySS�E��E�^>�Բ3�.�� 65��t��9$�/YE���R4Q�o�G:[(�r"�Y�{U�{���Xo��8eԬ%*���x��n{H�[
H���jM-3�*��[ύ�K���a�#��4�O��:����B�l�ĕ����`���5͡���Nq�Ӆ���D���I�T���4��h̨]�x�q����u�����C�ϐ�Ƶpa=ʱ�f�n�9F�TA���/⚀D�h�l�����(c�kD/�� �t�<��+�t�QY��2�ZE�rղ<�Gʂe���P�3P�V�y^�z�
��_]���	Qʗc���	�8�h�N���y�+�ɽUv�ƛ��<�D����Rk��j�cA�Q�,�b-��c�KA�X�nauqT��`�]��Q�&'���4�`%ɍ�aEm3�)~�(z�N��)"<E���"�\��4��m��Z�"�SdE;������^�>V��l�X��8HV�]xX?�n�V�GG;����� 
�_p� ϒ�;
t�'3-��"�����<����o3�U�I|����:JR��Ʀpli�c�L{씯�✂�&RD�,�
�q܀�.Fji���SC<�o��}��PK����  z  PK  �cvL            
   Job3.classU�OK�@�ߴi7ݦ���m��`�X��?���&$%nJ���<z����������v��ϯ���,����B��H.yU�\�k�\䮪�2��wn�Ѹ=�|��$4ON��`Q���f���ׄ�g��B�SQ�)_ez�h��{s:��L��h�;�2}H֜ni2�7Q��~����.����]qcQ%����'�d�r�!��������ZaDC��PK�<�   '  PK  �cvL               Job3$Job3Mapper.class�U�NA���v�e-P@XA�OiKK��_�(T�j���x�m'eI���n�[��DK"�����=�g
�E0��lzf�����93���� ��)#"#���H��	1#�� 
f���2n(P"%D\�0�d�+�pS�mw��iT����e�[N%��zi�'���e�I�J��7�,Y5{C�68��0w���n0HY�L��a��Z�;�z�JoM�e��e��A͙&w�U�^�u�Z�sҼeV^8�+�d~SW�-���r�ē�t����a�(UY�)q�5,�.##c�������
����Y��α�8{��B$���֓Uݬ$׊ۼD�'5H���<u�R�N���-�O��ܬ ��"�S�U��_ŀ�@ς2�x�%��2��+��aV�F����b���D��Fe<�[$���2���b�l�b�?:�� �'���r0��a���q���G�u���?�g�8�9�c����A9�#'��:��9�t����,f�!������h(��Ϗr�Uh=r�0F�g��bI���Es��5�h��5�q8�ۃgs�O��S�ڦ�#j9� ����R3�NzGc���x���D -i�7��i��J�5ɗJ˚�O����4A����P�.�ׄ*�T-@�����¨�P��k�D}�"<��j��\&9I�q*)O� /1�+d�'�0���=K�?E��V�>��Gw|�D=�<�3�_�x#]%o1NSf���r�1"1��� �PK�͡�    PK  �cvL               Job3$Job3Reducer.class�VKsE�FZy���#��X��䇈�[2�I@�lCd� a%-�:���j	�+Up�c��*�W�8�'�����l$[��+�ʁ�zz���~Ύ�|��3 QS�4ǌ� �e��� �Y=���=d#x2'��`A�+�d9��qMA?>��HAӂ��.ȼ���`y��9�9>aP�Yq���C,o�����u#���m�I�vjŸ�e�]sV�j�`�5-��2�Ui�.�i޴��F�h�+z�*�\��(7��myŠ�,�p�z�n�����п�SUݪ�r��
���5�y�*u�_ܑ��������{�����>�Q����+T���K�㙶U������Y�t��R�b����g_*��^�(;�Y��xGF���������/q�k�`7ܒq�+_��S"Ygp�A�Oc�0��4����ixf55����Y�h�T��g4_*ncM�|��|��.�R���0�[؂�V���*J(3��/�o�ղ�0� �0Ul���a���HaD�VE"{v�/���W�.C�{>ڇ~U�C�vۥ��w���{�t݆������p<��w#S'��^_�á��}�,3���hB��v��j�8%��^�.;�:��u�;�w�c՝�I��ǻ�w��ǰ� ���V���T��a�`�����+�֥B�����v1;8K�F�^� �%4��E�����0�#.C{Fk,�	��B��&��B�eC�I��&Ѿ��D��d����EZ{I��zȐ����3��Z�)d��1$8��Ҳ&ir�&�[���Ҥ&z��	���ä�c�O&rt��#iES��鈦H�iuQrwT#�A!��6w2�ı&��y�	�Q��	bC'f\�]'m���/��2K��^q�.�I$q	c��O`)������$�	SxH	~!�'x��LV�g_���	�$�(��8I8�`�
�=G_P���Kn@�����M̑�E�D߆�	���(�sl�	m�O=~K��	�`.Q�b�����P�1�%1�%|��PK7�\�  �  PK  �cvL            
   Job4.classU��J�@��m�N:MM|�
.ԅu�eqS,��J���")qR���rUp��P��*�9g�3s����0űE�A�``0$tE2}�U�rI�n��r�����`D��߻�F��,�����<;_�y�bB|�y~��._\���E]�|�顣.���E���3���I����1Ys���鿉�?�;�h�t��4�;UҺ؁>~�Hv+ׁ\��o�mѡ�@��F4Dx PK�#[h�   '  PK  �cvL               Job4$Job4Mapper.class�VmSU~.�d��&(%��&�%k-	��@m0@m(�V�K������mѪ������~ё��������G8��`yK�q��ɹo�>���{���ׯ�8����1!�� z��[����E	���r�)	Ӹ�W��zW��K���
��0�yW%t ��[\\�⊄"���|�"G�!�m��n��%�=o�(��ê�S)����lfZ3�yݾai��TU���Q3���2t7߰���\�n�Ԝ��6�a�Mh�f_`�$��B�(FkA��9���Z��SWm��>��3t��伮�V����j�!�hb����ZvJjzV1MՊo��rs��ZI5m���"n���,j]����x������C(9��C6_���\c�5s��9t(��������ªrWIW���_ZUKp�_R�ó�=\�h8VI�����1�Ύ�e����G����)��S���2R��`IDYF+24���#����8^��c��G@�!�ć",u�"wq��k�D����K�V-�������X�}��S�����_2�:�e���+"��,�Y�s�uDD����чʐ:�Ua�p����?I\�;��e9���w)D*�M�Zű���D"��K��ޣI�����cq7i�V�M��۹��s�׊�=K����@q~v����À�	�R��u�����?��T(\~`'af�Tu���5$r�k�ZcV5��t���[�<�2e8n�
��UW�u6��b���*�
ٮ�3��Db����Щ780�}�ɣ��r��;S�W��z�畹��y�0T��oy���*Ѽ��u}O͕n���Fķ�ׁBFi��G/} ��LB�v�0�m�I�Z����@��zY�d�v�6�R��rs�u��R׷�'ZfH��2�Z�����q����Bm45�j���ϐ��ڏ�E�Q�1�i䣽�8�1�їI���6�d������6�cA�XF���L(�m�m<��N$�Bߏ���h��D��G�;��G蠘N��:9Pg4��ɭ��zte$�,�B��6��� ��|�@�N$�"ه Q�Fdw��Dv��%�3D�$���'�>�c1�$�s�I��U�%���c
��g�1��<�c��Yx�g�<��6Cv@�d�Ce��w�G	_C�:.a�b�oPK�v���  Z  PK  �cvL               Job4$Job4Reducer.class�UISA�:L20��(����"�DA�,J0�ކ��a&5�(g��GO��� Zeq�,��$`R�r�0ݯ߼�{�������G C�+�!$�W��*ԣOF�wa�W0(��^r��F�0*���øB��e�`p'�D4��9oZ����k<��%M3��p԰Y����y��*and�Z:��~��7m��L�nO2T�qi�L�O��n����*�VDH��x2��2n2�Ӧa�;�5nM��l�g��n~]{��Ӛ�
GmntG�6�L!cxٙ��b҈�Y��ĳ-㠓1Š�l&x��M#+㖌i�BLO������A�8�y���}L������+���Ҟ��%�-����o6������3sV����Us��P�(]��e��Uxhdh96��&4����*�ஊ(�d�S1�b��E,����ϲ�VT�ႌ�uR���b𖭺N�c��o���������o���Q��OC{	Q�۲r�'K*m3f[��bh�JxU��ԗ�H	����rZ24��:�t/e9���QA5�Q�R�|�A겢(���e���{B�gQ�x(��c.��ʻ_1(u��g��q.V����Go��FbI>A�+�#.�HFR�֌��X�\�wP��D7��m�[������y �(�$-��t"�K�=4��B
�Q�Uy(���2�K-�.T�W�R5���n��[�y��	/�ʣ����-�H����#��(Ic��8�?B5^���Y��/�C')<�؅n\rB\��"�=$	�]�\N��\�}y��(���a�,HE1�g��餋M�K�5L`��PKf�4  �  PK  �cvL            
   Job5.classmQ�NA}-0������zz0.$Fj"j�:�d�1�x�\&��~��z0�t2��իW�]��� �Q2���:-�.�Xֱ=�;;g�;"`�=OM���u�2X*�oo���^x�ܿ�n,�Z}~ǫq$���I��`�e��Q�bwTQP.�z�vH��7�4�G�KOF�Te�!����kIO��7��K��G�a�V�1+�e����j�q��}��Mu�-Ua�Ƹ�<
�ꯔ��YO;}�DD�L��;����P�h�j��N��r����LR�F�3�/{L
��k�("����i��D����j-a�0034^O��RW/H?�=�)�!�l2�L�I�t�۠cfa|PK�}�t  U  PK  �cvL               Job5$Job5Mapper.class�WxW�o��ٝ��d��LA
H!�$KS
d��B�#4���`
�6�d�fg��Jժm�jZ�Z��`5��
h4Ej�Z��ڊh���j��g���n�cy���}ͷ�s�{�9�?���Ρ��0o������<�^\��������:Wa��*n�F^ܤ��
ޣ��U��fټσ����ܢ��ln��m*6�*>����+����p��;U����ml�`�����ۃ�I[w)���sp�l��=R|������ح���
>�"�O���R|��m���|V6�e�Ç��>�/ȦG�����<����a�|Y�W�U	��d|;%_W����R�[A��7duǖ��)0�ɲ;F�u��.�ݲb��h57$�
ڭP�B�ɹG5F��p�X19�}A8N�ȯ��L��`�����p�lNv�6�V9�F�f"S�G@i��	���Q�n��T���m�l�=2��X̴�df�Kw�-	#t�g�{�ur�i��)�c��K6��X"lE��������>��ƢɊv�qPO��I&ϫ��|�&Н+��-�� M.��|��%��t2V��������~KE�c�������k�͏���4��vǀ���XI;d^���]d�>�F�[C�'���r4
L<��p��G�����(��+Y÷�_zzL`lV� ��a����n�����B9�;�y\��^��A�3��%a�����3�{8���د�~�)8�������ʅ�f�4.�P4<q\��T��B�q��[Y#P�I&@�m��e-�����tL��f4�QF�$~��)	�/�:|+V�gV��L�V�����_ሆg�@�ȵi�5�j��	L8�lNF"WZa������w���~�g5�/(xQ��'��?�@��燹Yh%��/i��N�'J$_��?�S�+����U�<��5�.0锌ְ!'�nSd���q���SW�@���$�Hu�L�^�X��d�}X�[�lk}��+]�4��,1��6k#a����(3`'c	�}o
;�D��VҙGVI��QA)s���8�bz.�e��r�)��3���J�����H=6��4�Gx,&8�r͹r��5�.P�k�(Q�9ѓ��"�(d��p#2��)3gΔ��Lv@�܈w1��ê+#�yK2湧�oO-�C+.�41�p.����m�M������a���b�6�B�;w$J�Qr���V���w C�Pm3����Ȉ�3����"��p4~�y#��%SsŘk���a��tI��A�*��UrI��p���D�qŹu9kf�x:�v<�3���nݶ�m���:��V��,7X�G�68��\rz	���a��u'k�9iv��u�v'��՘�ru�,��+��˰[�u32���h<���f��d�&�)�)�?_O�=�̏˄��Z��{�9�*O��e�ȯ���1��<���ˣ�O�y�C���z���셨�C^[/�w��n���PvR-��m�4�c?��D+e4���&��7#��x��^�UA���]�?�]�.+؁�uW��6���ۦ+)���=}���Pئ{zQԋ1��)��Х{�%)������}��j�ZP,�UZ*�C)c=C��P&������dU������x�����)�/������ztO
�zP����c�%i���~���Gou���sz��A�c'<�E���t��y���'3�G��AJ�����e糖�x���y��0��"{65�P�ɴ0s��y9uKQ���D'f�U��q3x�bj�oc	2��؇:<Δ�y8�����=Aݓ�=E�3��l�2�q�\^	���/������]�71O��Ռn3
0�~@,W�.ml��*�(X���*X�������?j�#��A�`�{�h�2?@\����\�Ρ� ��;�7���"��|�ު&�ϖ���to�tiU��H+���O�:%��̣+�;D���yj��>L"�&��^�;S�r?v��6�ۋ��&)8m+v�gIJN�Wʁ3��Y.r�0�ŢѪA�?�tU�B���XR�<�އ�4\�{�+����)���AJR8�� ��bwV
��+��|�u�^�`��\��/{u�D	�P_WT�+/
��z��K8�+�`+���P.ʛ����1/�7��r�p����]P�=�8���d���T���p�G��u�N檟�{��MW�]d;�>ng~nh���ngӸ��r��F��#��-g=\�tpOJ��[���$���1��啼�^OFx�x��R������11�b��kE�؈��	�W�{��a�^�,:�C�v�C��Ώ&MW9�.�܉���ɟ��1�=U;�D��&�X7���o���bT0b.C��&�g!�)����,�({�D���gDQ�}XX�y��.9~���|����%1繖i�'N�bҖ�ӷ	<�PK*b��	  �  PK  �cvL               Job5$Job5Reducer.class�W�wU��6�$��B[��
e�5R@!�Eh)
EZ(���d��I�LJ���,"�"�+�Pȑ�9~����ܨ�M�6mR��==o�s���w�]&?>��!�y�)��֊�B��X'�M�5�Y/`�{b��/��x��xo��m.�p�v;|"��/���Eh�K�/
gwrA�/[��U@�?��a��0�D��6N�vD�0�g�C7��s�="j�C$wz��.��G�26mQ�w�Ђ�V]UW*�G�)FLW�K�&XA%p��
Ԥp±��ZS�q���FoC�Sq�Z������~��Ma=�V"��Suw*�p8���no�h�5C� 9�h�2ԐO�2LάԪ�$m��B�Qǐ�*]Ga����yMZH]�jW�VKr�����c�Ä�c0H�PH��J4�Mu���`�	���'x�Z]J$aѽ�|�3�Fk��9-���J!4��y�k�&)!���t����ǧF-�
�+`��?��	��{�c��0���6��r]Uj׹R��ܾE�����k}�dN]��G�
@l	�t�ڨ�,w,�ϯ�Q��20�<^�\�c��4"a>^b�V�*Q%a�(m%���c	⠄�pH�a|,��20��c8.��`�8�G,�iA��K�'%��i	��O����91�n4��9�8�G�I8�.⨄K�� r�|�,�
�J��|�J�5\�P�En�L�$��9b7�r���o�C�pT̈́5�� sDS�ߔ�Z��뷨�Wh��7��))�dX�cC��ص!�\�M�N�)�S��2��N��:m��ʧ�!!ᄓ�%�\w�(���Pw0��P�܊5	jdf�+�4cH�E���,�;��@�@�D����Li�d��0t+������J� �'#�<�=1S�,Q�W5��@vhz�0cd۪�0oy܈;"��ofe
c&6u[L	F͹��V|(I<��f,i;F ɿ�(�kqyy��(D|�v�6�[;(|���C	}�T�w�"o6T�n�e�E��I�W���1�x�25� I,$��D��=@ֆ�Ⱦ�V"mw ��ZsI�%�]�ûĥA<�:O�|<� vRt|q�rV܇D�r9}��S��`�XoC��q�ő?�d��8
<6ٖ]�d���c�m��{(\�͑�y�,�1��N����1q��&�!�݆ӤǔL�q<g�� ��1��=N�L��9�>��߹rn����2�����~��%�&���>'���y�������Y��3<�$�U�&��I�E�J�����]�?�^@��A,�,��<3G�X��+���8^��Yq̦��E]���wq��23�_�~L�u7��� �KY��� �hF��A��1��q��;8J^��8��p��<�'�����,},g��s��K	.2.�e��V�
k�Uf��m�5���+gp�lp��b�p���� `)�4
X&�5���5���X��跁u�
@/�hH�w,!V�-=��2Y��S7�+a%0�gY�#dUT�Q6��6�HzG�رz������ϔ��0�)�=wR�$�x�"Ο-�3�4�~�T�L���_PK5Ox!;  �  PK  �cvL               PA2$1.classuQ�J�@=k��1Zo���Z%U4*�)���P/PQЧm�&�4	��7���K� ?J�� �vggvϙ33/�O� �1o@ŀ�Auƈ�Q�&�M?��CΪ�2�w��`(��P^�"9ፀ"*wK�)�%��c�x��x3�b�2����6��F=�N��g0�x{m���p��� J��=ҋ�&ML�dBG��i3(3(1O䒆Y̑O�Us?E��4)CoFj<t��ƕpHY�_]�E��j$.�����aȺ�}��eB�6����à�B�U6hUÿ���QCRI%�g~FP��Hz�(4%⧑uЦ��g�m�,#�,<�=Ѕ��N�=xK���ϯe�f`c����\����C�{�z��ч~�ezW�-�PK��_w  :  PK  �cvL            	   PA2.class�W{xT���͆ٽ{�$7	x- �TRX�$�A�`7�@I�ٽ	���]��R|�Z�*�T[�PX�CJ�[[��/[�ҧ}��G���Þ�w��+�/���̙9�7�93���4�mW��c�	=n�v�zѼ�c����dx�#>���nD�0��s�_�z�nlpc�Q7bq1��$ǍS��M7	�̱E����U�m�]��*���Ʊ]4��G���Iv`������~�O��e���CxX0�c/��2��I��Ș#���~�Y���Ī�9>��/�9��F_��"�q�5���̠��q#���i#͠.�S�i�	ũ�G9���kMd░J�3F]a�tC��
Wk"b0T��q�#�5R]zo�F\1݌3��nߠo�Q=��Rf��X�����%zҞ�����q��0Ǘ8�0ȋ�FR�Js�~g"�
�Ma�����[�5�(C}"�Гzx�X�G�d ���Z�c�gR���Pӓ)#2�2�D�Jf��D*�[3�FR�I��(���V��/X�[��A���onaƌF����8�P�eĒ3�ĩ\'���6�F�zp�D�Lڼ�qzd(8�li�i�@�|g|/�˩__M_���Q���V������W�(�:�0T�YnD2a1ɛ�S�1K�
K�7�db���D �V�LKD\���o+x�p|G�w�W�=��;��~0��p�rX\@/8L��D���.
��
�?�xM�O�3��+�^���c�Q�o̷ޘo�Xo��+����8E���7��������MbiS��|͎�@�P�΅�?���;��X9�T��U���w��?9�r,5U�����-9�K�Ö����"�*G2|i�#l1T	��Յ����8Ǐl������(��Ρ���Ӗc������X��I���Õ�PڠMM�o�t�-1S����G���P4��D�m%�!��D���7�P<m��0U���{-�u��XX��#w�驅�s���ߍ=fﶊ�Q�w�KOo��.H��I�W�rz\�~�EDB'	s�4�˺܈P��.�TP��kxl�����m'j��8��� $Z��RO������3�/��B���.������#�\56gCT|�s�Tݤ�BH�45�u�WM]�x�n�ǃ�{hL�p��D��V���I04�2a\�9��Xۺ���k��N�'�O��%��.Ä�-�[�.&@���xd�����E�L�}�9�p��7�>+m?<J;��Q"%q=&^��v��jc�����8�cJ�6���f��e��΄>��-E���'9j�-��1�+�x(G�[D
���~��y�2�#���\�/��!>t�Y��IQ�xq�
t/�uNg�X�<�����\�\p9].c��P�����x�7D�:BFX>��!j0��W�����¥��Z�S���B(G-�8zR�1��7�.�<�Q��<	�3�,x��<	͐���YxU�>Y�H��*����K+F�����#RnSp	
-��jj��A�(7�5�j�@�A#ЧyԱY\�y�e�Q��,>D@�����`bu����y��^�h�A|X󸲸�vP�-��EV�����e�n*��,�^�ӛ��BM=A��&;�z49����,f��ּ�j��ګ�j�z�P�Z�:��@��5��YO0J��ڪqN�H̴U���Ae �&MqL(�	Em&u�0�8�Ǆ�W��]O��	�1��a�[*�
��1�"uy--�ZeK�Vuz�Z6����V�O&�j��-5�O��bnWdq�jv�ݳ��!�	��y��O�/�B>���!�s����4���s��p�����қ)���p�:T��X�	�W���`-�`���"�È���>z���״Io�xQz~����H࿸�S���_V��Ml<X=6��Lvoa��V� ��kp��V֍;�lc���p�=�v�˶cۉ�l7�c{������<�^�nv�W�{���(;���M<����?����I��%�j<%����$��xNj��R�Z��pX�∔�Qi�I[1(ݍ��.������^��t�~+-!����]ij8:8��6����9G'G�3��;��?,[�ޡ8t�>���^iG�:�nA&R�ZH��b«�-"\L�9vsc��N  ��;�	B?�]�|
^F�����1�Y�O��A<C8���;��Oh�PK4��e�  '  PK  �cvL               PA2$CountersClass.classeO�j�P=��Woc}��j7����
ҥ�%� �X�v[n�D�����Jp��Q�$�����9s����x����s��@�Pw�̤:N�P%	�����D�M�އ;{�O��2
L��	���;��F���x	���o_�K�<��(�W�9�I��qx�V堊���ţ�����2�����^������kl�у��A����d�+8P�ۂ~
���d;�A	]Fcu���Yh�RC���\� PK..��   5  PK  �cvL            "   PA2$CountersClass$N_COUNTERS.class�S�O�P=o-{ݣ��(�M��eJ4~�BF�����8 1~0ݬKIyK���K!����?�x_%�e���}���s�����?�x�R:�����(<Ta������x�a3dڭ��Z령�Qd���cy�����'�&�G�h~H���m��ƛ����p�_f�����A����'a�'n+�5ۙ�k���\×^3>�x��	�.�O�v��=qK�+{�v��Wv&���ҏv�\��sո��t�8$1�V��.YO�HuWn��!E���a�{�+���T��Y&6�!SKxb"�eӘg�5��Ű0ցa��%�.�ΐ�V���F����J�����O#��3:_�c��U���e�JTё�-,��6e봪K��}E��g�h���^��K���J��ҋ�0u}�R������xq3���1pN�D�Q�'�L�+��;�k�0ΐ�R���	8�j$���P+���
��>fi��ɄC�י��f�PK3U�Y�  �  PK  �cvL               PA2$DocIdUniComKey.class�VmWU~n�$��VJm
�V,!@S��R �h��֪U�䞰vs6m������z�9?��<�Y�����ag�Νyf�s7��?��!�I"g�$"�%��]�It�.ĜObK�-�����h�(R�/��VŶf�A�ȉ(Ʊ.Ƽ,��[����G�T訸�¢��U׫f��U���-�⺵��f��س}ks[�(Ǯz֎����O��Y۱��B4=���[�
ݫ��4v6��!�
wӧg=!�`w�LGB]J�>�~�do��bï5v=�K�˺�ۮS7�kO[�e[oW�
o����xU���Ԍ���BN����:.&d(Qvwj��7\�.�Sah��Y�/̂�h/�m����>?=���XA�|ɷ�?�Y��ϸ�|�v�
�, �l�r��]+�^L�7�� %Jvձ��G��Q����rF{���U�f�5��Jn�+�e;�N7��aDa�4�M��a���n��������|m�|+⩉4�LL`��MdML�;�0%��d��Ņ��]ў�`)���M����I�m��V29'ҥ0zF:.�o��?�
�)ɒ�j��2���l��p2d�u��X� �������?����1~�cy/�	�;�ZM;��v51�G���r�_��p�2"S��mD���X:���R�2��2oy��W�Q����
F{�oR�A�B'e1���S��8�'8;�A��C�������&� �D�+c��s����Rݢ�����]J���o�74���P]A-��G���p��8^��H/=�ޮ�� ~�?6����Y����Ci �,�f�o9��u�30���=l�j��.Q򶅮�,]���i�wn�t���5qa.2��2�A�cm@&N��	��Š$�#D�e��y�~�qQ����D!9�x�Z�[�|�3�:?�����Ȍ��TW^���`w�"g�V�s�� $���� ��PK[|�  R  PK  �cvL               PA2$PartitionerInitial.class�R[kA�&�M�mZ��k�M�M".�O�J���P/ϓ͐��̄�D�?��K���(�L,^�B�}�o���������/_<F�G7}q�G	�}��]u�}���!���PڕJ�=�|�|�P��`X�I%^��}a^���,�P�Cn��R+��N��k%�z�L����R�tG|:S�'Q�p�Q�o�NO�a�'<�D��֓X�8Q������$̈́!�z���G\��c���y�µr$��ۙ!����c>1b0KE�K�0���m����I�S�zR�0�� >��D#��=�����b����^�,@WV��	�J�z�q��'�4M�逡5//��f?��6�:_�=���fd�}|!Z�%O�ҢU�<r�E�=�W��[��>҅a�����I���n�@�Ԟ#7G�>��3���}��N)�Dp�j�}����sxg9!i�F��u�X�*Vkt+��Iy�p�PKj.��  T  PK   �cvL           	                META-INF/��  PK   �cvLUz�CD   E                =   META-INF/MANIFEST.MFPK   �cvLc�C"�   '  
             �   Job1.classPK   �cvL˔\��  "
               �  Job1$Job1Mapper.classPK   �cvL����  �               �  Job1$Job1Reducer.classPK   �cvL�.O��   '  
             '
  Job2.classPK   �cvL��%  �               A  Job2$Job2Mapper.classPK   �cvL����  z               �  Job2$Job2Reducer.classPK   �cvL�<�   '  
             n  Job3.classPK   �cvL�͡�                 �  Job3$Job3Mapper.classPK   �cvL7�\�  �               �  Job3$Job3Reducer.classPK   �cvL�#[h�   '  
               Job4.classPK   �cvL�v���  Z                  Job4$Job4Mapper.classPK   �cvLf�4  �               #  Job4$Job4Reducer.classPK   �cvL�}�t  U  
             y&  Job5.classPK   �cvL*b��	  �               %(  Job5$Job5Mapper.classPK   �cvL5Ox!;  �               2  Job5$Job5Reducer.classPK   �cvL��_w  :               �8  PA2$1.classPK   �cvL4��e�  '  	             B:  PA2.classPK   �cvL..��   5               (C  PA2$CountersClass.classPK   �cvL3U�Y�  �  "             WD  PA2$CountersClass$N_COUNTERS.classPK   �cvL[|�  R               �F  PA2$DocIdUniComKey.classPK   �cvLj.��  T               �J  PA2$PartitionerInitial.classPK      �  �L                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            