import { margin, width, height, createSvg } from "./common.js";
const svg = createSvg();
// Define the scales for the x-axis, y-axis, and circle size
var x = d3.scaleLog().range([0, width]);
var y = d3.scaleLog().range([height, 0]);
var radius = d3.scaleLinear().range([1, 20]); // set the range of the circle radius

// Define the color scale for the circles
var color = d3.scaleOrdinal(d3.schemeCategory10);

// Load the data from the server
d3.json("/bubble_chart_data")
  .then(function(data) {

    var keys = Object.keys(data[0]);

    // Set the domain of the scales based on the data
    x.domain([0.1, d3.max(data, function(d) {return d[keys[1]];})])
    y.domain([0.1, d3.max(data, function(d) { return d[keys[2]]; })]);
    radius.domain([d3.min(data, function(d) { return d[keys[3]]; }), d3.max(data, function(d) { return d[keys[3]]; })]);

    // Draw the circles on the chart
    svg.selectAll("circle")
      .data(data)
      .enter().append("circle")
      .attr("cx", function(d) { return x(d[keys[1]]); })
      .attr("cy", function(d) { return y(d[keys[2]]); })
      .attr("r", function(d) { return radius(d[keys[3]]); })
      .style("opacity", 0.5)
      .append("title")
      .text(function(d) {return keys[0] + ": " + d[keys[0]] + "\n" + keys[3] +": "+ d[keys[3]];}) // corrected syntax
      .each(function(d) {
        // Use an if statement to set the circle color only if d[keys[4]] is not null
        if (d[keys[4]] !== null) {
          d3.select(this.parentNode).style("fill", color(d[keys[4]])); //set the color of the parent circle element
        }
      });

    // Add the x-axis to the chart
    svg.append("g")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x));

    // Add the y-axis to the chart
    svg.append("g")
      .call(d3.axisLeft(y));

    // Add a legend for the circle color
    var legend = svg.selectAll(".legend")
      .data(color.domain())
      .enter().append("g")
      .attr("class", "legend")
      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

    legend.append("circle")
      .attr("cx", width - 18)
      .attr("r", 8)
      .style("fill", color);

    legend.append("text")
      .attr("x", width - 24)
      .attr("y", 4)
      .attr("dy", ".35em")
      .style("text-anchor", "end")
      .text(function(d) { return d; });

    // Create a tooltip div that is hidden by default:
    var tooltip = d3.select("body")
      .append("div")
      .style("opacity", 0)
      .attr("class", "tooltip")
      .style("background-color", "white")
      .style("border", "solid")
      .style("border-width", "2px")
      .style("border-radius", "5px")
      .style("padding", "5px");

    // Event listeners for mouseover and mouseout to show and hide the tooltip.
    svg.selectAll("circle")
      .on("mouseover", function(event, d) {
        tooltip.transition()
          .duration(200)
          .style("opacity", .9);
        tooltip.html(keys[0] + ": " + d[keys[0]] + "<br> +"+ keys[3] + ": " + d[keys[3]])
          .style("left", (event.pageX + 10) + "px")
          .style("top", (event.pageY - 28) + "px");
      })
      .on("mouseout", function(event, d) {
        tooltip.transition()
          .duration(500)
          .style("opacity", 0);
      });
})
  .catch(function(error) {
    console.error(error); // handle errors
  });
