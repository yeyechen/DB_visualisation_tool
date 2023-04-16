package com.example.demo.visualisation;

import com.example.demo.input.handler.InputService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
public class VisualController {

  @Autowired
  VisualService visualService;

  @GetMapping("/bar_chart_data")
  @ResponseBody
  public List<Map<String, Object>> barChartData() {
    return visualService.queryBarChart(InputService.getSelectionInfo());
  }

  /*-----------------------------------------*/
  @GetMapping("/input")
  public String databaseInfo() {
    return "database";
  }

  @GetMapping("/selection")
  public String select() {
    return "selection";
  }

  @GetMapping("/bar_chart")
  public String bar() {
    return "bar_chart";
  }

  @GetMapping("/scatter_diagram")
  public String scatter() {
    return "scatter_diagram";
  }
}
