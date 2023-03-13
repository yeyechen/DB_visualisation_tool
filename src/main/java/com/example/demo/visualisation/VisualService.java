package com.example.demo.visualisation;

import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.Entity;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class VisualService {

  @Autowired
  private JdbcTemplate jdbc;

  // todo: "primaryKey" also need to be of type Attribute
  public List<Map<String, Object>> queryBarChart(Entity entityName, String primaryKey, Attribute attribute) {
    String query = "SELECT " + primaryKey + ", " + attribute.getName() + " FROM " + entityName.getName();
    return jdbc.queryForList(query);
  }

}
