package com.example.demo.visualisation;

import com.example.demo.input.handler.InputService;
import java.sql.SQLException;
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
  public List<Map<String, Object>> lineChartData() throws SQLException {
    return visualService.queryLineChart(InputService.getSelectionInfo());
  }

  @GetMapping("/stacked_bar_chart_data")
  @ResponseBody
  public List<Map<String, Object>> stackedBarChartData() throws SQLException {
    return visualService.queryStackedBarChart(InputService.getSelectionInfo());
  }

  @GetMapping("spider_chart_data")
  @ResponseBody
  public List<Map<String, Object>> spiderChartData() throws SQLException {
    return visualService.querySpiderChart(InputService.getSelectionInfo());
  }

  @GetMapping("tree_map_data")
  @ResponseBody
  public List<Map<String, Object>> treeMapData() throws SQLException {
    return visualService.queryTreeMapData(InputService.getSelectionInfo());
  }

  @GetMapping("hierarchy_tree_data")
  @ResponseBody
  public List<Map<String, Object>> hierarchyTreeData() throws SQLException {
    return visualService.queryHierarchyTreeData(InputService.getSelectionInfo());
  }

  @GetMapping("sankey_diagram_data")
  @ResponseBody
  public List<Map<String, Object>> sankeyDiagramData() throws SQLException {
    return visualService.queryManyManyRelationshipData(InputService.getSelectionInfo());
  }

  @GetMapping("chord_diagram_data")
  @ResponseBody
  public List<Map<String, Object>> chordDiagramData() throws SQLException {
    return visualService.queryManyManyRelationshipData(InputService.getSelectionInfo());
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

  @GetMapping("/spider_chart")
  public String spider() {
    return "spider_chart";
  }

  @GetMapping("/tree_map")
  public String treeMap() {
    return "tree_map";
  }

  @GetMapping("/hierarchy_tree")
  public String hierarchyTree() {
    return "hierarchy_tree";
  }

  @GetMapping("/sankey_diagram")
  public String sankey() {
    return "sankey_diagram";
  }

  @GetMapping("/chord_diagram")
  public String chord() {
    return "chord_diagram";
  }
}
