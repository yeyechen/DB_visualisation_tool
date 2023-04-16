package com.example.demo.visualisation;

import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class VisualService {

  @Autowired
  private JdbcTemplate jdbc;
  private static ERConnectableObj table;
  private static List<Attribute> attributes;
  private static Attribute tablePK;

  // todo: need somehow get the k2 as well (Many-Many relationship)
  public void initialise(Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    table = selectionInfo.keySet().iterator().next();
    attributes = selectionInfo.get(table);
    Optional<Attribute> pk = table instanceof Entity ? ((Entity) table).getAttributeList().stream()
        .filter(Attribute::getIsPrimary).findFirst()
        : ((Relationship) table).getAttributeList().stream().filter(Attribute::getIsPrimary)
            .findFirst();
    pk.ifPresent(attribute -> tablePK = attribute);
  }

  public List<Map<String, Object>> queryBarChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Attribute attribute = attributes.iterator().next();
    String query =
        "SELECT " + tablePK.getName() + ", " + attribute.getName() + " FROM " + table.getName();
    return jdbc.queryForList(query);
  }

  // todo: needs refactor
  // public List<Map<String, Object>> queryScatterDiagram(ERConnectableObj table, String primaryKey,
  //     Attribute attribute1, Attribute attribute2) {
  //   String query =
  //       "SELECT " + primaryKey + ", " + attribute1.getName() + ", " + attribute2.getName()
  //           + " FROM " + table.getName();
  //   return jdbc.queryForList(query);
  // }
}
