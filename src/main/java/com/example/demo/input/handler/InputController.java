package com.example.demo.input.handler;

import com.google.gson.Gson;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERBaseObj;
import io.github.MigadaTang.ERConnectableObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.DataType;
import io.github.MigadaTang.common.EntityType;
import io.github.MigadaTang.exception.DBConnectionException;
import io.github.MigadaTang.exception.ParseException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
      table.put("attributes", entity.getAttributeList().stream().filter(a -> !a.getIsPrimary())
          .map(ERBaseObj::getName));
      tables.add(table);
    }
    for (Relationship relationship : schema.getRelationshipList()) {
      if (relationship.getAttributeList().size() > 0) {
        Map<String, Object> table = new HashMap<>();
        table.put("name", relationship.getName());
        table.put("pKey", relationship.getAttributeList().stream().filter(Attribute::getIsPrimary)
            .map(ERBaseObj::getName));
        table.put("attributes",
            relationship.getAttributeList().stream().filter(a -> !a.getIsPrimary())
                .map(ERBaseObj::getName));
        tables.add(table);
      }
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
        options = Arrays.asList("Bar Chart", "Calendar", "Scatter Diagram",
            "Bubble Chart", "Choropleth Map", "Word Cloud");
        table.put("option", options);
      }
      case WEAK_ENTITY -> {
        options = Arrays.asList("Line Chart", "Stacked Bar Chart", "Grouped Bar Chart",
            "Spider Chart");
        table.put("option", options);
      }
      case ONE_MANY_RELATIONSHIP -> {
        options = Arrays.asList("Tree Map", "Hierarchy Tree", "Circle Packing");
        table.put("option", options);
      }
      case MANY_MANY_RELATIONSHIP -> {
        options = List.of("Sankey Diagram");
        table.put("option", options);
      }
      case REFLEXIVE_RELATIONSHIP -> {
        options = List.of("Chord Diagram");
        table.put("option", options);
      }
      case UNKNOWN -> {
        //todo: handel UNKNOWN case
        ERConnectableObj obj = InputService.getSelectionInfo().keySet().iterator().next();
        // handle Subset case, treat Subset the same as Basic Entity, with extra pk from the main entity
        if (obj instanceof Entity && ((Entity) obj).getEntityType() == EntityType.SUBSET) {
          options = Arrays.asList("Bar Chart", "Calendar", "Scatter Diagram",
              "Bubble Chart");
          table.put("option", options);

          ((Entity) obj).addPrimaryKey(((Entity) obj).getBelongStrongEntity().getName(),
              DataType.TEXT);
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

  @PostMapping("/process-vis-selection")
  @ResponseBody
  public String processVisSelection(@RequestBody String selectedVisJson) {
    String formattedString = selectedVisJson.toLowerCase().replace(" ", "_")
        .replace("\"", "");
    return "/" + formattedString;
  }
}
