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

@Controller
@RequestMapping()
public class ExampleController {

  public static final String OUTPUT_PATH = "/Users/Mike_Home/Desktop/output_temp";

  @GetMapping("/d3")
  public String d3() {
    return "example";
  }

  // This is an example of creating vanilla ER schema, export to JSON, image, using external library
  // created by the other group of students.
  @GetMapping("/er")
  public String er() throws SQLException, ParseException {
    // initialize the in-memory database to store ER schema
    ER.initialize();
    // you could also specify your own database
    // ER.initialize(RDBMSType.POSTGRESQL, "hostname", "port", "database", "user", "password");

    Schema example = ER.createSchema("Vanilla");

    Entity branch = example.addEntity("branch");
    branch.addPrimaryKey("sortcode", DataType.INT);
    branch.addAttribute("bname", DataType.VARCHAR, AttributeType.Mandatory);
    branch.addAttribute("cash", DataType.DOUBLE, AttributeType.Mandatory);

    Entity account = example.addEntity("account");
    account.addPrimaryKey("no", DataType.INT);
    account.addAttribute("type", DataType.CHAR, AttributeType.Mandatory);
    account.addAttribute("cname", DataType.VARCHAR, AttributeType.Mandatory);
    account.addAttribute("rate", DataType.DOUBLE, AttributeType.Mandatory);

    Entity movement = example.addEntity("movement");
    movement.addPrimaryKey("mid", DataType.INT);
    movement.addAttribute("amount", DataType.DOUBLE, AttributeType.Mandatory);
    movement.addAttribute("tdate", DataType.DATETIME, AttributeType.Mandatory);

    Relationship holds = example.createRelationship("holds", account, branch, Cardinality.OneToOne, Cardinality.ZeroToMany);
    Relationship has = example.createRelationship("has", account, movement, Cardinality.ZeroToMany, Cardinality.OneToOne);

    // export the ER schema to a JSON format
    String result = example.toJSON();

    // save your ER schema as image
    // example.renderAsImage(String.format(OUTPUT_PATH, example.getName()));
    return result;
  }
}
