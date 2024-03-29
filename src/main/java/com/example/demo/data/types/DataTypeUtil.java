package com.example.demo.data.types;

import com.example.demo.input.handler.InputService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DataTypeUtil {

  public static DataType getDataType(String tableName, String attributeName)
      throws SQLException {
    ResultSet rs = InputService.getMetaData().getColumns(null, null, tableName, attributeName);
    int dataType = 0; // 0 is NULL type in SQL
    if (rs.next()) {
      dataType = rs.getInt("DATA_TYPE");
    }
    DataType result = getDataTypeBasedOnSQLType(dataType);
    assert
        result != DataType.GEOGRAPHICAL || rs.getString("TYPE_NAME").equalsIgnoreCase("geometry");
    return result;
  }

  private static DataType getDataTypeBasedOnSQLType(int SQLType) {
    return switch (SQLType) {
      case Types.INTEGER, Types.DOUBLE, Types.DECIMAL, Types.REAL, Types.FLOAT, Types.SMALLINT, Types.TINYINT, Types.NUMERIC -> DataType.NUMERICAL;
      case Types.DATE, Types.TIMESTAMP -> DataType.TEMPORAL;
      case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.NVARCHAR, Types.LONGNVARCHAR -> DataType.LEXICAL;
      case Types.OTHER -> DataType.GEOGRAPHICAL;
      default -> DataType.OTHER;
    };
  }
}
