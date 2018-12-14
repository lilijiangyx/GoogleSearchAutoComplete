import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

/**
 * Created by jianl018 on 12/3/18.
 */
public class LanguageModel {
  public static class Map extends Mapper<LongWritable, Text, Text, Text>{
    int threshold;
    @Override
    public void setup(Context context){
      Configuration conf = context.getConfiguration();
      threshold = conf.getInt("threshold", 20);
    }

    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
      if((value == null) || (value.toString().trim().isEmpty())){
        return;
      }
      //this is cool\t20
      String line = value.toString().trim();

      String[] wordsPlusCount = line.split("\t");
      if(wordsPlusCount.length < 2){
        return;
      }

      String[] words = wordsPlusCount[0].split("\\s+");
      int count = Integer.valueOf(wordsPlusCount[1]);

      if(count < threshold){
        return;
      }

      //this is --> cool = 20
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < words.length - 1; i++){
        sb.append(words[i]).append(" ");
      }
      String outputKey = sb.toString().trim();
      String outputValue = words[words.length - 1];

      if(!(outputKey == null || outputKey.length() < 1)){
        context.write(new Text(outputKey), new Text(outputValue + "=" + count));
      }
    }
  }


  public static class Reduce extends Reducer<Text, Text, DBOutputWritable, NullWritable>{
    int n;

    @Override
    public void setup(Context context){
      Configuration conf = context.getConfiguration();
      n = conf.getInt("n", 5);
    }

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

      //this is, <girl = 50, boy = 60>
      TreeMap<Integer, List<String>> tm = new TreeMap<>(Collections.reverseOrder());
      for(Text val : values){
        String curValue = val.toString().trim();
        String word = curValue.split("=")[0].trim();
        int count = Integer.parseInt(curValue.split("=")[1].trim());
        if(tm.containsKey(count)){
          tm.get(count).add(word);
        } else {
          List<String> list = new ArrayList<>();
          list.add(word);
          tm.put(count, list);
        }
      }

      //<50, <girl, bird>> <60, <boy...>>
      Iterator<Integer> iter = tm.keySet().iterator();
      for(int j = 0; iter.hasNext() && j < n; j++){
        int keyCount = iter.next();
        List<String> words = tm.get(keyCount);
        for(String curWord : words){
          context.write(new DBOutputWritable(key.toString(), curWord, keyCount), NullWritable.get());
          j++;
        }
      }

    }
  }


}
