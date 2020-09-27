package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helpers.Catcher;
import helpers.FunctionWithThrowable;

public class ConnectionHelper {

  private static final Logger logger = LoggerFactory.getLogger(ConnectionHelper.class);

  private final String databaseFileName;

  public ConnectionHelper(String databaseFileName) {
    this.databaseFileName = databaseFileName;
  }

  private Connection getConnection() {
    String url = "jdbc:sqlite:" + databaseFileName;
    try {
      return DriverManager.getConnection(url);
    } catch (SQLException e) {
      throw new InternalError(e);
    }
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
    try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(preparedSql);) {
      setParams(stmt, params);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          result.add(Catcher.wrap(() -> func.apply(rs)));
        }
      }
    } catch (SQLException e) {
      logger.error("Problem in sql statement:", e);
    }
    return result;
  }

  public void update(String preparedSQL, Object... params) {
    try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(preparedSQL);) {
      setParams(stmt, params);
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Problem in sql statement:", e);
    }
  }

  private static void setParams(PreparedStatement stmt, Object... params) throws SQLException {
    int i = 0;
    for (Object param : params) {
      stmt.setObject(++i, param);
    }
  }
}
