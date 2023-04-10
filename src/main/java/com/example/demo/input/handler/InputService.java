package com.example.demo.input.handler;

import io.github.MigadaTang.ER;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.RDBMSType;
import io.github.MigadaTang.exception.DBConnectionException;
import io.github.MigadaTang.exception.ParseException;
import io.github.MigadaTang.transform.Reverse;
import java.sql.SQLException;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class InputService {

  private Schema schema;

  public void initialiseSchema(Map<String, String> formData)
      throws DBConnectionException, ParseException, SQLException {
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
  }

}
