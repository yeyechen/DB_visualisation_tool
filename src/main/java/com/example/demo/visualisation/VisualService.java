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
  private Attribute tablePK;

  private void initialise(Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    table = selectionInfo.keySet().iterator().next();
    attributes = selectionInfo.get(table);
    Optional<Attribute> pk = table instanceof Entity ? ((Entity) table).getAttributeList().stream()
        .filter(Attribute::getIsPrimary).findFirst()
        : ((Relationship) table).getAttributeList().stream().filter(Attribute::getIsPrimary)
            .findFirst();
    pk.ifPresent(attribute -> tablePK = attribute);
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
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
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
    String query;
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    if (optional != null) {
      query = "SELECT " + strongEntity.getName() + "." + primaryKey1.get().getName() + ", "
          + table.getName() + "." + tablePK.getName() + ", "
          + table.getName() + "." + attribute1.getName() + ", "
          + table.getName() + "." + optional.getName() + " FROM " + table.getName()
          + " INNER JOIN " + strongEntity.getName()
          + " ON " + table.getName() + "." + fkStrongEntity + " = "
          + strongEntity.getName() + "."
          + primaryKey1.get().getName();
    } else {
      query = "SELECT " + strongEntity.getName() + "." + primaryKey1.get().getName() + ", "
          + table.getName() + "." + tablePK.getName()
          + ", " + table.getName() + "." + attribute1.getName() + " FROM " + table.getName()
          + " INNER JOIN " + strongEntity.getName()
          + " ON " + table.getName() + "." + fkStrongEntity + " = "
          + strongEntity.getName() + "."
          + primaryKey1.get().getName();
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
    // primaryKey1 is k1, tablePK is k2
    Optional<Attribute> primaryKey1 = strongEntity.getAttributeList().stream()
        .filter(Attribute::getIsPrimary)
        .findFirst();
    assert primaryKey1.isPresent();
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    String query = "SELECT " + strongEntity.getName() + "." + primaryKey1.get().getName() + ", "
        + table.getName() + "." + tablePK.getName() + ", "
        + table.getName() + "." + attribute1.getName() + " FROM " + table.getName()
        + " INNER JOIN " + strongEntity.getName()
        + " ON " + table.getName() + "." + fkStrongEntity + " = "
        + strongEntity.getName() + "."
        + primaryKey1.get().getName();
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
    // primaryKey1 is k1, tablePK is k2
    Optional<Attribute> primaryKey1 = strongEntity.getAttributeList().stream()
        .filter(Attribute::getIsPrimary)
        .findFirst();
    assert primaryKey1.isPresent();
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    String query = "SELECT " + strongEntity.getName() + "." + primaryKey1.get().getName() + ", "
        + table.getName() + "." + tablePK.getName() + ", "
        + table.getName() + "." + attribute1.getName() + " FROM " + table.getName()
        + " INNER JOIN " + strongEntity.getName()
        + " ON " + table.getName() + "." + fkStrongEntity + " = "
        + strongEntity.getName() + "."
        + primaryKey1.get().getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryTreeMapData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
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
    String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
    String query;
    if (optional != null) {
      query = "SELECT " + parentEntity.getName() + "." + parentKey.get().getName() + ", "
          + table.getName() + "." + tablePK.getName() + ", "
          + table.getName() + "." + attribute1.getName() + ", "
          + table.getName() + "." + optional.getName()
          + " FROM " + table.getName()
          + " INNER JOIN " + parentEntity.getName()
          + " ON " + table.getName() + "." + fkParentEntity + " = "
          + parentEntity.getName() + "."
          + parentKey.get().getName();
    } else {
      query = "SELECT " + parentEntity.getName() + "." + parentKey.get().getName() + ", "
          + table.getName() + "." + tablePK.getName() + ", "
          + table.getName() + "." + attribute1.getName() + " FROM " + table.getName()
          + " INNER JOIN " + parentEntity.getName()
          + " ON " + table.getName() + "." + fkParentEntity + " = "
          + parentEntity.getName() + "."
          + parentKey.get().getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryHierarchyTreeData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
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
    String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
    String query = "SELECT " + parentEntity.getName() + "." + parentKey.get().getName() + ", "
        + table.getName() + "." + tablePK.getName() + ", "
        + table.getName() + "." + attribute1.getName() + " FROM " + table.getName()
        + " INNER JOIN " + parentEntity.getName()
        + " ON " + table.getName() + "." + fkParentEntity + " = "
        + parentEntity.getName() + "."
        + parentKey.get().getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> querySankeyDiagramData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    // todo: handle optional attributes
    Attribute optional = iterator.hasNext() ? iterator.next() : null;
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    String query = null;
    if (entities.size() == 1) {
      Entity entity = entities.iterator().next();
      Optional<Attribute> entityPK = entity.getAttributeList().stream()
          .filter(Attribute::getIsPrimary)
          .findFirst();
      assert entityPK.isPresent();
      // assume reflexive relationship table has two foreign keys to the same entity
      List<String> compoundFKs = getCompoundForeignKeysName(table.getName(), entity.getName());
      Iterator<String> fkIterator = compoundFKs.iterator();
      String fk1 = fkIterator.next();
      String fk2 = fkIterator.next();
      query =
          "SELECT " + "r" + "." + fk1 + ", " + "r" + "." + fk2 + ", " + "r" + "."
              + attribute1.getName()
              + " FROM " + table.getName() + " r" + " JOIN " + entity.getName() + " e1" + " ON "
              + "r" + "." + fk1 + " = " + "e1" + "."
              + entityPK.get().getName() + " JOIN " + entity.getName() + " e2" + " ON " + "r" + "."
              + fk2 + " = " + "e2" + "." + entityPK.get().getName();
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
      String fkEntity1 = getForeignKeyName(table.getName(), entity1.getName());
      String fkEntity2 = getForeignKeyName(table.getName(), entity2.getName());
      query =
          "SELECT " + entity1.getName() + "." + entity1PK.get().getName() + ", " + entity2.getName()
              + "." + entity2PK.get()
              .getName() + ", " + table.getName() + "." + attribute1.getName()
              + " FROM " + entity1.getName() + " INNER JOIN " + table.getName() + " ON "
              + entity1.getName() + "." + entity1PK.get().getName() + " = " + table.getName() + "."
              + fkEntity1 + " INNER JOIN " + entity2.getName() + " ON " + table.getName()
              + "."
              + fkEntity2 + " = " + entity2.getName() + "." + entity2PK.get().getName();
    }
    assert query != null;
    return InputService.getJdbc().queryForList(query);
  }
}
