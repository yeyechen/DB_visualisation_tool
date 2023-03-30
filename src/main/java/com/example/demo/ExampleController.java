package com.example.demo;

import io.github.MigadaTang.ER;
import io.github.MigadaTang.Entity;
import io.github.MigadaTang.Relationship;
import io.github.MigadaTang.Schema;
import io.github.MigadaTang.common.AttributeType;
import io.github.MigadaTang.common.Cardinality;
import io.github.MigadaTang.common.DataType;
import io.github.MigadaTang.exception.ParseException;
import java.sql.SQLException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
public class ExampleController {

  public static final String OUTPUT_PATH = "/Users/Mike_Home/Desktop/output_temp";

  // This is an example of creating "person-department one many relation",
  // using external library created by the other group of students.
  @GetMapping("/er")
  @ResponseBody
  public String exampleER() throws SQLException, ParseException {
    // initialize the in-memory database to store ER schema
    ER.initialize();

    Schema example = ER.createSchema("Person_Department");

    Entity person = example.addEntity("person");
    person.addPrimaryKey("salary_number", DataType.INT);
    person.addAttribute("name", DataType.VARCHAR, AttributeType.Mandatory);
    person.addAttribute("bonus", DataType.DOUBLE, AttributeType.Optional);

    Entity department = example.addEntity("department");
    department.addPrimaryKey("dname", DataType.VARCHAR);

    Relationship worksIn = example.createRelationship("works_in", person, department,
        Cardinality.OneToOne, Cardinality.ZeroToMany);

    // export the ER schema to a JSON format
    String jsonString = example.toJSON();

    // save your ER schema as image
    // example.renderAsImage(String.format(outputImagePath, example.getName()));

    // transform your ER schema to DDL
    String DDL = example.generateSqlStatement();
    return jsonString;
  }
}
