package com.example.demo.input.handler;

import com.example.demo.data.types.DataType;
import com.example.demo.data.types.DataTypeUtil;
import com.example.demo.models.ModelType;
import com.example.demo.models.ModelUtil;
import com.example.demo.visualisation.VisualService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERBaseObj;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.RelationshipEdge;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.Cardinality;
import io.github.MigadaTang.common.EntityType;
import io.github.MigadaTang.exception.DBConnectionException;
import io.github.MigadaTang.exception.ParseException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
public class InputController {

  @Autowired
  InputService inputService;

  @PostMapping("/database-info")
  public String processDatabaseInfo(@RequestBody Map<String, String> formData)
      throws DBConnectionException, ParseException, SQLException {
    inputService.initialise(formData);
    return "redirect:/selection";
  }

  @GetMapping("/attr-options")
  @ResponseBody
  public List<Map<String, Object>> getAttrOptions() throws SQLException {
    Schema schema = InputService.getSchema();
    List<Map<String, Object>> tables = new ArrayList<>();
    for (Entity entity : schema.getEntityList()) {
      Map<String, Object> table = new HashMap<>();
      table.put("name", entity.getName());
      List<Map<String, String>> attributes = new LinkedList<>();
      for (Attribute attribute : entity.getAttributeList()) {
        if (attribute.getIsPrimary()) {
          table.put("pKey", attribute.getName());
          continue;
        }
        Map<String, String> attributeInfo = new HashMap<>();
        attributeInfo.put("name", attribute.getName());

        // Perform classification
        DataType dataType = DataTypeUtil.getDataType(entity.getName(), attribute.getName()
        );
        if (dataType == DataType.NUMERICAL || dataType == DataType.TEMPORAL) {
          attributeInfo.put("dataType", "(#)");
          attributes.add(attributeInfo);
        } else {
          attributeInfo.put("dataType", "(Abc)");
          attributes.add(0, attributeInfo);
        }
      }
      // case where user don't have to select any attribute: Hierarchy Tree (one-many)
      if (entity.getEntityType() == EntityType.STRONG && ModelUtil.getParentEntity(entity,
          schema) != null) {
        Map<String, String> none = new HashMap<>();
        none.put("name", "(select none)");
        none.put("dataType", " ");
        attributes.add(none);
      }
      table.put("attributes", attributes);
      tables.add(table);
    }

    for (Relationship relationship : schema.getRelationshipList()) {
      if (relationship.getAttributeList().size() > 0) {
        Map<String, Object> table = new HashMap<>();
        table.put("name", relationship.getName());
        List<Map<String, String>> attributes = new LinkedList<>();
        for (Attribute attribute : relationship.getAttributeList()) {
          if (attribute.getIsPrimary()) {
            table.put("pKey", attribute.getName());
            continue;
          }
          Map<String, String> attributeInfo = new HashMap<>();
          attributeInfo.put("name", attribute.getName());
          DataType dataType = DataTypeUtil.getDataType(relationship.getName(), attribute.getName()
          );
          if (dataType == DataType.NUMERICAL || dataType == DataType.TEMPORAL) {
            attributeInfo.put("dataType", "(#)");
            attributes.add(attributeInfo);
          } else {
            attributeInfo.put("dataType", "(Abc)");
            attributes.add(0, attributeInfo);
          }
        }
        // cases where user don't have to select any attribute: Network Chart
        if (!ModelUtil.getManyManyEntities(relationship).isEmpty()) {
          Map<String, String> none = new HashMap<>();
          none.put("name", "(select none)");
          none.put("dataType", " ");
          attributes.add(none);
        }
        table.put("attributes", attributes);
        tables.add(table);
      }
    }
    return tables;
  }

  @GetMapping("/filter-options")
  @ResponseBody
  public List<Map<String, Object>> getFilterOptions() {
    Schema schema = InputService.getSchema();
    List<Map<String, Object>> tables = new ArrayList<>();

    ERConnectableObj attrTable = inputService.getAttrTable();
    // adding the table itself into the filter condition
    List<ERConnectableObj> relatedTables = new ArrayList<>(List.of(attrTable));
    relatedTables.addAll(ModelUtil.tablesConnectableWith(attrTable, schema));
    for (ERConnectableObj table : relatedTables) {
      Map<String, Object> map = new HashMap<>();
      map.put("name", table.getName());
      if (table instanceof Entity) {
        List<String> attributes = ((Entity) table).getAttributeList().stream()
            .map(ERBaseObj::getName).toList();
        map.put("attributes", attributes);
      } else if (table instanceof Relationship) {
        List<String> attributes = ((Relationship) table).getAttributeList().stream()
            .map(ERBaseObj::getName).toList();
        map.put("attributes", attributes);
      }
      tables.add(map);
    }
    return tables;
  }

  @GetMapping("/vis-options")
  @ResponseBody
  public Map<String, Object> getVisOptions() throws SQLException {
    Map<String, Object> table = new HashMap<>();
    Map<DataType, Integer> attrTypeNumbersMap = inputService.getAttrTypeNumbers();
    int numericalNum = attrTypeNumbersMap.get(DataType.NUMERICAL);
    int lexicalNum = attrTypeNumbersMap.get(DataType.LEXICAL);
    int temporalNum = attrTypeNumbersMap.get(DataType.TEMPORAL);
    List<String> options = new ArrayList<>();
    switch (inputService.getModelType()) {
      case BASIC_ENTITY, ONE_MANY_RELATIONSHIP -> {
        if (temporalNum == 1 && numericalNum <= 1) {
          options.add("Calendar");
          break;
        }
        if (numericalNum == 1 && lexicalNum <= 1) {
          if (lexicalNum == 0) {
            options.add("Bar Chart");
            options.add("Pie Chart");

            // checking if the key is geographical to suggest choropleth map
            Entity entity = (Entity) InputService.getSelectionInfo().keySet().iterator().next();
            String key = entity.getAttributeList().stream().filter(Attribute::getIsPrimary).findFirst()
                .get().getName();
            String query = VisualService.generateSQLQuery(List.of(key), entity.getName(),
                key,
                InputService.getFilterConditions());
            List<Map<String, Object>> queryResults = InputService.getJdbc().queryForList(query);
            if (inputService.containGeographicalData(queryResults, key)) {
              options.add("Choropleth Map");
            }

          }
          options.add("Word Cloud");
        }
        if (numericalNum == 2 && lexicalNum <= 1) {
          options.add("Scatter Diagram");
        }
        if (numericalNum == 3 && lexicalNum <= 1) {
          options.add("Bubble Chart");
        }
        if (inputService.getModelType() == ModelType.ONE_MANY_RELATIONSHIP) {
          // if the user selects no mandatory attributes
          if (inputService.checkSelectNone()) {
            options.add("Hierarchy Tree");
          }
          if (numericalNum == 1 && lexicalNum <= 1){
            options.add("Tree Map");
            options.add("Circle Packing");
          } else if (numericalNum == 0 && lexicalNum == 1) {
            options.add("Hierarchy Tree");
          }
        }
      }
      case WEAK_ENTITY -> {
        if (numericalNum == 1 && lexicalNum == 0) {
          Entity entity = (Entity) InputService.getSelectionInfo().keySet().iterator().next();
          String weakKey = entity.getAttributeList().stream().filter(Attribute::getIsPrimary).findFirst()
              .get().getName();
          String strongEntityFk = VisualService.getForeignKeyName(entity.getName(),
              ModelUtil.getRelatedStrongEntity(entity, InputService.getSchema()).getName());
          List<String> attributes = List.of(
              strongEntityFk, weakKey);
          String query = VisualService.generateSQLQuery(attributes, entity.getName(),
              weakKey,
              InputService.getFilterConditions());
          List<Map<String, Object>> queryResults = InputService.getJdbc().queryForList(query);
          // check if data is complete
          if (InputService.isDataComplete(queryResults, strongEntityFk, weakKey)) {
            options.add("Stacked Bar Chart");
            options.add("Spider Chart");
          }
          options.add("Line Chart");
          options.add("Grouped Bar Chart");
        }
      }
      case MANY_MANY_RELATIONSHIP -> {
        if (inputService.checkSelectNone() && lexicalNum <= 0) {
          options = List.of("Network Chart");
        }
        if (numericalNum == 1 && lexicalNum == 0) {
          options = List.of("Sankey Diagram", "Chord Diagram", "Heatmap");
        }
      }
      case REFLEXIVE_RELATIONSHIP -> {
        if (inputService.checkSelectNone() && lexicalNum <= 0) {
          options = List.of("Network Chart");
        }
        if (numericalNum == 1 && lexicalNum == 0) {
          options = List.of("Chord Diagram", "Heatmap");
        }
      }
      case UNKNOWN -> {

        // find foreign key and get the whole data -> check if one-many -> update
        // return something that indicates the change from many-many to one-many

        Map<ERConnectableObj, List<Attribute>> selectionInfo = InputService.getSelectionInfo();
        Iterator<ERConnectableObj> keyIterator = selectionInfo.keySet()
            .iterator();
        Entity attrTable;
        Entity keyTable;

        ERConnectableObj table1 = keyIterator.next();
        ERConnectableObj table2 = keyIterator.next();

        // classify
        Iterator<Attribute> tableNameIterator = selectionInfo.get(table1).iterator();
        if (tableNameIterator.hasNext() && tableNameIterator
            .next().getIsPrimary()) {
          attrTable = (Entity) table2;
          keyTable = (Entity) table1;
        } else {
          attrTable = (Entity) table1;
          keyTable = (Entity) table2;
        }

        Optional<Attribute> attrTableKey = attrTable.getAttributeList().stream()
            .filter(Attribute::getIsPrimary).findFirst();
        Optional<Attribute> keyTableKey = keyTable.getAttributeList().stream()
            .filter(Attribute::getIsPrimary).findFirst();
        if (attrTableKey.isPresent() && keyTableKey.isPresent()) {
          List<String> attributes = List.of(attrTableKey.get().getName());
          String query = VisualService.generateSQLQuery(attributes, attrTable.getName(),
              attrTableKey.get().getName(), keyTable.getName(), keyTableKey.get().getName(),
              InputService.getFilterConditions());
          List<Map<String, Object>> queryResults = InputService.getJdbc().queryForList(query);
          // got the correct data now, check if the filtered data is one-many
          Set<Object> uniqueCodes = new HashSet<>();
          boolean isOneToMany = true;

          for (Map<String, Object> map : queryResults) {
            Object code = map.get(attrTableKey.get().getName());
            if (uniqueCodes.contains(code)) {
              isOneToMany = false;
              break;
            }
            uniqueCodes.add(code);
          }

          if (isOneToMany) {
            options = List.of("Tree Map", "Hierarchy Tree", "Circle Packing");
          }
        }
      }
    }
    table.put("option", options);
    return table;
  }

  @GetMapping("/error-message")
  @ResponseBody
  public String getNoVisOptionErrorMessage() {
    StringBuilder message = new StringBuilder();
    Map<ERConnectableObj, List<Attribute>> selectionInfo = InputService.getSelectionInfo();
    Iterator<ERConnectableObj> keyIterator = selectionInfo.keySet()
        .iterator();
    ERConnectableObj table1 = keyIterator.next();
    ERConnectableObj table2 = keyIterator.next();

    Relationship relationship = ModelUtil.getRelationshipBetween(table1.getName(), table2.getName(),
        InputService.getSchema());
    boolean isOneMany = false;
    ERConnectableObj childEntity = null;
    if (relationship != null) {
      for (RelationshipEdge edge : relationship.getEdgeList()) {
        if (edge.getCardinality() == Cardinality.OneToOne
            || edge.getCardinality() == Cardinality.ZeroToOne) {
          isOneMany = true;
          childEntity = edge.getConnObj();
          break;
        }
      }
      if (isOneMany) {
        message.append(
            "The relationship is One-Many, please choose attributes from the child entity");
        message.append(": ");
        message.append(childEntity.getName()).append(".");
      } else {
        message.append(
            "The relationship is Many-Many, please choose attributes from the relationship table");
        message.append(": ");
        message.append(relationship.getName());
        message.append(".\n");
        message.append("Or apply filters to make the data One-Many");
      }
    }
    return message.toString();
  }

  @PostMapping("/process-attr-selection")
  @ResponseBody
  public String processAttrSelection(@RequestBody String selectedAttrJson) {
    List<String> selectedAttributes = new Gson().fromJson(selectedAttrJson, List.class);

    Schema schema = InputService.getSchema();

    Map<ERConnectableObj, List<Attribute>> selectionInfo = new HashMap<>();

    for (String attribute : selectedAttributes) {
      List<Attribute> selectedAttrs = new ArrayList<>();
      String[] parts = attribute.split("\\.");
      String tableName = parts[0];
      int hashCode = attribute.hashCode();

      Optional<Entity> entity = schema.getEntityList().stream()
          .filter(e -> e.getName().equals(tableName))
          .findFirst();
      // select table could be an entity or a relationship
      if (entity.isPresent()) {
        // compare hashcode for entity-attr combination, to avoid collision
        for (Attribute attr : entity.get().getAttributeList()) {
          if ((entity.get().getName() + "." + attr.getName()).hashCode() == hashCode) {
            selectedAttrs.add(attr);
            break;
          }
        }
        if (selectionInfo.containsKey(entity.get())) {
          List<Attribute> prevAttrs = selectionInfo.get(entity.get());
          prevAttrs.addAll(selectedAttrs);
          selectionInfo.put(entity.get(), prevAttrs);
        } else {
          selectionInfo.put(entity.get(), selectedAttrs);
        }
      } else {
        Optional<Relationship> relationship = schema.getRelationshipList().stream()
            .filter(e -> e.getName().equals(tableName))
            .findFirst();
        if (relationship.isPresent()) {
          for (Attribute attr : relationship.get().getAttributeList()) {
            if ((relationship.get().getName() + "." + attr.getName()).hashCode() == hashCode) {
              selectedAttrs.add(attr);
              break;
            }
          }
          if (selectionInfo.containsKey(relationship.get())) {
            List<Attribute> prevAttrs = selectionInfo.get(relationship.get());
            prevAttrs.addAll(selectedAttrs);
            selectionInfo.put(relationship.get(), prevAttrs);
          } else {
            selectionInfo.put(relationship.get(), selectedAttrs);
          }
        }
      }
    }
    // selectionInfo is not null for sure
    inputService.patternMatchBasedOnSelection(selectionInfo);
    return selectedAttributes.toString();
  }

  @PostMapping("/filter-click")
  @ResponseBody
  public List<Object> respondFilterClick(@RequestBody String selectedFilterJson)
      throws SQLException {

    String[] parts = selectedFilterJson.split("\\.");
    String tableName = parts[0];
    String attributeName = parts[1].split("=")[0];
    DataType type = DataTypeUtil.getDataType(tableName, attributeName);
    List<Object> result = new ArrayList<>();
    switch (type) {
      case NUMERICAL, TEMPORAL -> result.addAll(inputService.getScalarFilterOptions(tableName,
          attributeName));
      case LEXICAL -> result.addAll(
          inputService.getDiscreteFilterOptions(tableName, attributeName));
    }
    return result;
  }

  @PostMapping("/process-filter")
  @ResponseBody
  public String processFilter(@RequestBody String selectedFilterJson) {
    Type mapType = new TypeToken<Map<String, List<String>>>() {
    }.getType();
    Map<String, List<String>> filterConditions = new Gson().fromJson(selectedFilterJson, mapType);
    inputService.setFilterConditions(filterConditions);
    return selectedFilterJson;
  }

  @PostMapping("/process-vis-selection")
  @ResponseBody
  public String processVisSelection(@RequestBody String selectedVisJson) {
    String formattedString = selectedVisJson.toLowerCase().replace(" ", "_")
        .replace("\"", "");
    return "/" + formattedString;
  }

  @PostMapping("/get-related-tables")
  @ResponseBody
  public String getRelatedEntityTables(@RequestBody String selectedTableURL)
      throws UnsupportedEncodingException {
    String selectedTableJson = URLDecoder.decode(selectedTableURL,
        StandardCharsets.UTF_8.toString());
    List<String> selectedTables = new Gson().fromJson(selectedTableJson.split("=")[0], List.class);

    Schema schema = InputService.getSchema();

    List<Entity> allEntities = new ArrayList<>(schema.getEntityList());

    // the result we are going to return
    List<String> relatedTables = new ArrayList<>();

    for (String tableName : selectedTables) {
      Optional<Entity> table = allEntities.stream().filter(e -> e.getName().equals(tableName))
          .findFirst();
      table.ifPresent(erConnectableObj -> relatedTables.addAll(
          ModelUtil.inRelationshipWith(erConnectableObj, schema).stream()
              .map(ERConnectableObj::getName).toList()));
    }
    return new Gson().toJson(relatedTables);
  }
}
