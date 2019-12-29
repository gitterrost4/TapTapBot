package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import helpers.Catcher;
import helpers.FunctionWithThrowable;

public class ConnectionHelper {
  public static Connection getConnection() {
    String url = "jdbc:sqlite:taptapbot.db";
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return conn;
  }

  public static <T> Optional<T> getFirstResult(String preparedSql, FunctionWithThrowable<ResultSet, T, Exception> func,
      Object... params) {
    List<T> results = getResults(preparedSql, func, params);
    if (results.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(results.get(0));
  }

  public static <T> List<T> getResults(String preparedSql, FunctionWithThrowable<ResultSet, T, Exception> func,
      Object... params) {
    List<T> result = new ArrayList<>();
    try (Connection connection = ConnectionHelper.getConnection();
        PreparedStatement stmt = connection.prepareStatement(preparedSql);) {
      setParams(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          result.add(Catcher.wrap(() -> func.apply(rs)));
        }
      }
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
    return result;
  }

  public static void update(String preparedSQL, Object... params) {
    try (Connection connection = ConnectionHelper.getConnection();
        PreparedStatement stmt = connection.prepareStatement(preparedSQL);) {
      setParams(stmt, params);
      stmt.executeUpdate();
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
  }

  private static void setParams(PreparedStatement stmt, Object... params) throws SQLException {
    int i = 0;
    for (Object param : params) {
      stmt.setObject(++i, param);
    }
  }
}
