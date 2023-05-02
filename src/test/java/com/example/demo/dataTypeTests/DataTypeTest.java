package com.example.demo.dataTypeTests;

import static org.junit.Assert.assertEquals;

import com.example.demo.data.types.DataType;
import com.example.demo.data.types.DataTypeUtil;
import java.sql.SQLException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DataTypeTest {

  static JdbcTemplate jdbc;

  @BeforeClass
  public static void init() throws Exception {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl("jdbc:postgresql://localhost:5432/sub_mondial");
    dataSource.setUsername("Mikeee");
    dataSource.setPassword("");
    jdbc = new JdbcTemplate(dataSource);
  }

  @Test
  public void integerNumericalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("country", "population", jdbc);
    assertEquals(type, DataType.NUMERICAL);
  }

  @Test
  public void numericNumericalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("country", "area", jdbc);
    assertEquals(type, DataType.NUMERICAL);
  }

  @Test
  public void varcharLexicalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("country", "name", jdbc);
    assertEquals(type, DataType.LEXICAL);
  }

}
