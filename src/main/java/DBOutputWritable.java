import org.apache.hadoop.mapreduce.lib.db.DBWritable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jianl018 on 12/3/18.
 */
public class DBOutputWritable implements DBWritable {

  private String starting_phrase;
  private String following_word;
  private int count;

  public DBOutputWritable(String starting_phrase, String following_word, int count){
    this.starting_phrase = starting_phrase;
    this.following_word = following_word;
    this.count = count;
  }

  @Override
  public void readFields(ResultSet var1) throws SQLException {
    this.starting_phrase = var1.getString(1);
    this.following_word = var1.getString(2);
    this.count = var1.getInt(3);
  }

  @Override
  public void write(PreparedStatement var1) throws SQLException {
    var1.setString(1, starting_phrase);
    var1.setString(2, following_word);
    var1.setInt(3,count);
  }



}
