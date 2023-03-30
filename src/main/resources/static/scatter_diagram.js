import { margin, width, height, createSvg } from "./common.js";
const svg = createSvg();

d3.json("/mondial/main")
  .then(function(data) {

    // Extract attribute names
    var keys = Object.keys(data[0]);

    // Define the scales for the x-axis and y-axis
    var x = d3.scaleLinear()
        .domain([0, d3.max(data, function(d) { return d[keys[1]];})])
        .range([0, width]);

    var y = d3.scaleLinear()
        .domain([0, d3.max(data, function(d) { return d[keys[2]]; })])
        .range([height, 0]);

    // Define the axes for the x-axis and y-axis
    var xAxis = d3.axisBottom(x);

    var yAxis = d3.axisLeft(y);

    // Add the x-axis and y-axis to the SVG element
    svg.append("g")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    svg.append("g")
        .call(yAxis);

    // Add labels to both x-axis and y-axis
    svg.append("text")
        .attr("text-anchor", "middle")
        .attr("x", width / 2)
        .attr("y", height + margin.bottom / 2)
        .text(Object.keys(data[0])[0]);

    svg.append("text")
        .attr("text-anchor", "middle")
        .attr("transform", "rotate(-90)")
        .attr("x", -height / 2)
        .attr("y", -margin.left / 2)
        .text(Object.keys(data[0])[1]);

    // Add the dots to the SVG element
    svg.selectAll(".dot")
        .data(data)
        .enter().append("circle")
        .attr("class", "dot")
        .attr("r", 5)
        .attr("cx", function(d) { return x(d[keys[1]]); })
        .attr("cy", function(d) { return y(d[keys[2]]); })
        .attr("fill", "none")
        .attr("stroke", "steelblue");
  })
  .catch(function(error) {
    console.error(error); // handle errors
  });
