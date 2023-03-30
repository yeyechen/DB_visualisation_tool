package com.example.demo.mondial;

import com.example.demo.visualisation.VisualService;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ER;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.RDBMSType;
import io.github.MigadaTang.exception.DBConnectionException;
import io.github.MigadaTang.exception.ParseException;
import io.github.MigadaTang.transform.Reverse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
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

    System.out.println("Start processing...");

    /*-------- User input --------*/

    // database info
    RDBMSType databaseType = RDBMSType.POSTGRESQL;
    String hostname = "localhost";
    String portNum = "5432";
    String databaseName = "mondial";
    String userName = "Mikeee";
    String password = "";

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

    // simulate user selection
    String selectedTable = "country";
    String attr1 = "population";
    String attr2 = "area";

    /*-------- pattern matching (not quite)--------*/

    Entity selectedEntity = null;
    for (Entity entity : schema.getEntityList()) {
      // sanity check
      if (entity.getName().equals(selectedTable)) {
        selectedEntity = entity;
        break;
      }
    }
    assert selectedEntity != null;

    Attribute selectedAttr1 = null;
    Attribute selectedAttr2 = null;
    for (Attribute attribute : selectedEntity.getAttributeList()) {
      // sanity check
      if (attribute.getName().equals(attr1)) {
        selectedAttr1 = attribute;
      }
      if (attribute.getName().equals(attr2)) {
        selectedAttr2 = attribute;
      }
    }
    assert selectedAttr1 != null;
    assert selectedAttr2 != null;

    // API does not provide function to get primary key, hardcode
    // todo: support acquire of primary key of an entity
    String primaryKey = "code";

    /*-------- generate visualisation --------*/

    System.out.println("Finished");

    List<Map<String, Object>> result;

    // user chose Bar chart
    // result = visualService.queryBarChart(selectedEntity, primaryKey, selectedAttr1);

    // user chose Scatter diagram
    result = visualService.queryScatterDiagram(selectedEntity, primaryKey, selectedAttr1,
        selectedAttr2);

    return result;
  }

}
