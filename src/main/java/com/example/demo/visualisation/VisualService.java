package com.example.demo.visualisation;

import com.example.demo.input.handler.InputService;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class VisualService {

  private ERConnectableObj table;
  private List<Attribute> attributes;
  private Attribute tablePrimaryKey;

  private void initialise(Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    table = selectionInfo.keySet().iterator().next();
    attributes = selectionInfo.get(table);
    Optional<Attribute> pk = table instanceof Entity ? ((Entity) table).getAttributeList().stream()
        .filter(Attribute::getIsPrimary).findFirst()
        : ((Relationship) table).getAttributeList().stream().filter(Attribute::getIsPrimary)
            .findFirst();
    pk.ifPresent(attribute -> tablePrimaryKey = attribute);
  }

  private String getForeignKeyName(String foreignKeyTableName, String primaryKeyTableName,
      String existingKeyName)
      throws SQLException {
    DatabaseMetaData metaData = Objects.requireNonNull(InputService.getJdbc().getDataSource())
        .getConnection().getMetaData();
    ResultSet foreignKeys = metaData.getImportedKeys(null, null, foreignKeyTableName);
    while (foreignKeys.next()) {
      String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
      String pkTableName = foreignKeys.getString("PKTABLE_NAME");
      if (pkTableName.equals(primaryKeyTableName) && !fkColumnName.equals(existingKeyName)) {
        return fkColumnName;
      }
    }
    return "";
  }

  private List<String> getCompoundForeignKeysName(String foreignKeyTableName,
      String primaryKeyTableName) throws SQLException {
    String fk1 = getForeignKeyName(foreignKeyTableName, primaryKeyTableName);
    String fk2 = getForeignKeyName(foreignKeyTableName, primaryKeyTableName, fk1);
    return List.of(fk1, fk2);
  }

  private String getForeignKeyName(String foreignKeyTableName, String primaryKeyTableName)
      throws SQLException {
    return getForeignKeyName(foreignKeyTableName, primaryKeyTableName, "");
  }

  public List<Map<String, Object>> queryBarChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Attribute attribute = attributes.iterator().next();
    String query =
        "SELECT " + tablePrimaryKey.getName() + ", " + attribute.getName() + " FROM " + table.getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryScatterDiagram(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute attribute2 = iterator.next();
    String query =
        "SELECT " + tablePrimaryKey.getName() + ", " + attribute1.getName() + ", " + attribute2.getName()
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
    Attribute optionalAttr = iterator.hasNext() ? iterator.next() : null;
    String query;
    if (optionalAttr != null) {
      query =
          "SELECT " + tablePrimaryKey.getName() + ", " + attribute1.getName() + ", " + attribute2.getName()
              + ", " + attribute3.getName() + ", " + optionalAttr.getName() + " FROM "
              + table.getName();
    } else {
      query =
          "SELECT " + tablePrimaryKey.getName() + ", " + attribute1.getName() + ", " + attribute2.getName()
              + ", " + attribute3.getName()
              + " FROM " + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryLineChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute optionalAttr = iterator.hasNext() ? iterator.next() : null;

    // must be weak entity
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    assert strongEntity != null;
    // fkStrongEntity is k1, tablePK is k2
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());

    String query;
    if (optionalAttr != null) {
      query = "SELECT " + fkStrongEntity + ", " + tablePrimaryKey.getName() + ", " + attribute1.getName()
          + ", " + optionalAttr.getName() + " FROM " + table.getName();
    } else {
      query = "SELECT " + fkStrongEntity + ", " + tablePrimaryKey.getName() + ", " + attribute1.getName()
          + " FROM " + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryStackedBarChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    assert strongEntity != null;
    // fkStrongEntity is k1, tablePK is k2
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    String query =
        "SELECT " + fkStrongEntity + ", " + tablePrimaryKey.getName() + ", " + attribute1.getName()
            + " FROM " + table.getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> querySpiderChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    assert strongEntity != null;

    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    String query =
        "SELECT " + fkStrongEntity + ", " + tablePrimaryKey.getName() + ", " + attribute1.getName()
            + " FROM " + table.getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryTreeMapData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute optionalAttr = iterator.hasNext() ? iterator.next() : null;

    Entity parentEntity = ModelUtil.getParentEntity((Entity) table,
        InputService.getSchema());
    assert parentEntity != null;
    String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
    String query;
    if (optionalAttr != null) {
      query = "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + ", " + attribute1.getName()
          + ", " + optionalAttr.getName() + " FROM " + table.getName();
    } else {
      query = "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + ", " + attribute1.getName()
          + " FROM " + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryHierarchyTreeData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Attribute optionalAttr = attributes.iterator().hasNext() ? attributes.iterator().next() : null;
    Entity parentEntity = ModelUtil.getParentEntity((Entity) table,
        InputService.getSchema());
    assert parentEntity != null;
    String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
    String query;
    if (optionalAttr != null) {
      query = "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + ", " + optionalAttr.getName()
          + " FROM " + table.getName();
    } else {
      query =
          "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + " FROM " + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryManyManyRelationshipData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    // todo: handle optional attributes
    Attribute optionalAttr = iterator.hasNext() ? iterator.next() : null;
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    String query = null;
    // Reflexive case
    if (entities.size() == 1) {
      Entity entity = entities.iterator().next();
      // assume reflexive relationship table has two foreign keys to the same entity
      List<String> compoundFKs = getCompoundForeignKeysName(table.getName(), entity.getName());
      Iterator<String> fkIterator = compoundFKs.iterator();
      String fk1 = fkIterator.next();
      String fk2 = fkIterator.next();
      query =
          "SELECT " + fk1 + ", " + fk2 + ", " + attribute1.getName() + " FROM " + table.getName();
    } else {
      Iterator<Entity> entityIterator = entities.iterator();
      Entity entity1 = entityIterator.next();
      Entity entity2 = entityIterator.next();

      String fkEntity1 = getForeignKeyName(table.getName(), entity1.getName());
      String fkEntity2 = getForeignKeyName(table.getName(), entity2.getName());
      query =
          "SELECT " + fkEntity1 + ", " + fkEntity2 + ", " + attribute1.getName() + " FROM "
              + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }
}
