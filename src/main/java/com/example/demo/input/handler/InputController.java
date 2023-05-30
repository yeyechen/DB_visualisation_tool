package com.example.demo.input.handler;

import com.example.demo.data.types.DataType;
import com.example.demo.data.types.DataTypeUtil;
import com.example.demo.models.ModelUtil;
import com.example.demo.visualisation.VisualService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERBaseObj;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.Schema;
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

        // Perform your classification logic here
        DataType dataType = DataTypeUtil.getDataType(entity.getName(), attribute.getName(),
            InputService.getJdbc());
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
          DataType dataType = DataTypeUtil.getDataType(relationship.getName(), attribute.getName(),
              InputService.getJdbc());
          if (dataType == DataType.NUMERICAL || dataType == DataType.TEMPORAL) {
            attributeInfo.put("dataType", "(#)");
            attributes.add(attributeInfo);
          } else {
            attributeInfo.put("dataType", "(Abc)");
            attributes.add(0, attributeInfo);
          }
        }
        // cases where user don't have to select any attribute: Network Chart, Chord Diagram (reflexive)
        if (ModelUtil.getManyManyEntities(relationship).size() == 1) {
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

    Iterator<ERConnectableObj> iterator = InputService.getSelectionInfo().keySet().iterator();
    ERConnectableObj tableObject = iterator.next();
    if (InputService.getSelectionInfo().get(tableObject).iterator().hasNext()
        && InputService.getSelectionInfo().get(tableObject).iterator().next().getIsPrimary()) {
      tableObject = iterator.next();
    }
    // adding the table itself into the filter condition
    List<ERConnectableObj> relatedTables = new ArrayList<>(List.of(tableObject));
    relatedTables.addAll(ModelUtil.tablesConnectableWith(tableObject, schema));
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
    List<String> options;
    switch (inputService.getModelType()) {
      case BASIC_ENTITY -> {
        options = List.of("Bar Chart", "Pie Chart", "Calendar", "Scatter Diagram",
            "Bubble Chart", "Choropleth Map", "Word Cloud");
        table.put("option", options);
      }
      case WEAK_ENTITY -> {
        options = List.of("Line Chart", "Stacked Bar Chart", "Grouped Bar Chart",
            "Spider Chart");
        table.put("option", options);
      }
      case ONE_MANY_RELATIONSHIP -> {
        // if the user selects no mandatory attributes
        if (inputService.checkSelectNone()) {
          options = List.of("Hierarchy Tree");
        } else {
          options = List.of("Tree Map", "Hierarchy Tree", "Circle Packing");
        }
        table.put("option", options);
      }
      case MANY_MANY_RELATIONSHIP -> {
        options = List.of("Sankey Diagram");
        table.put("option", options);
      }
      case REFLEXIVE_RELATIONSHIP -> {
        if (inputService.checkSelectNone()) {
          options = List.of("Network Chart");
        } else {
          options = List.of("Sankey Diagram", "Network Chart", "Chord Diagram", "Heatmap");
        }
        table.put("option", options);
      }
      case UNKNOWN -> {
        // todo: handel UNKNOWN case

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
            table.put("option", options);
          }
        }
      }
    }
    return table;
  }

  @PostMapping("/process-attr-selection")
  @ResponseBody
  public String processAttrSelection(@RequestBody String selectedAttrJson) {
    List<String> selectedAttributes = new Gson().fromJson(selectedAttrJson, List.class);
    System.out.println(selectedAttributes);

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
        selectionInfo.put(entity.get(), selectedAttrs);
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
          selectionInfo.put(relationship.get(), selectedAttrs);
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
    DataType type = DataTypeUtil.getDataType(tableName, attributeName, InputService.getJdbc());
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
    inputService.setFilterCondisions(filterConditions);
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
