function BarChart(data, {
  margin = ({top: 20, right: 0, bottom: 30, left: 100}),
  width = 870,
  height = 500,
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

  function zoom(svg) {
    const extent = [[margin.left, margin.top], [width - margin.right, height - margin.top]];

    svg.call(d3.zoom()
        .scaleExtent([1, 8])
        .translateExtent(extent)
        .extent(extent)
        .on("zoom", zoomed));

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
  d3.select("body").append(() => svg);

  d3.select("#sort").on("change", function() {
    var keys = Object.keys(data[0]); // index: 0->k, 1->a1

    var sortValue = d3.select("#sort").property("value");
    console.log(sortValue);
    var sortOrder = sortValue === "alpha" ? function(a, b) {
      return d3.ascending(a[keys[0]], b[keys[0]]);
    } : sortValue === "asc" ? function(a, b) {
      return d3.ascending(a[keys[1]], b[keys[1]]);
    } : function(a, b) {
      return d3.descending(a[keys[1]], b[keys[1]]);
    };
    data.sort(sortOrder);
    const svg = BarChart(data);

    d3.select("body").select("svg").remove();
    d3.select("body").append(() => svg);
  });

});
