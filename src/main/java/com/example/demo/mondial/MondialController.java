package com.example.demo.mondial;

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

  public MondialController(MondialService service) {
    this.service = service;
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
}
