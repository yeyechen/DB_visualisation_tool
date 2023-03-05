// Load the data from the /mondial/economy endpoint
d3.json("/mondial/economy")
  .then(function(data) {
    // Define the margin, width, and height of the plot
    var margin = {top: 20, right: 20, bottom: 60, left: 60},
        width = 960 - margin.left - margin.right,
        height = 600 - margin.top - margin.bottom;

    // Create the SVG element and append it to the body
    var svg = d3.select("body").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    // Define the scales for the x-axis and y-axis
    var x = d3.scaleLinear()
        .domain([0, d3.max(data, function(d) { return d.inflation; })])
        .range([0, width]);

    var y = d3.scaleLinear()
        .domain([0, d3.max(data, function(d) { return d.unemployment; })])
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
        .attr("cx", function(d) { return x(d.inflation); })
        .attr("cy", function(d) { return y(d.unemployment); })
        .attr("fill", "none")
        .attr("stroke", "steelblue");
  })
  .catch(function(error) {
    console.error(error); // handle errors
  });
