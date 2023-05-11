package com.example.demo.visualisation;

import com.example.demo.data.types.DataType;
import com.example.demo.data.types.DataTypeUtil;
import com.example.demo.input.handler.InputService;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERBaseObj;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;
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

  // helper function to generate basic SQL queries (SELECT ... FROM ...)
  private String generateBasicSQLQuery(List<String> attributes, String tableName) {
    StringBuilder sb = new StringBuilder("SELECT ");
    Iterator<String> attrIterator = attributes.iterator();
    sb.append(attrIterator.next());
    while (attrIterator.hasNext()) {
      sb.append(", ").append(attrIterator.next());
    }
    sb.append(" FROM ");
    sb.append(tableName);
    return sb.toString();
  }

  public List<Map<String, Object>> queryBarChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute attribute = attributes.iterator().next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc()),
        DataType.NUMERICAL);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute.getName());
    return InputService.getJdbc()
        .queryForList(generateBasicSQLQuery(attributeNameStrings, table.getName()));
  }

  public List<Map<String, Object>> queryCalendar(Map<ERConnectableObj, List<Attribute>> selectionInfo)
      throws SQLException {
    initialise(selectionInfo);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute temporalAttr = null;
    Iterator<Attribute> iterator = attributes.iterator();
    while (iterator.hasNext()) {
      Attribute attribute = iterator.next();
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc())
          == DataType.TEMPORAL) {
        temporalAttr = attribute;
        iterator.remove();
      }
    }
    Assert.assertNotNull(temporalAttr);
    Attribute optionalAttr = attributes.iterator().hasNext() ? attributes.iterator().next() : null;

    attributeNameStrings.add(temporalAttr.getName());
    if (optionalAttr != null) {
      Assert.assertSame(
          DataTypeUtil.getDataType(table.getName(), optionalAttr.getName(), InputService.getJdbc()),
          DataType.NUMERICAL);
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(generateBasicSQLQuery(attributeNameStrings, table.getName()));
  }

  public List<Map<String, Object>> queryScatterDiagram(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    for (Attribute attribute : attributes) {
      Assert.assertSame(
          DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc()),
          DataType.NUMERICAL);
    }
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.addAll(attributes.stream().map(ERBaseObj::getName).toList());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(generateBasicSQLQuery(attributeNameStrings, table.getName()));
  }

  public List<Map<String, Object>> queryBubbleChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    for (Attribute attribute : attributes) {
      Assert.assertSame(
          DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc()),
          DataType.NUMERICAL);
    }
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.addAll(attributes.stream().map(ERBaseObj::getName).toList());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(generateBasicSQLQuery(attributeNameStrings, table.getName()));
  }

  public List<Map<String, Object>> queryWordCloud(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    Attribute attribute1 = attributes.iterator().next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), tablePrimaryKey.getName(), InputService.getJdbc()),
        DataType.LEXICAL);
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName(), InputService.getJdbc()),
        DataType.NUMERICAL);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute1.getName());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(generateBasicSQLQuery(attributeNameStrings, table.getName()));
  }

  public List<Map<String, Object>> queryLineChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute optionalAttr = iterator.hasNext() ? iterator.next() : null;
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName(), InputService.getJdbc()),
        DataType.NUMERICAL);
    // must be weak entity
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(strongEntity);
    // fkStrongEntity is k1, tablePK is k2
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), tablePrimaryKey.getName(),
            InputService.getJdbc()),
        DataType.NUMERICAL);
    String query;
    if (optionalAttr != null) {
      Assert.assertSame(
          DataTypeUtil.getDataType(table.getName(), optionalAttr.getName(), InputService.getJdbc()),
          DataType.NUMERICAL);
      query = "SELECT " + fkStrongEntity + ", " + tablePrimaryKey.getName() + ", "
          + attribute1.getName()
          + ", " + optionalAttr.getName() + " FROM " + table.getName();
    } else {
      query = "SELECT " + fkStrongEntity + ", " + tablePrimaryKey.getName() + ", "
          + attribute1.getName()
          + " FROM " + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryStackedBarChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName(), InputService.getJdbc()),
        DataType.NUMERICAL);
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(strongEntity);
    // fkStrongEntity is k1, tablePK is k2
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    String query =
        "SELECT " + fkStrongEntity + ", " + tablePrimaryKey.getName() + ", " + attribute1.getName()
            + " FROM " + table.getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryGroupedBarChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName(), InputService.getJdbc()),
        DataType.NUMERICAL);
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(strongEntity);
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
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName(), InputService.getJdbc()),
        DataType.NUMERICAL);
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(strongEntity);
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    String query =
        "SELECT " + fkStrongEntity + ", " + tablePrimaryKey.getName() + ", " + attribute1.getName()
            + " FROM " + table.getName();
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryTreeMapData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(DataTypeUtil.getDataType(table.getName(), attribute1.getName(),
        InputService.getJdbc()), DataType.NUMERICAL);
    Entity parentEntity = ModelUtil.getParentEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(parentEntity);
    String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
    String query;
    if (optionalAttr != null) {
      query = "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + ", "
          + attribute1.getName()
          + ", " + optionalAttr.getName() + " FROM " + table.getName();
    } else {
      query = "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + ", "
          + attribute1.getName()
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
    Assert.assertNotNull(parentEntity);
    String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
    String query;
    if (optionalAttr != null) {
      Assert.assertSame(DataTypeUtil.getDataType(table.getName(), optionalAttr.getName(),
          InputService.getJdbc()), DataType.LEXICAL);
      query = "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + ", "
          + optionalAttr.getName()
          + " FROM " + table.getName();
    } else {
      query =
          "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + " FROM "
              + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryCirclePacking(
      Map<ERConnectableObj, List<Attribute>> selectionInfo)
      throws SQLException {
    initialise(selectionInfo);
    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(DataTypeUtil.getDataType(table.getName(), attribute1.getName(),
        InputService.getJdbc()), DataType.NUMERICAL);
    Entity parentEntity = ModelUtil.getParentEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(parentEntity);
    String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
    String query;
    if (optionalAttr != null) {
      query = "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + ", "
          + attribute1.getName()
          + ", " + optionalAttr.getName() + " FROM " + table.getName();
    } else {
      query = "SELECT " + fkParentEntity + ", " + tablePrimaryKey.getName() + ", "
          + attribute1.getName()
          + " FROM " + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> querySankeyDiagramData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    // reflexive case
    if (entities.size() == 1) {
      // attributes identical to chord case (see chart on paper)
      return queryChordDiagramData(selectionInfo);
    }
    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName(), InputService.getJdbc()),
        DataType.NUMERICAL);
    Assert.assertEquals(entities.size(), 2);
    Iterator<Entity> entityIterator = entities.iterator();
    Entity entity1 = entityIterator.next();
    Entity entity2 = entityIterator.next();
    String fkEntity1 = getForeignKeyName(table.getName(), entity1.getName());
    String fkEntity2 = getForeignKeyName(table.getName(), entity2.getName());
    String query;
    if (optionalAttr != null) {
      query =
          "SELECT " + fkEntity1 + ", " + fkEntity2 + ", " + attribute1.getName() + ", "
              + optionalAttr.getName() + " FROM " + table.getName();
    } else {
      query =
          "SELECT " + fkEntity1 + ", " + fkEntity2 + ", " + attribute1.getName() + " FROM "
              + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryNetworkChartData(Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
      }
    }
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    // reflexive
    Assert.assertEquals(entities.size(), 1);
    String query;

    Entity entity = entities.iterator().next();
    // assume reflexive relationship table has two foreign keys to the same entity
    List<String> compoundFKs = getCompoundForeignKeysName(table.getName(), entity.getName());
    Iterator<String> fkIterator = compoundFKs.iterator();
    String fk1 = fkIterator.next();
    String fk2 = fkIterator.next();
    if (optionalAttr != null) {
      query =
          "SELECT " + fk1 + ", " + fk2 + ", "
              + optionalAttr.getName() + " FROM " + table.getName();
    } else {
      query =
          "SELECT " + fk1 + ", " + fk2 + " FROM " + table.getName();
    }
    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryChordDiagramData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName(), InputService.getJdbc())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName(), InputService.getJdbc()),
        DataType.NUMERICAL);
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    // assert reflexive
    Assert.assertEquals(entities.size(), 1);
    String query;

    Entity entity = entities.iterator().next();
    // assume reflexive relationship table has two foreign keys to the same entity
    List<String> compoundFKs = getCompoundForeignKeysName(table.getName(), entity.getName());
    Iterator<String> fkIterator = compoundFKs.iterator();
    String fk1 = fkIterator.next();
    String fk2 = fkIterator.next();
    if (optionalAttr != null) {
      query =
          "SELECT " + fk1 + ", " + fk2 + ", " + attribute1.getName() + ", "
              + optionalAttr.getName() + " FROM " + table.getName();
    } else {
      query =
          "SELECT " + fk1 + ", " + fk2 + ", " + attribute1.getName() + " FROM " + table.getName();
    }

    return InputService.getJdbc().queryForList(query);
  }

  public List<Map<String, Object>> queryHeatmapData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo) throws SQLException {
    initialise(selectionInfo);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName(), InputService.getJdbc()),
        DataType.NUMERICAL);
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    // assert reflexive
    Assert.assertEquals(entities.size(), 1);
    Entity entity = entities.iterator().next();
    // assume reflexive relationship table has two foreign keys to the same entity
    List<String> compoundFKs = getCompoundForeignKeysName(table.getName(), entity.getName());
    Iterator<String> fkIterator = compoundFKs.iterator();
    String fk1 = fkIterator.next();
    String fk2 = fkIterator.next();
    String query =
        "SELECT " + fk1 + ", " + fk2 + ", " + attribute1.getName() + " FROM " + table.getName();
    return InputService.getJdbc().queryForList(query);
  }
}
