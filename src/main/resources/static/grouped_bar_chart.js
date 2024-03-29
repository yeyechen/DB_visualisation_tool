// Copyright 2021 Observable, Inc.
// Released under the ISC license.
// https://observablehq.com/@d3/grouped-bar-chart
function GroupedBarChart(data, {
  x = (d, i) => i, // given d in data, returns the (ordinal) x-value
  y = d => d, // given d in data, returns the (quantitative) y-value
  z = () => 1, // given d in data, returns the (categorical) z-value
  title, // given d in data, returns the title text
  marginTop = 30, // top margin, in pixels
  marginRight = 0, // right margin, in pixels
  marginBottom = 30, // bottom margin, in pixels
  marginLeft = 100, // left margin, in pixels
  width = 640, // outer width, in pixels
  height = 400, // outer height, in pixels
  xLabel,
  xDomain, // array of x-values
  xRange = [marginLeft, width - marginRight], // [xmin, xmax]
  xPadding = 0.1, // amount of x-range to reserve to separate groups
  yType = d3.scaleLinear, // type of y-scale
  yDomain, // [ymin, ymax]
  yRange = [height - marginBottom, marginTop], // [ymin, ymax]
  zDomain, // array of z-values
  zPadding = 0.05, // amount of x-range to reserve to separate bars
  yFormat, // a format specifier string for the y-axis
  yLabel, // a label for the y-axis
  colors = d3.schemeTableau10, // array of colors
  entityLabel,
} = {}) {
  // Compute values.
  const X = d3.map(data, x);
  const Y = d3.map(data, y);
  const Z = d3.map(data, z);

  // Compute default domains, and unique the x- and z-domains.
  if (xDomain === undefined) xDomain = X;
  if (yDomain === undefined) yDomain = [0, d3.max(Y)];
  if (zDomain === undefined) zDomain = Z;
  xDomain = new d3.InternSet(xDomain);
  zDomain = new d3.InternSet(zDomain);

  // Omit any data not present in both the x- and z-domain.
  const I = d3.range(X.length).filter(i => xDomain.has(X[i]) && zDomain.has(Z[i]));

  // Construct scales, axes, and formats.
  const xScale = d3.scaleBand(xDomain, xRange).paddingInner(xPadding);
  const xzScale = d3.scaleBand(zDomain, [0, xScale.bandwidth()]).padding(zPadding);
  const yScale = yType(yDomain, yRange);
  const zScale = d3.scaleOrdinal(zDomain, colors);
  const xAxis = d3.axisBottom(xScale).tickSizeOuter(0);
  const yAxis = d3.axisLeft(yScale).ticks(height / 60, yFormat);


  const svg = d3.create("svg")
      .attr("width", width)
      .attr("height", height)
      .attr("viewBox", [0, 0, width, height])
      .attr("style", "max-width: 100%; height: auto; height: intrinsic;");

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

  const bar = svg.append("g")
    .selectAll("rect")
    .data(I)
    .join("rect")
      .attr("x", i => xScale(X[i]) + xzScale(Z[i]))
      .attr("y", i => yScale(Y[i]))
      .attr("width", xzScale.bandwidth())
      .attr("height", i => yScale(0) - yScale(Y[i]))
      .attr("fill", i => zScale(Z[i]));

  if (title) bar.append("title")
      .text(title)
      .attr("font-size", "13px");

  svg.append("g")
      .attr("transform", `translate(0,${height - marginBottom})`)
      .call(xAxis);

  var tooltip = d3.select("#tooltip")
    .append("div")
    .style("opacity", 0)
    .attr("class", "tooltip")
    .style("background-color", "white")
    .style("border", "solid")
    .style("border-radius", "1px")
    .style("padding", "1px");

  svg.selectAll("rect")
    .on("mouseover", function(event, i) {
      const [x, y] = d3.pointer(event, this);
      tooltip.transition()
        .duration(50)
        .style("opacity", .8);
      tooltip.html(entityLabel + ": " + X[i] + "<br>"
        + xLabel + ": " + Z[i] + "<br>"
        + yLabel + ": " + Y[i])
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

  return Object.assign(svg.node(), {scales: {color: zScale}});
}

d3.json("/grouped_bar_chart_data")
  .then(function(data) {

  var keys = Object.keys(data[0]); // index: 0->k1, 1->k2, 2->a1
  let category = new Set();
  for (let i = 1; i < data.length; i++) {
    category.add(data[i][keys[1]])
  }
  category = Array.from(category).sort();

  const svg = GroupedBarChart(data, {
    x: d => d[keys[0]],
    y: d => d[keys[2]],
    z: d => d[keys[1]],
    entityLabel: keys[0],
    xLabel: keys[1],
    yLabel: keys[2],
    xDomain: d3.groupSort(data, D => d3.sum(D, d => -d[keys[2]]), d => d[keys[0]]).slice(0, 6), // top 6
    zDomain: category,
    colors: d3.schemeSpectral[category.length],
    height: 600,
    width: 1000
  })
  key = swatches({
    colour: svg.scales.color
  })
  d3.select("#chart").append(() => key);
  d3.select("#chart").append(() => svg);
})