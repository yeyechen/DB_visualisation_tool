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

  @GetMapping("/scatter_diagram_data")
  @ResponseBody
  public List<Map<String, Object>> scatterDiagramData() {
    return visualService.queryScatterDiagram(InputService.getSelectionInfo());
  }

  @GetMapping("/bubble_chart_data")
  @ResponseBody
  public List<Map<String, Object>> bubbleChartData() {
    return visualService.queryBubbleChart(InputService.getSelectionInfo());
  }

  @GetMapping("/line_chart_data")
  @ResponseBody
  public List<Map<String, Object>> lineChartData() {
    return visualService.queryLineChart(InputService.getSelectionInfo());
  }

  @GetMapping("/stacked_bar_chart_data")
  @ResponseBody
  public List<Map<String, Object>> stackedBarChartData() {
    return visualService.queryStackedBarChart(InputService.getSelectionInfo());
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

  @GetMapping("/bubble_chart")
  public String bubble() {
    return "bubble_chart";
  }

  @GetMapping("/line_chart")
  public String line() {
    return "line_chart";
  }

  @GetMapping("/stacked_bar_chart")
  public String stacked() {
    return "stacked_bar_chart";
  }

}
