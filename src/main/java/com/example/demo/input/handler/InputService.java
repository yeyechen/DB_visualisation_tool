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
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

@Getter
@Service
public class InputService {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Value("${spring.datasource.username}")
  private String dbUsername;

  @Value("${spring.datasource.password}")
  private String dbPassword;


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

    dbUrl = DatabaseUtil.generateDatabaseURL(dbType, host, port, databaseName);
    dbUsername = username;
    dbPassword = password;

    // create datasource
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(dbUrl);
    dataSource.setUsername(dbUsername);
    dataSource.setPassword(dbPassword);
    jdbc = new JdbcTemplate(dataSource);
  }

  public void patternMatchBasedOnSelection(Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    modelType = ModelUtil.patternMatching(selectionInfo.keySet().iterator().next(), schema);
    InputService.selectionInfo = selectionInfo;
  }
}
