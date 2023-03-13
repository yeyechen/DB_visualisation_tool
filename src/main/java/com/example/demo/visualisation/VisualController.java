package com.example.demo.visualisation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class VisualController {

  @GetMapping("/bar")
  public String bar() {
    return "bar_chart";
  }

}
