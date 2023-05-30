package com.example.demo.dataTypeTests;

import static org.junit.Assert.assertEquals;

import com.example.demo.data.types.DataType;
import com.example.demo.data.types.DataTypeUtil;
import com.example.demo.input.handler.InputService;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class MondialDataTypeTest {

  static InputService inputService;

  @BeforeClass
  public static void init() throws Exception {
    Map<String, String> formData = new HashMap<>();
    formData.put("dbType", "postgresql");
    formData.put("host", "localhost");
    formData.put("port", "5432");
    formData.put("databaseName", "sub_mondial");
    formData.put("username", "Mikeee");
    formData.put("password", "");

    inputService = new InputService();
    inputService.initialise(formData);
  }

  @Test
  public void integerNumericalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("country", "population");
    assertEquals(type, DataType.NUMERICAL);
  }

  @Test
  public void numericNumericalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("country", "area");
    assertEquals(type, DataType.NUMERICAL);
  }

  @Test
  public void varcharLexicalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("country", "name");
    assertEquals(type, DataType.LEXICAL);
  }
}
