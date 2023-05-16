package com.example.demo.visualServiceTests;

import com.example.demo.TestObject;
import com.example.demo.input.handler.InputService;
import com.example.demo.visualisation.VisualService;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERConnectableObj;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class GenerateSQLQueryTest {

  static InputService inputService;
  static VisualService visualService;
  static TestObject testObject;

  @BeforeClass
  public static void init() throws Exception {
    Map<String, String> formData = new HashMap<>();
    formData.put("dbType", "postgresql");
    formData.put("host", "localhost");
    formData.put("port", "5432");
    formData.put("databaseName", "sub_mondial");
    formData.put("username", "Mikeee");
    formData.put("password", "");
    testObject = new TestObject();
    testObject.setup();

    Map<ERConnectableObj, List<Attribute>> selectionInfo = new HashMap<>();
    Map<String, List<String>> filterConditions = new HashMap<>();
    selectionInfo.put(testObject.getCountry(), testObject.getCountry().getAttributeList());
    filterConditions.put("airport.iata_code", List.of("HEA", "KBL"));
    inputService = new InputService();
    inputService.initialise(formData);
    inputService.patternMatchBasedOnSelection(selectionInfo);
    inputService.setFilterCondisions(filterConditions);
    visualService = new VisualService();
    visualService.initialise(InputService.getSelectionInfo(), InputService.getFilterConditions());
  }

  @Test
  public void GenerateSQLQueryTest() throws SQLException {
    System.out.println(visualService.generateSQLQuery(List.of("population"), "country", "code"));
  }

}
