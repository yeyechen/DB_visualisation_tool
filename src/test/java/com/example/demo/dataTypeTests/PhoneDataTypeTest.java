package com.example.demo.dataTypeTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.data.types.DataType;
import com.example.demo.data.types.DataTypeUtil;
import com.example.demo.input.handler.InputService;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class PhoneDataTypeTest {

  static InputService inputService;

  @BeforeClass
  public static void init() throws Exception {
    Map<String, String> formData = new HashMap<>();
    formData.put("dbType", "postgresql");
    formData.put("host", "localhost");
    formData.put("port", "5432");
    formData.put("databaseName", "phones");
    formData.put("username", "Mikeee");
    formData.put("password", "");

    inputService = new InputService();
    inputService.initialise(formData);
  }

  @Test
  public void dateTemporalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("sales", "date");
    assertEquals(type, DataType.TEMPORAL);
  }

}
