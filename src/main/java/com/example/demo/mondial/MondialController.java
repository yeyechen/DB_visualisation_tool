package com.example.demo.mondial;

import com.example.demo.visualisation.VisualService;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ER;
import io.github.MigadaTang.ERBaseObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.RDBMSType;
import io.github.MigadaTang.exception.DBConnectionException;
import io.github.MigadaTang.exception.ParseException;
import io.github.MigadaTang.transform.Reverse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mondial")
public class MondialController {

  @Autowired
  private JdbcTemplate jdbc;
  @Autowired
  private final MondialService service;
  @Autowired
  private final VisualService visualService;

  public MondialController(MondialService service, VisualService visualService) {
    this.service = service;
    this.visualService = visualService;
  }

  @GetMapping("/metadata")
  public MetaData index() {
    try {
      Connection connection = jdbc.getDataSource().getConnection();
      return service.getMetaData(connection, MondialService.TABLE_NAME);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return new MetaData();
  }

  @GetMapping("/economy")
  public List<Map<String, Object>> getEcoData() {
    String query = "SELECT inflation, unemployment FROM economy";
    return jdbc.queryForList(query);
  }

  @GetMapping("/economy2")
  public List<Map<String, Object>> getEcoData2() {
    // join tables "country", "encompasses" and "economy" on
    // "country.code=encompasses.country=economy.country"
    String query = "SELECT code, continent, gdp, inflation, unemployment FROM country " +
        "INNER JOIN encompasses ON country.code = encompasses.country " +
        "INNER JOIN economy ON economy.country = country.code";
    return jdbc.queryForList(query);
  }

  // example of pattern matching of the very basic entity visualisation.
  @GetMapping("/main")
  public List<Map<String, Object>> simpleFlowExample()
      throws SQLException, ParseException, DBConnectionException {

    /*-------- User input --------*/

    // database info
    RDBMSType databaseType = RDBMSType.POSTGRESQL;
    String hostname = "localhost";
    String portNum = "5432";
    String databaseName = "mondial";
    String userName = "Mikeee";
    String password = "";

    System.out.println("Start processing...");
    /*-------- reverse engineering to ER schema --------*/

    ER.initialize();

    // todo: current version of "Amazing ER" does not support Mondial database, needs refactor/improve
    // Reverse reverse = new Reverse();
    // Schema schema = reverse.relationSchemasToERModel(databaseType, hostname, portNum,
    //     databaseName, userName, password);

    // feed a smaller (subset of the Mondial), but viable example database
    Reverse reverse = new Reverse();
    Schema schema = reverse.relationSchemasToERModel(databaseType, hostname, portNum,
        "sub_mondial", userName, password);

    // display entities
    for (Entity entity : schema.getEntityList()) {
      System.out.println("\ntable: \n\t" + entity.getName());
      System.out.println("attributes: ");
      for (Attribute attribute : entity.getAttributeList()) {
        System.out.println("\t" + attribute.getName() + " id: " + attribute.getID());
      }
    }

    // user selection
    Scanner scanner = new Scanner(System.in);
    System.out.println("Please enter the first table id you want to visualise: ");
    Entity selectedEntity = Entity.queryByID(scanner.nextLong());
    System.out.println("selected table: " + selectedEntity.getName());

    System.out.println(
        "Please enter the second table id you want to visualise (0 if only single table): ");
    long secondId = scanner.nextLong();
    Entity selectedEntity2 = secondId == 0 ? null : Entity.queryByID(secondId);
    String secondTableName = secondId == 0 ? "none" : selectedEntity2.getName();
    System.out.println("selected table: " + secondTableName);

    List<Attribute> selectedAttributesList = new ArrayList<>();
    System.out.println("Please enter attributes id you want to visualise (0 to stop): ");

    while (true) {
      long input = scanner.nextLong();

      if (input == 0) {
        break;
      }
      Attribute att = Attribute.queryByID(input);
      System.out.println("attribute " + att.getName() + " added");
      selectedAttributesList.add(att);
    }
    System.out.println("List of attributes: ");
    selectedAttributesList.stream().map(
        ERBaseObj::getName).forEach(System.out::println);


    /*-------- pattern matching (not quite)--------*/

    // note: one working example is country -> population, area
    Attribute selectedAttr1 = selectedAttributesList.get(0);
    Attribute selectedAttr2 = selectedAttributesList.get(1);


    // API does not provide function to get primary key, hardcode
    // todo: support acquire of primary key of an entity
    String primaryKey = "code";

    /*-------- generate visualisation --------*/

    System.out.println("Finished");

    List<Map<String, Object>> result = null;

    // user chose Bar chart
    // result = visualService.queryBarChart(selectedEntity, selectedAttr1);
    // user chose Scatter diagram
    // result = visualService.queryScatterDiagram(selectedEntity, primaryKey, selectedAttr1,
    //     selectedAttr2);

    return result;
  }
}
