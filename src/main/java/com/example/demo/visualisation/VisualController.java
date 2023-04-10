package com.example.demo.visualisation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class VisualController {

  @GetMapping("/input")
  public String databaseInfo() {
    return "database";
  }

  @GetMapping("/selection")
  public String select() {
    return "selection";
  }

  @GetMapping("/bar")
  public String bar() {
    return "bar_chart";
  }

  @GetMapping("/scatter")
  public String scatter() {
    return "scatter_diagram";
  }
}
