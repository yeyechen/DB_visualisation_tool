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

  static JdbcTemplate mondialJdbc;
  static JdbcTemplate phoneJdbc;

  @BeforeClass
  public static void init() throws Exception {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl("jdbc:postgresql://localhost:5432/sub_mondial");
    dataSource.setUsername("Mikeee");
    dataSource.setPassword("");
    mondialJdbc = new JdbcTemplate(dataSource);

    DriverManagerDataSource dataSource2 = new DriverManagerDataSource();
    dataSource2.setUrl("jdbc:postgresql://localhost:5432/phones");
    dataSource2.setUsername("Mikeee");
    dataSource2.setPassword("");
    phoneJdbc = new JdbcTemplate(dataSource2);
  }

  @Test
  public void integerNumericalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("country", "population", mondialJdbc);
    assertEquals(type, DataType.NUMERICAL);
  }

  @Test
  public void numericNumericalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("country", "area", mondialJdbc);
    assertEquals(type, DataType.NUMERICAL);
  }

  @Test
  public void varcharLexicalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("country", "name", mondialJdbc);
    assertEquals(type, DataType.LEXICAL);
  }

  @Test
  public void dataTemporalTypeTest() throws SQLException {
    DataType type = DataTypeUtil.getDataType("sales", "date", phoneJdbc);
    assertEquals(type, DataType.TEMPORAL);
  }
}
