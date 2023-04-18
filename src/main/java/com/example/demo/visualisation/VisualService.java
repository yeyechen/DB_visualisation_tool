package com.example.demo.visualisation;

import com.example.demo.input.handler.InputService;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VisualService {

  private ERConnectableObj table;
  private List<Attribute> attributes;
  private Attribute tablePK;

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
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryScatterDiagram(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute attribute2 = iterator.next();
    String query =
        "SELECT " + tablePK.getName() + ", " + attribute1.getName() + ", " + attribute2.getName()
            + " FROM " + table.getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryBubbleChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute attribute2 = iterator.next();
    Attribute attribute3 = iterator.next();
    Attribute optional = iterator.hasNext() ? iterator.next() : null;
    String query;
    if (optional != null) {
      query =
          "SELECT " + tablePK.getName() + ", " + attribute1.getName() + ", " + attribute2.getName()
              + ", " + attribute3.getName() + ", " + optional.getName() + " FROM "
              + table.getName();
    } else {
      query =
          "SELECT " + tablePK.getName() + ", " + attribute1.getName() + ", " + attribute2.getName()
              + ", " + attribute3.getName()
              + " FROM " + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryLineChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute optional = iterator.hasNext() ? iterator.next() : null;

    // must be weak entity
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    assert strongEntity != null;
    // primaryKey1 is k1, tablePK is k2
    Optional<Attribute> primaryKey1 = strongEntity.getAttributeList().stream()
        .filter(Attribute::getIsPrimary)
        .findFirst();
    assert primaryKey1.isPresent();
    String unambiguityPrefix = table.getName() + ".";
    String query;
    if (optional != null) {
      query = "SELECT " + primaryKey1.get().getName() + ", " + tablePK.getName() + ", "
          + unambiguityPrefix + attribute1.getName() + ", " + unambiguityPrefix + optional.getName()
          + " FROM " + table.getName()
          + " INNER JOIN " + strongEntity.getName()
          + " ON " + table.getName() + "." + strongEntity.getName() + " = "
          + strongEntity.getName() + "."
          + primaryKey1.get().getName();
    } else {
      query = "SELECT " + primaryKey1.get().getName() + ", " + tablePK.getName() + ", "
          + unambiguityPrefix + attribute1.getName() + " FROM " + table.getName()
          + " INNER JOIN " + strongEntity.getName()
          + " ON " + table.getName() + "." + strongEntity.getName() + " = "
          + strongEntity.getName() + "."
          + primaryKey1.get().getName();
    }
    return InputService.getJdbc().queryForList(query);
  }
}
