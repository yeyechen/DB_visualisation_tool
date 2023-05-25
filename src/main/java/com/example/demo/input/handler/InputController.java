package com.example.demo.input.handler;

import com.example.demo.data.types.DataType;
import com.example.demo.data.types.DataTypeUtil;
import com.example.demo.models.ModelUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  public List<Map<String, Object>> getAttrOptions() {
    Schema schema = InputService.getSchema();
    List<Map<String, Object>> tables = new ArrayList<>();
    for (Entity entity : schema.getEntityList()) {
      Map<String, Object> table = new HashMap<>();
      table.put("name", entity.getName());
      table.put("pKey", entity.getAttributeList().stream().filter(Attribute::getIsPrimary)
          .map(ERBaseObj::getName));
      List<String> attributes = new ArrayList<>(
          entity.getAttributeList().stream().filter(a -> !a.getIsPrimary())
              .map(ERBaseObj::getName).toList());
      // case where user don't have to select any attribute: Hierarchy Tree (one-many)
      if (entity.getEntityType() == EntityType.STRONG && ModelUtil.getParentEntity(entity,
          schema) != null) {
        attributes.add("(select none)");
      }
      table.put("attributes", attributes);
      tables.add(table);
    }
    for (Relationship relationship : schema.getRelationshipList()) {
      if (relationship.getAttributeList().size() > 0) {
        Map<String, Object> table = new HashMap<>();
        table.put("name", relationship.getName());
        table.put("pKey", relationship.getAttributeList().stream().filter(Attribute::getIsPrimary)
            .map(ERBaseObj::getName));
        List<String> attributes = new ArrayList<>(
            relationship.getAttributeList().stream().filter(a -> !a.getIsPrimary())
                .map(ERBaseObj::getName).toList());
        // cases where user don't have to select any attribute: Network Chart, Chord Diagram (reflexive)
        if (ModelUtil.getManyManyEntities(relationship).size() == 1) {
          attributes.add("(select none)");
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
    ERConnectableObj tableObject = InputService.getSelectionInfo().keySet().iterator().next();
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
  public Map<String, Object> getVisOptions() {
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
        //todo: handel UNKNOWN case
        ERConnectableObj obj = InputService.getSelectionInfo().keySet().iterator().next();
        // handle Subset case, treat Subset the same as Basic Entity, with extra pk from the main entity
        if (obj instanceof Entity && ((Entity) obj).getEntityType() == EntityType.SUBSET) {
          options = List.of("Bar Chart", "Calendar", "Scatter Diagram",
              "Bubble Chart", "Choropleth Map", "Word Cloud");
          table.put("option", options);
          try {
            ((Entity) obj).addPrimaryKey(((Entity) obj).getBelongStrongEntity().getName(),
                io.github.MigadaTang.common.DataType.TEXT);
          } catch (Exception ignored) {
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

    Schema schema = InputService.getSchema();

    Map<ERConnectableObj, List<Attribute>> selectionInfo = new HashMap<>();
    List<Attribute> selectedAttrs = new ArrayList<>();

    for (String attribute : selectedAttributes) {
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
    String selectedTableJson = URLDecoder.decode(selectedTableURL, StandardCharsets.UTF_8.toString());
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
