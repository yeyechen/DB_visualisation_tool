package com.example.demo.input.handler;

import com.example.demo.data.types.DataType;
import com.example.demo.data.types.DataTypeUtil;
import com.example.demo.models.ModelType;
import com.example.demo.models.ModelUtil;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ER;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.AttributeType;
import io.github.MigadaTang.common.Cardinality;
import io.github.MigadaTang.common.RDBMSType;
import io.github.MigadaTang.exception.DBConnectionException;
import io.github.MigadaTang.exception.ParseException;
import io.github.MigadaTang.transform.DatabaseUtil;
import io.github.MigadaTang.transform.Reverse;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

@Getter
@Service
public class InputService {

  private ModelType modelType;

  private static Schema schema;

  private static Map<ERConnectableObj, List<Attribute>> selectionInfo;

  private static Map<String, Map<String, List<String>>> filterConditions;

  private static JdbcTemplate jdbc;

  private static DatabaseMetaData metaData;

  public static Schema getSchema() {
    return schema;
  }

  public static Map<ERConnectableObj, List<Attribute>> getSelectionInfo() {
    return selectionInfo;
  }

  public static JdbcTemplate getJdbc() {
    return jdbc;
  }

  public static DatabaseMetaData getMetaData() {
    return metaData;
  }

  //todo: delete cache
  @PostConstruct
  public void init() throws SQLException, DBConnectionException, ParseException {
    ER.initialize();
    Schema mondialSchema = ER.createSchema("Mondial_Test");

    Entity country = mondialSchema.addEntity("country");
    country.addPrimaryKey("code", io.github.MigadaTang.common.DataType.VARCHAR);
    country.addAttribute("population", io.github.MigadaTang.common.DataType.INT, AttributeType.Mandatory);
    country.addAttribute("area", io.github.MigadaTang.common.DataType.DOUBLE, AttributeType.Mandatory);
    country.addAttribute("province", io.github.MigadaTang.common.DataType.VARCHAR, AttributeType.Mandatory);
    country.addAttribute("capital", io.github.MigadaTang.common.DataType.VARCHAR, AttributeType.Mandatory);
    country.addAttribute("name", io.github.MigadaTang.common.DataType.VARCHAR, AttributeType.Mandatory);

    Entity economy = mondialSchema.addSubset("economy", country);
    economy.addAttribute("gdp", io.github.MigadaTang.common.DataType.DOUBLE,
        AttributeType.Optional);
    economy.addAttribute("agriculture", io.github.MigadaTang.common.DataType.DOUBLE,
        AttributeType.Optional);
    economy.addAttribute("service", io.github.MigadaTang.common.DataType.DOUBLE,
        AttributeType.Optional);
    economy.addAttribute("industry", io.github.MigadaTang.common.DataType.DOUBLE,
        AttributeType.Optional);
    economy.addAttribute("inflation", io.github.MigadaTang.common.DataType.DOUBLE,
        AttributeType.Optional);
    economy.addAttribute("unemployment", io.github.MigadaTang.common.DataType.DOUBLE,
        AttributeType.Optional);

    // Reflexive Relationship
    Relationship borders = mondialSchema.createRelationship("borders", country,
        country, Cardinality.OneToMany, Cardinality.OneToMany);
    borders.addAttribute("length", io.github.MigadaTang.common.DataType.DOUBLE, AttributeType.Mandatory);

    // Weak Entities
    ImmutablePair<Entity, Relationship> provincePartOfPair = mondialSchema.addWeakEntity("province",
        country, "partOf", Cardinality.OneToOne, Cardinality.ZeroToMany);
    Entity province = provincePartOfPair.left;
    Relationship provincePartOf = provincePartOfPair.right;
    province.addPrimaryKey("name", io.github.MigadaTang.common.DataType.VARCHAR);
    province.addAttribute("population", io.github.MigadaTang.common.DataType.INT, AttributeType.Mandatory);
    province.addAttribute("area", io.github.MigadaTang.common.DataType.DOUBLE, AttributeType.Mandatory);
    province.addAttribute("capital_province", io.github.MigadaTang.common.DataType.VARCHAR, AttributeType.Mandatory);
    province.addAttribute("capital", io.github.MigadaTang.common.DataType.VARCHAR, AttributeType.Mandatory);

    ImmutablePair<Entity, Relationship> countryPopPartOfPair = mondialSchema.addWeakEntity("country_population",
        country, "partOf", Cardinality.OneToOne, Cardinality.ZeroToMany);
    Entity countryPop = countryPopPartOfPair.left;
    Relationship countryPopPartOf = countryPopPartOfPair.right;
    countryPop.addPrimaryKey("year", io.github.MigadaTang.common.DataType.VARCHAR);
    countryPop.addAttribute("population", io.github.MigadaTang.common.DataType.INT,
        AttributeType.Mandatory);

    Entity continent = mondialSchema.addEntity("continent");
    continent.addPrimaryKey("name", io.github.MigadaTang.common.DataType.DOUBLE);
    continent.addAttribute("area", io.github.MigadaTang.common.DataType.DOUBLE,
        AttributeType.Mandatory);
    Relationship encompasses = mondialSchema.createRelationship("encompasses", continent, country,
        Cardinality.ZeroToMany, Cardinality.ZeroToMany); // should be (1:2), but for test purposes
    encompasses.addAttribute("percentage", io.github.MigadaTang.common.DataType.DOUBLE, AttributeType.Mandatory);

    Entity airport = mondialSchema.addEntity("airport");
    Relationship in = mondialSchema.createRelationship("in", airport, country, Cardinality.OneToOne,
        Cardinality.ZeroToMany);
    airport.addPrimaryKey("iata_code", io.github.MigadaTang.common.DataType.VARCHAR);
    airport.addAttribute("name", io.github.MigadaTang.common.DataType.VARCHAR, AttributeType.Mandatory);
    airport.addAttribute("island", io.github.MigadaTang.common.DataType.VARCHAR, AttributeType.Mandatory);
    airport.addAttribute("province", io.github.MigadaTang.common.DataType.VARCHAR, AttributeType.Mandatory);
    airport.addAttribute("city", io.github.MigadaTang.common.DataType.VARCHAR, AttributeType.Mandatory);
    airport.addAttribute("latitude", io.github.MigadaTang.common.DataType.DOUBLE, AttributeType.Mandatory);
    airport.addAttribute("longitude", io.github.MigadaTang.common.DataType.DOUBLE, AttributeType.Mandatory);
    airport.addAttribute("elevation", io.github.MigadaTang.common.DataType.DOUBLE, AttributeType.Mandatory);
    airport.addAttribute("gmt_offset", io.github.MigadaTang.common.DataType.DOUBLE, AttributeType.Mandatory);

    schema = mondialSchema;

    Map<String, String> formData = new HashMap<>();
    formData.put("dbType", "postgresql");
    formData.put("host", "localhost");
    formData.put("port", "5432");
    formData.put("databaseName", "sub_mondial");
    formData.put("username", "Mikeee");
    formData.put("password", "");
    initialise(formData);
  }

  public void setFilterConditions(Map<String, List<String>> filterConditions) {
    Map<String, Map<String, List<String>>> processedConditions = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : filterConditions.entrySet()) {
      String key = entry.getKey();
      String[] parts = key.split("\\.");
      String tableName = parts[0];
      String attributeName = parts[1];
      List<String> value = entry.getValue();

      if (!processedConditions.containsKey(tableName)) {
        Map<String, List<String>> singleCondition = new HashMap<>();
        singleCondition.put(attributeName, value);
        processedConditions.put(tableName, singleCondition);
      } else {
        Map<String, List<String>> conditions = processedConditions.get(tableName);
        conditions.put(attributeName, value);
        processedConditions.put(tableName, conditions);
      }
    }
    InputService.filterConditions = processedConditions;
  }

  public static Map<String, Map<String, List<String>>> getFilterConditions() {
    return filterConditions;
  }

  public void initialise(Map<String, String> formData)
      throws SQLException, ParseException, DBConnectionException {
    String dbTypeString = formData.get("dbType");
    RDBMSType dbTypeEnum = switch (dbTypeString) {
      case "postgresql" -> RDBMSType.POSTGRESQL;
      case "mysql" -> RDBMSType.MYSQL;
      case "oracle" -> RDBMSType.ORACLE;
      case "sqlserver" -> RDBMSType.SQLSERVER;
      case "db2" -> RDBMSType.DB2;
      case "h2" -> RDBMSType.H2;
      default -> null;
    };
    String host = formData.get("host");
    String port = formData.get("port");
    String databaseName = formData.get("databaseName");
    String username = formData.get("username");
    String password = formData.get("password");

    // todo: remember to uncomment
    // schema = reverse(dbTypeEnum, host, port, databaseName, username, password);
    jdbc = updateDatabaseDetails(dbTypeEnum, host, port, databaseName, username, password);

    selectionInfo = new HashMap<>();
    metaData = Objects.requireNonNull(jdbc.getDataSource())
        .getConnection().getMetaData();
  }

  public Schema reverse(RDBMSType dbTypeEnum, String host, String port, String databaseName,
      String username, String password) throws ParseException, DBConnectionException, SQLException {
    ER.initialize();
    Reverse reverse = new Reverse();
    Schema schema = reverse.relationSchemasToERModel(dbTypeEnum, host, port, databaseName, username,
        password);
    return schema;
  }

  public JdbcTemplate updateDatabaseDetails(RDBMSType dbType, String host, String port, String databaseName, String username, String password)
      throws ParseException {

    String dbUrl = DatabaseUtil.generateDatabaseURL(dbType, host, port, databaseName);

    // create datasource
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(dbUrl);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    return new JdbcTemplate(dataSource);
  }

  public void patternMatchBasedOnSelection(Map<ERConnectableObj, List<Attribute>> selectionInfo) {
    InputService.selectionInfo = selectionInfo;
    modelType = ModelUtil.patternMatchBasedOnSelection(selectionInfo, schema);
    filterConditions = new HashMap<>();
  }

  public boolean checkSelectNone() {
    // here we assume that user only select in on table, but in future development we may not guarantee that
    List<Attribute> attributes = selectionInfo.values().iterator().next();
    return attributes.isEmpty();
  }

  // get filter options for discrete data types
  public List<String> getDiscreteFilterOptions(String tableName, String attributeName) {
    StringBuilder query = new StringBuilder("SELECT ").append(attributeName);
    query.append(" FROM ").append(tableName);

    List<Map<String, Object>> resultMaps = jdbc.queryForList(query.toString());
    List<String> result = new ArrayList<>();
    for (Map<String, Object> row : resultMaps) {
      String element = (String) row.get(attributeName);
      result.add(element);
    }
    return result;
  }

  // get filter options for scalar data types
  public List<BigDecimal> getScalarFilterOptions(String tableName, String attributeName) {
    StringBuilder query = new StringBuilder("SELECT ").append(attributeName);
    query.append(" FROM ").append(tableName);

    List<Map<String, Object>> resultMaps = jdbc.queryForList(query.toString());
    List<BigDecimal> resultList = new ArrayList<>();
    for (Map<String, Object> resultMap : resultMaps) {
      Object value = resultMap.get(attributeName);
      // handle nullable values
      if (value != null) {
        Number numberValue = (Number) value;
        BigDecimal decimalValue = new BigDecimal(numberValue.toString());
        resultList.add(decimalValue);
      }
    }

    if (resultList.isEmpty()) {
      // handle the case where all values were null
      return Collections.emptyList();
    }
    BigDecimal minValue = Collections.min(resultList);
    BigDecimal maxValue = Collections.max(resultList);

    return List.of(minValue, maxValue);
  }

  // when multiple tables are selected, get the one with attributes
  public ERConnectableObj getAttrTable() {
    if (selectionInfo.keySet().size() > 1) {
      Iterator<ERConnectableObj> iterator = selectionInfo.keySet().iterator();
      ERConnectableObj attrTable = iterator.next();
      if (selectionInfo.get(attrTable).iterator().hasNext()
          && selectionInfo.get(attrTable).iterator().next().getIsPrimary()) {
        attrTable = iterator.next();
      }
      return attrTable;
    } else {
      return selectionInfo.keySet().iterator().next();
    }
  }

  // get the number of attributes in different types
  // e.g. Numerical : 2, Lexical : 1
  public Map<DataType, Integer> getAttrTypeNumbers() throws SQLException {
    int numerical = 0;
    int lexical = 0;
    int temporal = 0;
    Map<DataType, Integer> result = new HashMap<>();

    ERConnectableObj attrTable = getAttrTable();
    List<Attribute> attributeList = selectionInfo.get(attrTable);

    for (Attribute attribute : attributeList) {
      DataType dataType = DataTypeUtil.getDataType(attrTable.getName(), attribute.getName());
      switch (dataType) {
        case NUMERICAL -> numerical++;
        case LEXICAL -> lexical++;
        case TEMPORAL -> temporal++;
      }
    }
    result.put(DataType.NUMERICAL, numerical);
    result.put(DataType.LEXICAL, lexical);
    result.put(DataType.TEMPORAL, temporal);

    return result;
  }

  // check if the data is complete for weak-entity visualisations
  public static boolean isDataComplete(List<Map<String, Object>> queryResults, String strongEntity,
      String weakEntity) {
    Map<String, Set<Object>> map = new HashMap<>();

    // Build a map of country to the set of years
    for (Map<String, Object> result : queryResults) {
      String strong = (String) result.get(strongEntity);
      Object weak = result.get(weakEntity);

      Set<Object> data = map.getOrDefault(strong, new HashSet<>());
      data.add(weak);
      map.put(strong, data);
    }

    for (Set<Object> data : map.values()) {
      if (data.size() != queryResults.size() / map.size()) {
        return false;
      }
    }

    return true; // Data is complete for all countries
  }
}
