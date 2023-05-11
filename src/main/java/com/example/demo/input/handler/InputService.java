package com.example.demo.input.handler;

import com.example.demo.models.ModelType;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ER;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.RDBMSType;
import io.github.MigadaTang.exception.DBConnectionException;
import io.github.MigadaTang.exception.ParseException;
import io.github.MigadaTang.transform.DatabaseUtil;
import io.github.MigadaTang.transform.Reverse;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

@Getter
@Service
public class InputService {

  private ModelType modelType;

  private static Schema schema;

  private static Map<ERConnectableObj, List<Attribute>> selectionInfo;
  private static JdbcTemplate jdbc;

  public static Schema getSchema() {
    return schema;
  }

  public static Map<ERConnectableObj, List<Attribute>> getSelectionInfo() {
    return selectionInfo;
  }

  public static JdbcTemplate getJdbc() {
    return jdbc;
  }

  public void initialise(Map<String, String> formData)
      throws SQLException, ParseException, DBConnectionException {
    ER.initialize();
    String dbTypeString = formData.get("dbType");
    RDBMSType dbTypeEnum = switch (dbTypeString) {
      case "postgresql" -> RDBMSType.POSTGRESQL;
      case "mysql" -> RDBMSType.MYSQL;
      case "oracle" -> RDBMSType.ORACLE;
      case "sqlserver" -> RDBMSType.SQLSERVER;
      case "db2" -> RDBMSType.DB2;
      case "h2" -> RDBMSType.H2;
      default -> null;
    };
    String host = formData.get("host");
    String port = formData.get("port");
    String databaseName = formData.get("databaseName");
    String username = formData.get("username");
    String password = formData.get("password");

    Reverse reverse = new Reverse();
    schema = reverse.relationSchemasToERModel(dbTypeEnum, host, port, databaseName, username,
        password);
    updateDatabaseDetails(dbTypeEnum, host, port, databaseName, username, password);
  }

  private void updateDatabaseDetails(RDBMSType dbType, String host, String port, String databaseName, String username, String password)
      throws ParseException {

    String dbUrl = DatabaseUtil.generateDatabaseURL(dbType, host, port, databaseName);

    // create datasource
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(dbUrl);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    jdbc = new JdbcTemplate(dataSource);
  }

  public void patternMatchBasedOnSelection(Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    // user can select attributes from at most two tables (or select none)
    if (selectionInfo.keySet().size() == 1) {
      modelType = ModelUtil.patternMatch(selectionInfo.keySet().iterator().next(), schema);
    } else {
      Iterator<ERConnectableObj> iterator = selectionInfo.keySet().iterator();
      modelType = ModelUtil.patternMatch(iterator.next(), iterator.next(), schema);
    }
    InputService.selectionInfo = selectionInfo;
  }

  public static boolean checkSelectNone() {
    if (selectionInfo.keySet().size() == 1) {
      List<Attribute> attributes = selectionInfo.values().iterator().next();
      return attributes.isEmpty();
    }
    return false;
  }
}
