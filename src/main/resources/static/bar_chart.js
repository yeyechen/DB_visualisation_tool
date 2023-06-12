function BarChart(data, {
  margin = ({top: 20, right: 0, bottom: 30, left: 100}),
  width = 1300,
  height = 600,
  keys = Object.keys(data[0]), // index: 0->k, 1->a1
  yLabel = keys[1]
} = {}) {

  x = d3.scaleBand()
      .domain(data.map(d => d[keys[0]]))
      .range([margin.left, width - margin.right])
      .padding(0.1);

  y = d3.scaleLinear()
      .domain([0, d3.max(data, d => d[keys[1]])]).nice()
      .range([height - margin.bottom, margin.top]);

  xAxis = g => g
      .attr("transform", `translate(0,${height - margin.bottom})`)
      .call(d3.axisBottom(x).tickSizeOuter(0));

  yAxis = g => g
      .attr("transform", `translate(${margin.left},0)`)
      .call(d3.axisLeft(y))
      .call(g => g.select(".domain").remove())
      .append("text")
        .attr("fill", "black")
        .attr("x", -margin.left)
        .attr("y", margin.top / 2)
        .attr("text-anchor", "start")
        .text(yLabel);

  const svg = d3.create("svg")
      .attr("viewBox", [0, 0, width, height])
      .call(zoom);

  svg.append("g")
      .attr("class", "bars")
      .attr("fill", "steelblue")
    .selectAll("rect")
    .data(data)
    .join("rect")
      .attr("x", d => x(d[keys[0]]))
      .attr("y", d => y(d[keys[1]]))
      .attr("height", d => y(0) - y(d[keys[1]]))
      .attr("width", x.bandwidth());

  svg.append("g")
      .attr("class", "x-axis")
      .call(xAxis);

  svg.append("g")
      .attr("class", "y-axis")
      .call(yAxis);

  var tooltip = d3.select("#tooltip")
    .append("div")
    .style("opacity", 0)
    .attr("class", "tooltip")
    .style("background-color", "white")
    .style("border", "solid")
    .style("border-radius", "1px")
    .style("padding", "1px");

  svg.selectAll("rect")
    .on("mouseover", function(event, d) {
      const [x, y] = d3.pointer(event, this);
      tooltip.transition()
        .duration(50)
        .style("opacity", .8);
      tooltip.html(keys[0] + ": " + d[keys[0]] + "<br>"
        + yLabel + ": " +d[keys[1]] + "<br>")
        .style("transform", `translate(${x + 40}px, ${y + 50}px)`);
    })
    .on("mousemove", function(event, i) {
      const [x, y] = d3.pointer(event, this);
      tooltip
        .style("transform", `translate(${x + 40}px, ${y + 50}px)`);
    })
    .on("mouseout", function(event, d) {
      tooltip.transition()
        .duration(500)
        .style("opacity", 0);
    });

  function zoom(svg) {
    const extent = [[margin.left, margin.top], [width - margin.right, height - margin.top]];

    const zoomBehavior = d3.zoom()
      .scaleExtent([1, 8])
      .translateExtent(extent)
      .extent(extent)
      .on("zoom", zoomed);

    svg.call(zoomBehavior);

    // Programmatically trigger the initial zoom
    if (data.length > 100) {
      var zoomScale = data.length / 100;
      var initialTransform = d3.zoomIdentity.scale(zoomScale);
      svg.call(zoomBehavior.transform, initialTransform);
      x.range([margin.left, (width - margin.right) * zoomScale]);
      svg.selectAll(".bars rect").attr("x", d => x(d[keys[0]])).attr("width", x.bandwidth());
      svg.selectAll(".x-axis").call(xAxis);
    }

    function zoomed(event) {
      x.range([margin.left, width - margin.right].map(d => event.transform.applyX(d)));
      svg.selectAll(".bars rect").attr("x", d => x(d[keys[0]])).attr("width", x.bandwidth());
      svg.selectAll(".x-axis").call(xAxis);
    }
  }

  return svg.node();
}

d3.json("/bar_chart_data")
  .then(function(data) {

  const svg = BarChart(data);
  d3.select("#chart").append(() => svg);

  d3.select("#sort").on("change", function() {
    var keys = Object.keys(data[0]); // index: 0->k, 1->a1

    var sortValue = d3.select("#sort").property("value");
    var sortOrder = sortValue === "alpha" ? function(a, b) {
      return d3.ascending(a[keys[0]], b[keys[0]]);
    } : sortValue === "asc" ? function(a, b) {
      return d3.ascending(a[keys[1]], b[keys[1]]);
    } : function(a, b) {
      return d3.descending(a[keys[1]], b[keys[1]]);
    };
    data.sort(sortOrder);
    const svg = BarChart(data);

    d3.select("#chart").select("svg").remove();
    d3.select("#chart").append(() => svg);
  });

});
