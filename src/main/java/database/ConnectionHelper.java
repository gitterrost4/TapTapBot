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
  
  private final String databaseFileName;
  
  public ConnectionHelper(String databaseFileName) {
    this.databaseFileName = databaseFileName;
  }
  
  private Connection getConnection() {
    String url = "jdbc:sqlite:"+databaseFileName;
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return conn;
  }

  public <T> Optional<T> getFirstResult(String preparedSql, FunctionWithThrowable<ResultSet, T, Exception> func,
      Object... params) {
    List<T> results = getResults(preparedSql, func, params);
    if (results.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(results.get(0));
  }

  public <T> List<T> getResults(String preparedSql, FunctionWithThrowable<ResultSet, T, Exception> func,
      Object... params) {
    List<T> result = new ArrayList<>();
    try (Connection connection = getConnection();
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

  public void update(String preparedSQL, Object... params) {
    try (Connection connection = getConnection();
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
