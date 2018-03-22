/**
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
