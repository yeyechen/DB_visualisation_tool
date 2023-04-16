package com.example.demo.input.handler;

import com.example.demo.models.ModelType;
import com.example.demo.models.PatternMatch;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ER;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.RDBMSType;
import io.github.MigadaTang.exception.DBConnectionException;
import io.github.MigadaTang.exception.ParseException;
import io.github.MigadaTang.transform.Reverse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class InputService {

  private Schema schema;
  private ModelType modelType;
  private static Map<ERConnectableObj, List<Attribute>> selectionInfo;

  public static Map<ERConnectableObj, List<Attribute>> getSelectionInfo() {
    return selectionInfo;
  }

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

  public void patternMatchBasedOnSelection(Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    modelType = PatternMatch.patternMatching(selectionInfo.keySet().iterator().next(), schema);
    InputService.selectionInfo = selectionInfo;
  }
}
