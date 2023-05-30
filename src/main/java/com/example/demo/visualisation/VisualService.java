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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

  // for multiple tables
  private ERConnectableObj keyTable;
  private Attribute keyTablePrimaryKey;

  public Map<String, Map<String, List<String>>> filterConditions;

  public void initialise(Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    if (selectionInfo.keySet().size() > 1) {
      Iterator<ERConnectableObj> keyIterator = selectionInfo.keySet()
          .iterator();
      ERConnectableObj table1 = keyIterator.next();
      ERConnectableObj table2 = keyIterator.next();
      Iterator<Attribute> tableNameIterator = selectionInfo.get(table1).iterator();
      if (tableNameIterator.hasNext() && tableNameIterator
          .next().getIsPrimary()) {
        table = table2;
        keyTable = table1;
      } else {
        table = table1;
        keyTable = table2;
      }
      Optional<Attribute> keyTableKey = ((Entity) keyTable).getAttributeList().stream()
          .filter(Attribute::getIsPrimary).findFirst();
      keyTableKey.ifPresent(attribute -> keyTablePrimaryKey = attribute);
    } else {
      table = selectionInfo.keySet().iterator().next();
    }

    attributes = selectionInfo.get(table);
    Optional<Attribute> pk = table instanceof Entity ? ((Entity) table).getAttributeList().stream()
        .filter(Attribute::getIsPrimary).findFirst()
        : ((Relationship) table).getAttributeList().stream().filter(Attribute::getIsPrimary)
            .findFirst();
    pk.ifPresent(attribute -> tablePrimaryKey = attribute);
    // process filter conditions:

    this.filterConditions = filterConditions;
  }

  private static String getForeignKeyName(String foreignKeyTableName, String primaryKeyTableName,
      String existingKeyName)
      throws SQLException {
    ResultSet foreignKeys = InputService.getMetaData().getImportedKeys(null, null, foreignKeyTableName);
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

  public static String getForeignKeyName(String foreignKeyTableName, String primaryKeyTableName)
      throws SQLException {
    return getForeignKeyName(foreignKeyTableName, primaryKeyTableName, "");
  }

  public static String generateSQLQuery(
      List<String> attributes,
      String attrTableName,
      String attrTablePrimaryKey,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    return generateSQLQuery(attributes, attrTableName, attrTablePrimaryKey, "", "", filterConditions);
  }
  public static String generateSQLQuery(
      List<String> attributes,
      String attrTableName,
      String attrTablePrimaryKey,
      String keyTableName,
      String keyTablePrimaryKey,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    if (!keyTableName.isEmpty() && !filterConditions.containsKey(keyTableName)) {
      filterConditions.put(keyTableName, new HashMap<>());
    }
    if (filterConditions.isEmpty()) {
      return generateBasicSQLQuery(attributes, attrTableName);
    } else { // has filter conditions
      StringBuilder fromJoins = new StringBuilder(attrTableName);
      StringBuilder whereCondition = new StringBuilder();
      for (Map.Entry<String, Map<String, List<String>>> entry : filterConditions.entrySet()) {
        String joinTableName = entry.getKey();
        if (!joinTableName.equals(attrTableName) && !fromJoins.toString().contains(joinTableName)) {
          String fkName1 = getForeignKeyName(joinTableName, attrTableName);
          String fkName2 = getForeignKeyName(attrTableName, joinTableName);
          if (fkName1.isEmpty() && fkName2.isEmpty()) {
            // need to join three tables, including the middle relationship table
            String middleJoinTableName = Objects.requireNonNull(
                ModelUtil.getRelationshipBetween(attrTableName,
                    joinTableName,
                    InputService.getSchema())).getName();
            String fk1 = getForeignKeyName(middleJoinTableName, attrTableName);
            fromJoins.append(" INNER JOIN ").append(middleJoinTableName);
            fromJoins.append(" ON ");
            fromJoins.append(attrTableName).append(".").append(attrTablePrimaryKey);
            fromJoins.append("=").append(middleJoinTableName).append(".").append(fk1);

            String fk2 = getForeignKeyName(middleJoinTableName, joinTableName);
            fromJoins.append(" INNER JOIN ").append(joinTableName);
            fromJoins.append(" ON ");
            fromJoins.append(middleJoinTableName).append(".").append(fk2);
            fromJoins.append("=").append(joinTableName).append(".")
                .append(getPrimaryKeyName(joinTableName));
          }
          if (!fkName1.isEmpty()) {
            fromJoins.append(" INNER JOIN ").append(joinTableName);
            fromJoins.append(" ON ");
            fromJoins.append(attrTableName).append(".").append(attrTablePrimaryKey);
            fromJoins.append("=").append(joinTableName).append(".").append(fkName1);
          } else if (!fkName2.isEmpty()) {
            fromJoins.append(" INNER JOIN ").append(joinTableName);
            fromJoins.append(" ON ");
            fromJoins.append(attrTableName).append(".").append(fkName2);
            fromJoins.append("=").append(joinTableName).append(".")
                .append(getPrimaryKeyName(joinTableName));
          }
        }
        Map<String, List<String>> conditions = entry.getValue();
        Set<Entry<String, List<String>>> entries = conditions.entrySet();
        for (Map.Entry<String, List<String>> singleCondition : entries) {
          String conditionAttr = singleCondition.getKey();
          List<String> conditionValues = singleCondition.getValue();

          DataType type = DataTypeUtil.getDataType(joinTableName, conditionAttr
          );
          if (type == DataType.NUMERICAL) {
            whereCondition.append("(");
            // only two element in "conditionValues" index 0 for min, index 1 for max
            Iterator<String> minMax = conditionValues.iterator();
            String min = minMax.next();
            String max = minMax.next();
            whereCondition.append(joinTableName)
                .append(".")
                .append(conditionAttr)
                .append(">=")
                .append(min)
                .append(" AND ")
                .append(joinTableName)
                .append(".")
                .append(conditionAttr)
                .append("<=")
                .append(max);
            whereCondition.append(")");
          }

          if (type == DataType.LEXICAL) {
            whereCondition.append("(");
            for (String value : conditionValues) {
              String stringFormatValue = "'" + value + "'";
              whereCondition.append(joinTableName)
                  .append(".")
                  .append(conditionAttr)
                  .append("=")
                  .append(stringFormatValue);
              whereCondition.append(" OR ");
            }
            // remove the extra " OR " at the end
            whereCondition.setLength(whereCondition.length() - 4);
            whereCondition.append(")");
          }
          whereCondition.append(" AND ");
        }
      }
      if (!whereCondition.isEmpty()) {
        whereCondition.setLength(whereCondition.length() - 5);
      }
      StringBuilder selectAttr = new StringBuilder();
      if (!keyTableName.isEmpty() && !keyTablePrimaryKey.isEmpty()) {
        selectAttr.append(keyTableName).append(".").append(keyTablePrimaryKey);
        selectAttr.append(", ");
      }
      for (String attr : attributes) {
        selectAttr.append(attrTableName).append(".").append(attr);
        selectAttr.append(", ");
      }
      selectAttr.setLength(selectAttr.length() - 2);
      StringBuilder query = new StringBuilder("SELECT ");
      query.append(selectAttr);
      query.append(" FROM ");
      query.append(fromJoins);
      if (!whereCondition.isEmpty()) {
        query.append(" WHERE ");
        query.append(whereCondition);
      }
      System.out.println(query);
      return query.toString();
    }
  }

  private static String getPrimaryKeyName(String tableName) throws SQLException {
    ResultSet primaryKeys = InputService.getMetaData().getPrimaryKeys(null, null, tableName);
    if (primaryKeys.next()) {
      return primaryKeys.getString("COLUMN_NAME");
    }
    // No primary key found for the table
    return "";
  }

  // helper function to generate basic SQL queries (SELECT ... FROM ...)
  private static String generateBasicSQLQuery(List<String> attributes, String tableName) {
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
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute attribute = attributes.iterator().next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute.getName()),
        DataType.NUMERICAL);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute.getName());
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryPieChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute attribute = attributes.iterator().next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute.getName()),
        DataType.NUMERICAL);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute.getName());
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryCalendar(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions)
      throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute temporalAttr = null;
    Iterator<Attribute> iterator = attributes.iterator();
    while (iterator.hasNext()) {
      Attribute attribute = iterator.next();
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName())
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
          DataTypeUtil.getDataType(table.getName(), optionalAttr.getName()),
          DataType.NUMERICAL);
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryScatterDiagram(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    for (Attribute attribute : attributes) {
      Assert.assertSame(
          DataTypeUtil.getDataType(table.getName(), attribute.getName()),
          DataType.NUMERICAL);
    }
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.addAll(attributes.stream().map(ERBaseObj::getName).toList());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryBubbleChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    for (Attribute attribute : attributes) {
      Assert.assertSame(
          DataTypeUtil.getDataType(table.getName(), attribute.getName()),
          DataType.NUMERICAL);
    }
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.addAll(attributes.stream().map(ERBaseObj::getName).toList());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryChoroplethMap(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute attribute = attributes.iterator().next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute.getName()),
        DataType.NUMERICAL);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute.getName());
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryWordCloud(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    Attribute attribute1 = attributes.iterator().next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), tablePrimaryKey.getName()
        ),
        DataType.LEXICAL);
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName()),
        DataType.NUMERICAL);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute1.getName());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryLineChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Attribute optionalAttr = iterator.hasNext() ? iterator.next() : null;
    // must be weak entity
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(strongEntity);
    // fkStrongEntity is k1, tablePK is k2
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName()),
        DataType.NUMERICAL);
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), tablePrimaryKey.getName()
        ),
        DataType.NUMERICAL);
    attributeNameStrings.add(fkStrongEntity);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute1.getName());
    if (optionalAttr != null) {
      Assert.assertSame(
          DataTypeUtil.getDataType(table.getName(), optionalAttr.getName()),
          DataType.NUMERICAL);
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryStackedBarChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName()),
        DataType.NUMERICAL);
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(strongEntity);
    // fkStrongEntity is k1, tablePK is k2
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    attributeNameStrings.add(fkStrongEntity);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute1.getName());
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryGroupedBarChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName()),
        DataType.NUMERICAL);
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(strongEntity);
    // fkStrongEntity is k1, tablePK is k2
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    attributeNameStrings.add(fkStrongEntity);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute1.getName());
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> querySpiderChart(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName()),
        DataType.NUMERICAL);
    Entity strongEntity = ModelUtil.getRelatedStrongEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(strongEntity);
    // fkStrongEntity is k1, tablePK is k2
    String fkStrongEntity = getForeignKeyName(table.getName(), strongEntity.getName());
    attributeNameStrings.add(fkStrongEntity);
    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute1.getName());
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryTreeMapData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(DataTypeUtil.getDataType(table.getName(), attribute1.getName()
    ), DataType.NUMERICAL);

    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute1.getName());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    Entity parentEntity;
    if (keyTable != null) {
      return InputService.getJdbc().queryForList(
          generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
              keyTable.getName(), keyTablePrimaryKey.getName(), this.filterConditions));
    } else {
      parentEntity = ModelUtil.getParentEntity((Entity) table,
          InputService.getSchema());
      String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
      attributeNameStrings.add(fkParentEntity);
      Assert.assertNotNull(parentEntity);
      return InputService.getJdbc()
          .queryForList(
              generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                  this.filterConditions));
    }
  }

  public List<Map<String, Object>> queryHierarchyTreeData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute optionalAttr = attributes.iterator().hasNext() ? attributes.iterator().next() : null;
    Entity parentEntity = ModelUtil.getParentEntity((Entity) table,
        InputService.getSchema());
    Assert.assertNotNull(parentEntity);
    attributeNameStrings.add(tablePrimaryKey.getName());
    if (optionalAttr != null) {
      Assert.assertSame(DataTypeUtil.getDataType(table.getName(), optionalAttr.getName()
      ), DataType.LEXICAL);
      attributeNameStrings.add(optionalAttr.getName());
    }
    if (keyTable != null) {
      return InputService.getJdbc()
          .queryForList(
              generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                  keyTable.getName(), keyTablePrimaryKey.getName(),
                  this.filterConditions));
    }
    String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
    attributeNameStrings.add(fkParentEntity);
    return InputService.getJdbc()
        .queryForList(
            generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                this.filterConditions));
  }

  public List<Map<String, Object>> queryCirclePacking(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    List<String> attributeNameStrings = new ArrayList<>();

    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(DataTypeUtil.getDataType(table.getName(), attribute1.getName()
    ), DataType.NUMERICAL);

    attributeNameStrings.add(tablePrimaryKey.getName());
    attributeNameStrings.add(attribute1.getName());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    Entity parentEntity;
    if (keyTable != null) {
      return InputService.getJdbc().queryForList(
          generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
              keyTable.getName(), keyTablePrimaryKey.getName(), this.filterConditions));
    } else {
      parentEntity = ModelUtil.getParentEntity((Entity) table,
          InputService.getSchema());
      String fkParentEntity = getForeignKeyName(table.getName(), parentEntity.getName());
      attributeNameStrings.add(fkParentEntity);
      Assert.assertNotNull(parentEntity);
      return InputService.getJdbc()
          .queryForList(
              generateSQLQuery(attributeNameStrings, table.getName(), tablePrimaryKey.getName(),
                  this.filterConditions));
    }
  }

  public List<Map<String, Object>> querySankeyDiagramData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);

    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    // reflexive case
    if (entities.size() == 1) {
      // attributes identical to chord case (see chart on paper)
      return queryChordDiagramData(selectionInfo, filterConditions);
    }

    Attribute optionalAttr = null;
    Iterator<Attribute> attributeIterator = attributes.iterator();
    while (attributeIterator.hasNext()) {
      Attribute attribute = attributeIterator.next();
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributeIterator.remove();
      }
    }

    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName()),
        DataType.NUMERICAL);
    Assert.assertEquals(entities.size(), 2);
    List<String> attributeNameStrings = new ArrayList<>(
        entities.stream().map(ERBaseObj::getName).toList());
    attributeNameStrings.add(attribute1.getName());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(generateSQLQuery(attributeNameStrings, table.getName(), "",
            this.filterConditions));
  }

  public List<Map<String, Object>> queryNetworkChartData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);

    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
      }
    }
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    // reflexive
    Assert.assertEquals(entities.size(), 1);

    Entity entity = entities.iterator().next();
    // assume reflexive relationship table has two foreign keys to the same entity
    List<String> compoundFKs = getCompoundForeignKeysName(table.getName(), entity.getName());
    List<String> attributeNameStrings = new ArrayList<>(compoundFKs);

    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(generateSQLQuery(attributeNameStrings, table.getName(), "",
            this.filterConditions));
  }

  public List<Map<String, Object>> queryChordDiagramData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    Attribute optionalAttr = null;
    for (Attribute attribute : attributes) {
      if (DataTypeUtil.getDataType(table.getName(), attribute.getName())
          == DataType.LEXICAL) {
        optionalAttr = attribute;
        attributes.remove(attribute);
      }
    }
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName()),
        DataType.NUMERICAL);
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    // assert reflexive
    Assert.assertEquals(entities.size(), 1);
    String query;

    Entity entity = entities.iterator().next();
    // assume reflexive relationship table has two foreign keys to the same entity
    List<String> compoundFKs = getCompoundForeignKeysName(table.getName(), entity.getName());
    List<String> attributeNameStrings = new ArrayList<>(compoundFKs);
    attributeNameStrings.add(attribute1.getName());
    if (optionalAttr != null) {
      attributeNameStrings.add(optionalAttr.getName());
    }
    return InputService.getJdbc()
        .queryForList(generateSQLQuery(attributeNameStrings, table.getName(), "",
            this.filterConditions));
  }

  public List<Map<String, Object>> queryHeatmapData(
      Map<ERConnectableObj, List<Attribute>> selectionInfo,
      Map<String, Map<String, List<String>>> filterConditions) throws SQLException {
    initialise(selectionInfo, filterConditions);
    Iterator<Attribute> iterator = attributes.iterator();
    Attribute attribute1 = iterator.next();
    Assert.assertSame(
        DataTypeUtil.getDataType(table.getName(), attribute1.getName()),
        DataType.NUMERICAL);
    Set<Entity> entities = ModelUtil.getManyManyEntities((Relationship) table);
    // assert reflexive
    Assert.assertEquals(entities.size(), 1);
    Entity entity = entities.iterator().next();
    // assume reflexive relationship table has two foreign keys to the same entity
    List<String> compoundFKs = getCompoundForeignKeysName(table.getName(), entity.getName());
    List<String> attributeNameStrings = new ArrayList<>(compoundFKs);
    attributeNameStrings.add(attribute1.getName());
    return InputService.getJdbc()
        .queryForList(generateSQLQuery(attributeNameStrings, table.getName(), "",
            this.filterConditions));
  }
}
