const margin = { top: 20, right: 30, bottom: 30, left: 40 };
const width = 960 - margin.left - margin.right;
const height = 600 - margin.top - margin.bottom;

function createSvg() {
  const svg = d3.select("body").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
  return svg;
}

const svg = createSvg();

var x = d3.scaleLinear().range([0, width]);
var y = d3.scaleLinear().range([height, 0]);
var radius = d3.scaleLinear().range([1, 20]); // set the range of the circle radius


var color = d3.scaleOrdinal(d3.schemeCategory10);


d3.json("/bubble_chart_data")
  .then(function(data) {

    var keys = Object.keys(data[0]);

    x.domain([0.1, d3.max(data, function(d) {return d[keys[1]];})])
    y.domain([0.1, d3.max(data, function(d) { return d[keys[2]]; })]);
    radius.domain([d3.min(data, function(d) { return d[keys[3]]; }), d3.max(data, function(d) { return d[keys[3]]; })]);


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

        if (d[keys[4]] !== null) {
          d3.select(this.parentNode).style("fill", color(d[keys[4]])); //set the color of the parent circle element
        }
      });

    svg.append("g")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x))
      .append("text")
      .attr("class", "axis-label")
      .attr("x", width / 2)
      .attr("y", margin.bottom - 10)
      .attr("fill", "black")
      .attr("text-anchor", "middle")
      .text(keys[1]);

    svg.append("g")
      .call(d3.axisLeft(y))
      .append("text")
      .attr("class", "axis-label")
      .attr("x", -margin.left)
      .attr("y", 10)
      .attr("fill", "black")
      .attr("text-anchor", "start")
      .text(keys[2]);

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

    var tooltip = d3.select("body")
      .append("div")
      .style("opacity", 0)
      .attr("class", "tooltip")
      .style("background-color", "white")
      .style("border", "solid")
      .style("border-width", "2px")
      .style("border-radius", "5px")
      .style("padding", "5px");

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
    console.error(error);
  });
