function Scatterplot(data, {
  x = ([x]) => x, // given d in data, returns the (quantitative) x-value
  y = ([, y]) => y, // given d in data, returns the (quantitative) y-value
  r = 3, // (fixed) radius of dots, in pixels
  title, // given d in data, returns the title
  name,
  entityLabel,
  marginTop = 20, // top margin, in pixels
  marginRight = 30, // right margin, in pixels
  marginBottom = 30, // bottom margin, in pixels
  marginLeft = 100, // left margin, in pixels
  height = 700,
  width = 1000,
  inset = r * 2, // inset the default range, in pixels
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
  const C = d3.map(data, color);
  const NAME = d3.map(data, name);
  const T = title == null ? null : d3.map(data, title);
  const I = d3.range(X.length).filter(i => !isNaN(X[i]) && !isNaN(Y[i]));

  // Compute default domains.
  if (xDomain === undefined) xDomain = d3.extent(X);
  if (yDomain === undefined) yDomain = d3.extent(Y);

  // Construct scales and axes.
  const xScale = xType(xDomain, xRange);
  const yScale = yType(yDomain, yRange);
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
          .attr("y", 13)
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
      .attr("r", r)
      .attr("stroke", i => colorScale ? colorScale(C[i]): stroke);

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
        + yLabel + ": " +Y[i] + "<br>")
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
d3.json("/scatter_diagram_data")
  .then(function(data) {
  console.log(data);
  var keys = Object.keys(data[0]); // index: 0->k, 1->a1, 2->a2, 3->optional

  const optionalSet = new Set(data.map(item => item[keys[3]]));
  const colorScale = d3.scaleOrdinal()
    .domain(optionalSet)
    .range(d3.schemeCategory10);
  console.log(optionalSet);
  const svg = Scatterplot(data, {
    x: d => d[keys[1]],
    y: d => d[keys[2]],
    xLabel: keys[1],
    yLabel: keys[2],
    name: d => d[keys[0]],
    entityLabel: keys[0],
    stroke: "steelblue",
    colorScale: optionalSet.size === 1 ? null : colorScale,
    color: d => d[keys[3]]
  })
  if (optionalSet.size != 1) {
    key = swatches({
      colour: colorScale
    })
    d3.select("#chart").append(() => key);
  }
  d3.select("#chart").append(() => svg);
})
