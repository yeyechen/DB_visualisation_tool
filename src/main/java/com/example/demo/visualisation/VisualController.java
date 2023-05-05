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
  public List<Map<String, Object>> barChartData() throws SQLException {
    return visualService.queryBarChart(InputService.getSelectionInfo());
  }

  @GetMapping("/scatter_diagram_data")
  @ResponseBody
  public List<Map<String, Object>> scatterDiagramData() throws SQLException {
    return visualService.queryScatterDiagram(InputService.getSelectionInfo());
  }

  @GetMapping("/bubble_chart_data")
  @ResponseBody
  public List<Map<String, Object>> bubbleChartData() throws SQLException {
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

  @GetMapping("/grouped_bar_chart_data")
  @ResponseBody
  public List<Map<String, Object>> groupedBarChartData() throws SQLException {
    return visualService.queryGroupedBarChart(InputService.getSelectionInfo());
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

  @GetMapping("circle_packing_data")
  @ResponseBody
  public List<Map<String, Object>> circlePackingData() throws SQLException {
    return visualService.queryCirclePacking(InputService.getSelectionInfo());
  }

  @GetMapping("sankey_diagram_data")
  @ResponseBody
  public List<Map<String, Object>> sankeyDiagramData() throws SQLException {
    return visualService.querySankeyDiagramData(InputService.getSelectionInfo());
  }

  @GetMapping("network_chart_data")
  @ResponseBody
  public List<Map<String, Object>> networkChartData() throws SQLException {
    return visualService.queryNetworkChartData(InputService.getSelectionInfo());
  }

  @GetMapping("chord_diagram_data")
  @ResponseBody
  public List<Map<String, Object>> chordDiagramData() throws SQLException {
    return visualService.queryChordDiagramData(InputService.getSelectionInfo());
  }

  /*-----------------------------------------*/
  @GetMapping("/input")
  public String databaseInputPage() {
    return "database";
  }

  @GetMapping("/selection")
  public String selectionPage() {
    return "selection";
  }

  @GetMapping("/bar_chart")
  public String barChartPage() {
    return "bar_chart";
  }

  @GetMapping("/scatter_diagram")
  public String scatterDiagramPage() {
    return "scatter_diagram";
  }

  @GetMapping("/bubble_chart")
  public String bubbleChartPage() {
    return "bubble_chart";
  }

  @GetMapping("/line_chart")
  public String lineChartPage() {
    return "line_chart";
  }

  @GetMapping("/stacked_bar_chart")
  public String stackedBarChartPage() {
    return "stacked_bar_chart";
  }

  @GetMapping("/grouped_bar_chart")
  public String groupedBarChartPage() {
    return "grouped_bar_chart";
  }

  @GetMapping("/spider_chart")
  public String spiderChartPage() {
    return "spider_chart";
  }

  @GetMapping("/tree_map")
  public String treeMapPage() {
    return "tree_map";
  }

  @GetMapping("/hierarchy_tree")
  public String hierarchyTreePage() {
    return "hierarchy_tree";
  }

  @GetMapping("/circle_packing")
  public String circlePackingPage() {
    return "circle_packing";
  }

  @GetMapping("/sankey_diagram")
  public String sankeyDiagramPage() {
    return "sankey_diagram";
  }

  @GetMapping("/network_chart")
  public String networkChartPage() {
    return "network_chart";
  }

  @GetMapping("/chord_diagram")
  public String chordDiagramPage() {
    return "chord_diagram";
  }
}
