package com.rm.datasources;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 *
 * @author rmarquez
 */
@XmlRootElement(name = "histdlr")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DbConnection implements Serializable {

  private final String user;
  private final String password;
  private final String databaseName;
  private final String url;
  private final Integer port;
  private Connection connection;
  private final Converters converters;

  public DbConnection(String user, String password, String databaseName, String url, Integer port) {
    this.user = user;
    this.password = password;
    this.databaseName = databaseName;
    this.url = url;
    this.port = port;
    this.converters = new Converters();
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public String getUrl() {
    return url;
  }

  public Integer getPort() {
    return port;
  }

  /**
   *
   * @param table
   * @param record
   * @return
   */
  public int executeInsert(String table, RecordValue record) {
    Set<String> keySet = record.keySetNoPk();
    List<Object> values = new ArrayList<>(record.valueSetNoPk());
    String separator = ", ";
    String columns = String.join(separator, keySet);
    String valuePlaceHolders = StringUtils.repeat("?", separator, values.size());
    String sql = String.format("insert into %s (%s) values (%s)",
      new Object[]{table, columns, valuePlaceHolders});
    Connection conn = this.getConnection();
    int effectedRows;
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      this.setParamValues(values, statement);
      effectedRows = statement.executeUpdate();
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        conn.close();
      } catch (SQLException ex) {
        Logger.getLogger(DbConnection.class.getName())
          .log(Level.SEVERE, "An error occurred while closing the connection. ", ex);
      }
    }
    return effectedRows;
  }

  /**
   *
   * @param sql
   * @param consumer
   */
  public void executeQuery(String sql, Consumer<ResultSet> consumer) {
    Connection conn = this.getConnection();
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        consumer.accept(resultSet);
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        conn.close();
      } catch (SQLException ex) {
        Logger.getLogger(DbConnection.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   *
   * @return
   */
  @Override
  public String toString() {
    return this.getConnectionUrl();
  }

  /**
   * Returns a new connection.
   *
   * @return
   */
  public Connection getConnection() {
    Connection result;
    try {
      String _url = this.getConnectionUrl();
      String _username = this.user;
      String _password = this.password;
      result = DriverManager.getConnection(_url, _username, _password);
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public String getConnectionUrl() {
    String _url = "jdbc:postgresql://" + this.getUrl()
      + ":" + this.port
      + "/" + this.databaseName;
    return _url;
  }

  /**
   *
   * @return
   */
  public Exception test() {
    Exception result;
    try {
      this.connection = this.getConnection();
      this.connection.close();
      this.connection = null;
      result = null;
    } catch (SQLException ex) {
      result = ex;
    } catch (Exception ex) {
      result = ex;
    }
    return result;
  }

  /**
   *
   * @param text
   * @return
   */
  public Exception tableExists(String text) {
    MutableObject<Exception> result = new MutableObject<>(null);
    String schema;
    String table_name;
    if (!text.contains(".")) {
      schema = "public";
      table_name = text;
    } else {
      String[] parts = text.split("\\.");
      schema = parts[0];
      table_name = parts[1];
    }
    String query = "SELECT EXISTS (\n"
      + "   SELECT 1\n"
      + "   FROM   information_schema.tables \n"
      + "   WHERE  table_schema = '" + schema + "'\n"
      + "   AND    table_name = '" + table_name + "'\n"
      + "   );";
    this.executeQuery(query, (r) -> {
      try {
        boolean tableExists = r.getBoolean(1);
        if (!tableExists) {
          result.setValue(new Exception("Table '" + text + "' does not exist."));
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    });
    return result.getValue();
  }

  /**
   *
   * @param table
   * @param record
   * @param pk
   * @return
   */
  public int executeUpsert(String table, RecordValue record, String... pk) {
    Objects.requireNonNull(pk, "Primary key field name cannot be null");
    Objects.requireNonNull(record, "Record cannot be null");
    Objects.requireNonNull(table, "table cannot be null");
    if (pk.length == 0) {
      throw new IllegalArgumentException("No primary key field names specified cannot be empty");
    }
    if (table.trim().isEmpty()) {
      throw new IllegalArgumentException("table string cannot be empty");
    }
    Set<String> keySet = record.keySetNoPk();
    List<Object> values = new ArrayList<>(record.valueSetNoPk());
    String separator = ", ";
    String columns = String.join(separator, keySet);
    String valuePlaceHolders = StringUtils.repeat("?", separator, values.size());
    HashSet<String> keySetNoPk = new HashSet<>(keySet);
    keySetNoPk.removeIf((k) -> Arrays.asList(pk).contains(k));
    String columns_no_pk = String.join(separator, keySetNoPk);
    List<Object> values_no_pk = record.valueSet(keySetNoPk);
    String valuePlaceHoldersNoPk = StringUtils.repeat("?", separator, values_no_pk.size());
    String sql = String.format("insert into %s \n(%s) \n values (%s) \n"
      + " on conflict (%s) do update\n "
      + " set (%s) = (%s) ",
      new Object[]{table, 
        columns, 
        valuePlaceHolders, 
        String.join(",", pk), 
        columns_no_pk, 
        valuePlaceHoldersNoPk
      }
    );
    Connection conn = this.getConnection();
    int effectedRows;
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      List<Object> allValues = new ArrayList<>();
      allValues.addAll(values);
      allValues.addAll(values_no_pk);
      this.setParamValues(allValues, statement);
      effectedRows = statement.executeUpdate();
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        conn.close();
      } catch (SQLException ex) {
        Logger.getLogger(DbConnection.class.getName())
          .log(Level.SEVERE, "An error occurred while closing the connection. ", ex);
      }
    }
    return effectedRows;
  }

  /**
   *
   * @param table
   * @param records
   * @param pk
   * @return
   */
  public int executeUpsert(String table, List<RecordValue> records, String... pk) {
    Objects.requireNonNull(pk, "Primary key field name cannot be null");
    Objects.requireNonNull(records, "Records cannot be null");
    Objects.requireNonNull(table, "table cannot be null");
    if (pk.length == 0) {
      throw new IllegalArgumentException("No primary key field names specified cannot be empty");
    }
    if (table.trim().isEmpty()) {
      throw new IllegalArgumentException("table string cannot be empty");
    }
    int effectedRows;
    if (!records.isEmpty()) {
      Set<String> keySet = records.get(0).keySet();
      String separator = ", ";
      String columns = String.join(separator, keySet);
      String valuePlaceHolders = records.stream()
        .map(
          (r) -> keySet
            .stream()
            .map((k) -> this.converters.convert(r.get(k)))
            .collect(Collectors.joining(","))
        ).collect(Collectors.joining(")\n" + separator + "("));
      Set<String> keySetNoPk = records.get(0).keySetNoPk()
        .stream()
        .map((e) -> "excluded." + e)
        .collect(Collectors.toSet());
      
      
      String columns_no_pk = String.join(separator, keySetNoPk);
      String sql = String.format("insert into %s \n(%s) \n values (%s) \n"
        + " on conflict (%s) do update\n "
        + " set %s = %s ",
        new Object[]{
          table,
          columns,
          valuePlaceHolders,
          String.join(",", pk),
          String.join(",", records.get(0).keySetNoPk()),
          columns_no_pk
        });
      Connection conn = this.getConnection();
      try {
        PreparedStatement statement = conn.prepareStatement(sql);
        effectedRows = statement.executeUpdate();
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      } finally {
        try {
          conn.close();
        } catch (SQLException ex) {
          Logger.getLogger(DbConnection.class.getName())
            .log(Level.SEVERE, "An error occurred while closing the connection. ", ex);
        }
      }
    } else {
      effectedRows = 0;
    }
    return effectedRows;
  }

  /**
   *
   * @param values
   * @param statement
   * @throws SQLException
   */
  private void setParamValues(List<Object> values, PreparedStatement statement) throws SQLException {
    for (int columnIndex = 1; columnIndex <= values.size(); columnIndex++) {
      Object objectValue = values.get(columnIndex - 1);
      if (objectValue instanceof ZonedDateTime) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(((ZonedDateTime) objectValue).getZone()));
        ZonedDateTime withZoneSameInstant = ((ZonedDateTime) objectValue);
        long epochMilli = withZoneSameInstant.toInstant()
          .toEpochMilli();
        Timestamp p = new Timestamp(epochMilli);
        statement.setTimestamp(columnIndex, p, cal);

      } else {
        statement.setObject(columnIndex, objectValue);
      }
    }
  }

  /**
   * *
   *
   * @return
   */
  public Properties properties() {
    Properties result = new Properties();
    result.put("url", this.url);
    result.put("databaseName", this.databaseName);
    result.put("password", this.password);
    result.put("port", this.port);
    result.put("user", this.user);
    return result;
  }

  /**
   *
   * @param p
   * @return
   */
  public static DbConnection fromProperties(Properties p) {
    return new DbConnection.Builder()
      .setUrl(p.getProperty("url"))
      .setDatabaseName(p.getProperty("databaseName"))
      .setPassword(p.getProperty("password"))
      .setPort((Integer) p.get("port"))
      .setUser(p.getProperty("user"))
      .createDbConnection();
  }

  /**
   *
   */
  public static class Builder {

    private String user;
    private String password;
    private String databaseName;
    private String url;
    private Integer port;

    public Builder() {
    }

    public Builder setUser(String user) {
      this.user = user;
      return this;
    }

    public Builder setPassword(String password) {
      this.password = password;
      return this;
    }

    public Builder setDatabaseName(String databaseName) {
      this.databaseName = databaseName;
      return this;
    }

    public Builder setUrl(String url) {
      this.url = url;
      return this;
    }

    public Builder setPort(Integer port) {
      this.port = port;
      return this;
    }

    public DbConnection createDbConnection() {
      Objects.requireNonNull(this.user, "user cannot be null");
      Objects.requireNonNull(this.password, "password cannot be null");
      Objects.requireNonNull(this.databaseName, "database cannot be null");
      Objects.requireNonNull(this.url, "url cannot be null");
      Objects.requireNonNull(this.port, "port cannot be null");
      return new DbConnection(this.user, this.password, this.databaseName, this.url, this.port);
    }

  }

}
