package containers;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import config.Config;
import database.ConnectionHelper;
import helpers.FunctionWithThrowable;
import net.dv8tion.jda.api.entities.Guild;

public class WatchMessage {
  public final Integer id;
  public final String userId;
  public final String channelId;
  public final String messageId;
  public final String inserterId;
  public final Instant tmstmp;
  public final String message;
  public final boolean confirmed;

  private final static FunctionWithThrowable<ResultSet, WatchMessage, Exception> RSFUNC = rs -> new WatchMessage(
      rs.getInt("id"), rs.getString("userid"), rs.getString("channelid"), rs.getString("messageid"),rs.getString("inserterid"),
      Instant.parse(rs.getString("tmstmp")), rs.getString("message"), rs.getInt("confirmed") == 1);

  public WatchMessage(String userId, String channelId, String messageId, String inserterId, Instant tmstmp, String message) {
    this.id = null;
    this.userId = userId;
    this.channelId = channelId;
    this.messageId = messageId;
    this.inserterId = inserterId;
    this.message = message;
    this.confirmed = false;
    this.tmstmp = tmstmp;
  }

  private WatchMessage(Integer id, String userId, String channelId, String messageId, String inserterId, Instant tmstmp, String message,
      boolean confirmed) {
    this.id = id;
    this.userId = userId;
    this.channelId = channelId;
    this.messageId = messageId;
    this.inserterId = inserterId;
    this.message = message;
    this.confirmed = confirmed;
    this.tmstmp = tmstmp;
  }

  public static List<WatchMessage> findWatchMessageByUserId(String userId, boolean onlyConfirmed) {
    String sql = "select * from watchmessage where userId=? " + (onlyConfirmed ? "and confirmed=1" : "");
    return ConnectionHelper.getResults(sql, RSFUNC, userId);
  }

  public static Optional<WatchMessage> findWatchMessageByChannelIdAndMessageId(String channelId, String messageId) {
    String sql = "select * from watchmessage where channelid=? and messageid=?";
    return ConnectionHelper.getFirstResult(sql, RSFUNC, channelId, messageId);
  }

  public static Optional<WatchMessage> findWatchMessageById(Integer id) {
    String sql = "Select * from watchmessage where id=?";
    return ConnectionHelper.getFirstResult(sql, RSFUNC, id);
  }
  
  public static List<WatchMessage> all(){
    String sql = "select * from watchmessage";
    return ConnectionHelper.getResults(sql,RSFUNC);
  }

  public WatchMessage persist() {
    if (id == null) {
      String sql = "INSERT INTO watchmessage (userid, channelid, messageid, tmstmp, message, inserterId, confirmed) values (?,?,?,?,?,?,?)";
      ConnectionHelper.update(sql, userId, channelId, messageId, tmstmp, message, inserterId,confirmed);
      String lastIdSql = "select max(id) as id from watchmessage";
      Integer lastId = ConnectionHelper.getFirstResult(lastIdSql, rs -> rs.getInt("id")).get();
      return findWatchMessageById(lastId).get();
    } else {
      String sql = "UPDATE watchmessage set userid=?, channelid=?, messageid=?, tmstmp=?, message=?, inserterId=?, confirmed=? where id=?";
      ConnectionHelper.update(sql, userId, channelId, messageId, tmstmp, message, inserterId, confirmed, id);
      return findWatchMessageById(id).get();
    }
  }

  public WatchMessage confirm() {
    return new WatchMessage(id, userId, channelId, messageId, inserterId, tmstmp, message, true).persist();
  }

  public void delete() {
    if (id != null) {
      String sql = "DELETE from watchmessage where id=?";
      ConnectionHelper.update(sql, id);
    }
  }

  public String getPrintableString(Guild guild) {
    return "Message from " + tmstmp + " added by *"+guild.getMemberById(inserterId).getEffectiveName()+"*:\nhttps://discordapp.com/channels/"+Config.get("bot.serverId")+"/"+channelId+"/"+messageId+"\n>>> " + message;
  }
}
