// Set the dimensions of the chart
var margin = {top: 20, right: 100, bottom: 70, left: 40},
    width = 960 - margin.left - margin.right,
    height = 600 - margin.top - margin.bottom;

var svg = d3.select("body").append("svg")
  .attr("width", width + margin.left + margin.right)
  .attr("height", height + margin.top + margin.bottom)
  .append("g")
  .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

// Load the data from the server
d3.json("/mondial/economy3")
  .then(function(data) {

    // Extract the primary key name and attribute name (x-axis data and y-axis data)
    var keys = Object.keys(data[0]);

    // display attribute value in descending order
    data.sort((a, b) => (b[keys[1]] - a[keys[1]]));

    var topN = 10;
    // first n result
    data = data.slice(0, topN);

    var x = d3.scaleBand()
      .range([0, width])
      .padding(0.1)
      .domain(data.map(d => d[keys[0]]));

    var y = d3.scaleLinear()
      .range([height, 0])
      .domain([0, d3.max(data, d => d[keys[1]])]);

    var xAxis = d3.axisBottom(x);

    var yAxis = d3.axisLeft(y)
      .ticks(10)
      .tickFormat(d3.format(".2s"));

    svg.append("g")
      .attr("class", "x-axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis);

    svg.append("g")
      .attr("class", "y-axis")
      .call(yAxis);

    svg.selectAll(".bar")
      .data(data)
      .enter().append("rect")
      .attr("class", "bar")
      .attr("x", d => x(d[keys[0]]))
      .attr("width", x.bandwidth())
      .attr("y", d => y(d[keys[1]]))
      .attr("height", d => height - y(d[keys[1]]))
      .attr("fill", "steelblue");
  });



