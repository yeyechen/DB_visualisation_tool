function BubbleChart(data, {
  x = ([x]) => x, // given d in data, returns the (quantitative) x-value
  y = ([, y]) => y, // given d in data, returns the (quantitative) y-value
  z = ([,,z]) => z,
  name,
  title, // given d in data, returns the title
  marginTop = 20, // top margin, in pixels
  marginRight = 30, // right margin, in pixels
  marginBottom = 30, // bottom margin, in pixels
  marginLeft = 100, // left margin, in pixels
  height = 700,
  width = 1000,
  inset = 5, // inset the default range, in pixels
  insetTop = inset, // inset the default y-range
  insetRight = inset, // inset the default x-range
  insetBottom = inset, // inset the default y-range
  insetLeft = inset, // inset the default x-range
  xType = d3.scaleLinear, // type of x-scale
  xDomain, // [xmin, xmax]
  xRange = [marginLeft + insetLeft, width - marginRight - insetRight], // [left, right]
  yType = d3.scaleLinear, // type of y-scale
  yDomain, // [ymin, ymax]
  yRange = [height - marginBottom - insetBottom, marginTop + insetTop], // [bottom, top]
  xLabel, // a label for the x-axis
  yLabel, // a label for the y-axis
  zLabel,
  entityLabel,
  xFormat, // a format specifier string for the x-axis
  yFormat, // a format specifier string for the y-axis
  fill = "none", // fill color for dots
  stroke = "currentColor", // stroke color for the dots
  strokeWidth = 1.5, // stroke width for dots
  halo = "#fff", // color of label halo
  haloWidth = 3, // padding around the labels
  colorScale,
  color,
} = {}) {
  // Compute values.
  const X = d3.map(data, x);
  const Y = d3.map(data, y);
  const Z = d3.map(data, z);
  const NAME = d3.map(data, name);
  const C = d3.map(data, color);
  const T = title == null ? null : d3.map(data, title);
  const I = d3.range(X.length).filter(i => !isNaN(X[i]) && !isNaN(Y[i]));

  // Compute default domains.
  if (xDomain === undefined) xDomain = d3.extent(X);
  if (yDomain === undefined) yDomain = d3.extent(Y);

  // Construct scales and axes.
  const xScale = xType(xDomain, xRange);
  const yScale = yType(yDomain, yRange);
  const zScale = d3.scaleLinear().range([1, 20]); // set the range of the circle radius
  zScale.domain([d3.min(data, z), d3.max(data, z)]);

  const xAxis = d3.axisBottom(xScale).ticks(width / 80, xFormat);
  const yAxis = d3.axisLeft(yScale).ticks(height / 50, yFormat);

  const svg = d3.create("svg")
      .attr("width", width)
      .attr("height", height)
      .attr("viewBox", [0, 0, width, height])
      .attr("style", "max-width: 100%; height: auto; height: intrinsic;");

  svg.append("g")
      .attr("transform", `translate(0,${height - marginBottom})`)
      .call(xAxis)
      .call(g => g.select(".domain").remove())
      .call(g => g.selectAll(".tick line").clone()
          .attr("y2", marginTop + marginBottom - height)
          .attr("stroke-opacity", 0.1))
      .call(g => g.append("text")
          .attr("x", width)
          .attr("y", marginBottom - 4)
          .attr("fill", "currentColor")
          .attr("text-anchor", "end")
          .text(xLabel)
          .attr("font-size", "13px"));

  svg.append("g")
      .attr("transform", `translate(${marginLeft},0)`)
      .call(yAxis)
      .call(g => g.select(".domain").remove())
      .call(g => g.selectAll(".tick line").clone()
          .attr("x2", width - marginLeft - marginRight)
          .attr("stroke-opacity", 0.1))
      .call(g => g.append("text")
          .attr("x", -marginLeft/2)
          .attr("y", 10)
          .attr("fill", "currentColor")
          .attr("text-anchor", "start")
          .text(yLabel)
          .attr("font-size", "13px"));

  if (T) svg.append("g")
      .attr("font-family", "sans-serif")
      .attr("font-size", 10)
      .attr("stroke-linejoin", "round")
      .attr("stroke-linecap", "round")
    .selectAll("text")
    .data(I)
    .join("text")
      .attr("dx", 7)
      .attr("dy", "0.35em")
      .attr("x", i => xScale(X[i]))
      .attr("y", i => yScale(Y[i]))
      .text(i => T[i])
      .call(text => text.clone(true))
      .attr("fill", "none")
      .attr("stroke", halo)
      .attr("stroke-width", haloWidth);

  svg.append("g")
      .attr("fill", fill)
      .attr("stroke-width", strokeWidth)
    .selectAll("circle")
    .data(I)
    .join("circle")
      .attr("cx", i => xScale(X[i]))
      .attr("cy", i => yScale(Y[i]))
      .attr("r", i => zScale(Z[i]))
      .attr("fill", i => colorScale ? colorScale(C[i]): stroke)
      .attr("opacity", 0.5);

  var tooltip = d3.select("#tooltip")
    .append("div")
    .style("opacity", 0)
    .attr("class", "tooltip")
    .style("background-color", "white")
    .style("border", "solid")
    .style("border-radius", "1px")
    .style("padding", "1px");

  svg.selectAll("circle")
    .on("mouseover", function(event, i) {
      const [x, y] = d3.pointer(event, this);
      tooltip.transition()
        .duration(50)
        .style("opacity", .8);
      tooltip.html(entityLabel + ": " + NAME[i] + "<br>"
        + xLabel + ": " +X[i] + "<br>"
        + yLabel + ": " +Y[i] + "<br>"
        + zLabel + ": " +Z[i])
        .style("transform", `translate(${x + 10}px, ${y + 10}px)`);
    })
    .on("mousemove", function(event, i) {
      const [x, y] = d3.pointer(event, this);
      tooltip
        .style("transform", `translate(${x + 10}px, ${y + 10}px)`);
    })
    .on("mouseout", function(event, d) {
      tooltip.transition()
        .duration(500)
        .style("opacity", 0);
    });

  return svg.node();
}
d3.json("/bubble_chart_data")
  .then(function(data) {
  var keys = Object.keys(data[0]); // index: 0->k, 1->a1, 2->a2, 3->a3, 4->optional
  d3.select("#x1").text(keys[1]);
  d3.select("#x2").text(keys[2]);
  d3.select("#x3").text(keys[3]);
  d3.select("#y1").text(keys[1]);
  d3.select("#y2").text(keys[2]);
  d3.select("#y3").text(keys[3]);
  d3.select("#z1").text(keys[1]);
  d3.select("#z2").text(keys[2]);
  d3.select("#z3").text(keys[3]);

  const optionalSet = new Set(data.map(item => item[keys[4]]));
  const orderedArray = Array.from(optionalSet).sort();
  const colorScale = d3.scaleOrdinal()
    .domain(orderedArray)
    .range(d3.schemeCategory10);

  const svg = BubbleChart(data, {
    x: d => d[keys[1]],
    y: d => d[keys[2]],
    z: d => d[keys[3]],
    name: d => d[keys[0]],
    xLabel: keys[1],
    yLabel: keys[2],
    zLabel: keys[3],
    entityLabel: keys[0],
    stroke: "steelblue",
    colorScale: optionalSet.size === 1 ? null : colorScale,
    color: d => d[keys[4]]
  })

  if (optionalSet.size != 1) {
    key = swatches({
      colour: colorScale
    })
    d3.select("#chart").append(() => key);
  }
  d3.select("#chart").append(() => svg);

  var xAxis = d3.select("#x-axis");
  var yAxis = d3.select("#y-axis");
  var zAxis = d3.select("#z-axis");

  xAxis.on("change", handleAxisChange);
  yAxis.on("change", handleAxisChange);
  zAxis.on("change", handleAxisChange);

  function handleAxisChange() {
    var xAxis = d3.select("#x-axis").property("value") == "1" ? function(d) {return d[keys[1]];}
      : d3.select("#x-axis").property("value") == "2" ? function(d) {return d[keys[2]];}
      : function(d) {return d[keys[3]];};
    var xLabel = d3.select("#x-axis").property("value") == "1" ? keys[1]
      : d3.select("#x-axis").property("value") == "2" ? keys[2]
      : keys[3];
    var yAxis = d3.select("#y-axis").property("value") == "1" ? function(d) {return d[keys[1]];}
      : d3.select("#y-axis").property("value") == "2" ? function(d) {return d[keys[2]];}
      : function(d) {return d[keys[3]];};
    var yLabel = d3.select("#y-axis").property("value") == "1" ? keys[1]
      : d3.select("#y-axis").property("value") == "2" ? keys[2]
      : keys[3];
    var zAxis = d3.select("#z-axis").property("value") == "1" ? function(d) {return d[keys[1]];}
      : d3.select("#y-axis").property("value") == "2" ? function(d) {return d[keys[2]];}
      : function(d) {return d[keys[3]];};
    var zLabel = d3.select("#z-axis").property("value") == "1" ? keys[1]
      : d3.select("#y-axis").property("value") == "2" ? keys[2]
      : keys[3];

    d3.select("#chart").selectAll("*").remove();
    d3.select("#chart").append("div").attr("id", "tooltip");

    var chart = BubbleChart(data, {
      x: xAxis,
      y: yAxis,
      z: zAxis,
      name: d => d[keys[0]],
      xLabel: xLabel,
      yLabel: yLabel,
      zLabel: zLabel,
      entityLabel: keys[0],
      stroke: "steelblue",
      colorScale: optionalSet.size === 1 ? null : colorScale,
      color: d => d[keys[4]]
    })
    if (optionalSet.size != 1) {
      key = swatches({
        colour: colorScale
      })
      d3.select("#chart").append(() => key);
    }

    d3.select("#chart").append(() => chart);

  }
})
