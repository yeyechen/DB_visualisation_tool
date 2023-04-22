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
import java.util.Set;
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

  public List<Map<String, Object>> queryStackedBarChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    assert strongEntity != null;
    // primaryKey1 is k1, tablePK is k2
    Optional<Attribute> primaryKey1 = strongEntity.getAttributeList().stream()
        .filter(Attribute::getIsPrimary)
        .findFirst();
    assert primaryKey1.isPresent();
    String unambiguityPrefix = table.getName() + ".";
    String query = "SELECT " + primaryKey1.get().getName() + ", " + tablePK.getName() + ", "
        + unambiguityPrefix + attribute1.getName() + " FROM " + table.getName()
        + " INNER JOIN " + strongEntity.getName()
        // todo: when accessing the column when join, there is risk using strongEntity.getName()
        //  needs proper foreign key info!
        + " ON " + table.getName() + "." + strongEntity.getName() + " = "
        + strongEntity.getName() + "."
        + primaryKey1.get().getName();
    return InputService.getJdbc().queryForList(query);
  }

  // todo: same as query stacked bar chart, needs refactor
  public List<Map<String, Object>> querySpiderChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    assert strongEntity != null;
    // primaryKey1 is k1, tablePK is k2
    Optional<Attribute> primaryKey1 = strongEntity.getAttributeList().stream()
        .filter(Attribute::getIsPrimary)
        .findFirst();
    assert primaryKey1.isPresent();
    String unambiguityPrefix = table.getName() + ".";
    String query = "SELECT " + primaryKey1.get().getName() + ", " + tablePK.getName() + ", "
        + unambiguityPrefix + attribute1.getName() + " FROM " + table.getName()
        + " INNER JOIN " + strongEntity.getName()
        + " ON " + table.getName() + "." + strongEntity.getName() + " = "
        + strongEntity.getName() + "."
        + primaryKey1.get().getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryTreeMapData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute optional = iterator.hasNext() ? iterator.next() : null;

    Entity parentEntity = ModelUtil.getParentEntity((Entity) table,
        InputService.getSchema());
    assert parentEntity != null;
    Optional<Attribute> parentKey = parentEntity.getAttributeList().stream()
        .filter(Attribute::getIsPrimary)
        .findFirst();
    assert parentKey.isPresent();
    String unambiguityPrefix = table.getName() + ".";
    String query;
    if (optional != null) {
      query = "SELECT " + parentKey.get().getName() + ", " + tablePK.getName() + ", "
          + unambiguityPrefix + attribute1.getName() + unambiguityPrefix + optional.getName()
          + " FROM " + table.getName()
          + " INNER JOIN " + parentEntity.getName()
          + " ON " + table.getName() + "." + parentEntity.getName() + " = "
          + parentEntity.getName() + "."
          + parentKey.get().getName();
    } else {
      query = "SELECT " + parentKey.get().getName() + ", " + tablePK.getName() + ", "
          + unambiguityPrefix + attribute1.getName() + " FROM " + table.getName()
          + " INNER JOIN " + parentEntity.getName()
          + " ON " + table.getName() + "." + parentEntity.getName() + " = "
          + parentEntity.getName() + "."
          + parentKey.get().getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryHierarchyTreeData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Entity parentEntity = ModelUtil.getParentEntity((Entity) table,
        InputService.getSchema());
    assert parentEntity != null;
    Optional<Attribute> parentKey = parentEntity.getAttributeList().stream()
        .filter(Attribute::getIsPrimary)
        .findFirst();
    assert parentKey.isPresent();
    String unambiguityPrefix = table.getName() + ".";
    String query = "SELECT " + parentKey.get().getName() + ", " + tablePK.getName() + ", "
        + unambiguityPrefix + attribute1.getName() + " FROM " + table.getName()
        + " INNER JOIN " + parentEntity.getName()
        + " ON " + table.getName() + "." + parentEntity.getName() + " = "
        + parentEntity.getName() + "."
        + parentKey.get().getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> querySankeyDiagramData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute optional = iterator.hasNext() ? iterator.next() : null;
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    // todo: Reflexive case
    String query = null;
    if (entities.size() == 1) {

    } else {
      Iterator<Entity> entityIterator = entities.iterator();
      Entity entity1 = entityIterator.next();
      Entity entity2 = entityIterator.next();

      Optional<Attribute> entity1PK = entity1.getAttributeList().stream()
          .filter(Attribute::getIsPrimary)
          .findFirst();
      assert entity1PK.isPresent();

      Optional<Attribute> entity2PK = entity2.getAttributeList().stream()
          .filter(Attribute::getIsPrimary)
          .findFirst();
      assert entity2PK.isPresent();
      query =
          "SELECT " + entity1PK.get().getName() + ", " + entity1.getName() + "." + entity2PK.get()
              .getName() + ", " + attribute1.getName()
              + " FROM " + entity1.getName() + " INNER JOIN " + table.getName() + " ON "
              + entity1.getName() + "." + entity1PK.get().getName() + " = " + table.getName() + "."
              + entity1.getName() + " INNER JOIN " + entity2.getName() + " ON " + table.getName()
              + "."
              + entity2.getName() + " = " + entity2.getName() + "." + entity2PK.get().getName();
    }
    assert query != null;
    return InputService.getJdbc().queryForList(query);
  }
}
