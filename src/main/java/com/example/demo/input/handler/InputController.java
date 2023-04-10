package com.example.demo.input.handler;

import com.google.gson.Gson;
import io.github.MigadaTang.Attribute;
import io.github.MigadaTang.ERBaseObj;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.exception.DBConnectionException;
import io.github.MigadaTang.exception.ParseException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping
public class InputController {

  @Autowired
  InputService service;

  @PostMapping("/database-info")
  public RedirectView processDatabaseInfo(@RequestBody Map<String, String> formData)
      throws DBConnectionException, ParseException, SQLException {
    service.initialiseSchema(formData);
    // todo: fails to redirect the page
    return new RedirectView("/selection");
  }

  @GetMapping("/options")
  public List<Map<String, Object>> getOptions() {
    Schema schema = service.getSchema();

    List<Map<String, Object>> tables = new ArrayList<>();
    for (Entity entity : schema.getEntityList()) {
      Map<String, Object> table = new HashMap<>();
      table.put("name", entity.getName());
      table.put("attributes", entity.getAttributeList().stream().map(ERBaseObj::getName));
      tables.add(table);
    }
    return tables;
  }

  @PostMapping("/selection")
  @ResponseBody
  public String submitSelection(@RequestParam("attributes") String selectedAttributesJSON) {
    List<String> selectedAttributes = new Gson().fromJson(selectedAttributesJSON, List.class);

    Schema schema = service.getSchema();

    Map<Entity, List<Attribute>> selectionInfo = new HashMap<>();
    List<Attribute> selectedAttrs = new ArrayList<>();

    for (String attribute : selectedAttributes) {
      String[] parts = attribute.split("\\.");
      String entityName = parts[0];

      Optional<Entity> entity = schema.getEntityList().stream()
          .filter(e -> e.getName().equals(entityName))
          .findFirst();
      if (entity.isPresent()) {
        // compare hashcode for entity-attr combination, to avoid collision
        int hashCode = attribute.hashCode();
        for (Attribute attr : entity.get().getAttributeList()) {
          if ((entity.get().getName() + "." + attr.getName()).hashCode() == hashCode) {
            selectedAttrs.add(attr);
            break;
          }
        }
        selectionInfo.put(entity.get(), selectedAttrs);
      }
    }

    return selectedAttributes.toString();
  }
}
