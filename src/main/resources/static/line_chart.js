function LineChart(data, {
  x = ([x]) => x, // given d in data, returns the (temporal) x-value
  y = ([, y]) => y, // given d in data, returns the (quantitative) y-value
  z = () => 1, // given d in data, returns the (categorical) z-value
  title, // given d in data, returns the title text
  defined, // for gaps in data
  curve = d3.curveLinear, // method of interpolation between points
  marginTop = 20, // top margin, in pixels
  marginRight = 30, // right margin, in pixels
  marginBottom = 30, // bottom margin, in pixels
  marginLeft = 100, // left margin, in pixels
  width = 900, // outer width, in pixels
  height = 400, // outer height, in pixels
  xType = d3.scaleLinear, // type of x-scale
  xDomain, // [xmin, xmax]
  xRange = [marginLeft, width - marginRight], // [left, right]
  yType = d3.scaleLinear, // type of y-scale
  yDomain, // [ymin, ymax]
  yRange = [height - marginBottom, marginTop], // [bottom, top]
  yFormat, // a format specifier string for the y-axis
  yLabel, // a label for the y-axis
  zDomain, // array of z-values
  color = "currentColor", // stroke color of line, as a constant or a function of *z*
  strokeLinecap, // stroke line cap of line
  strokeLinejoin, // stroke line join of line
  strokeWidth = 1.5, // stroke width of line
  strokeOpacity, // stroke opacity of line
  mixBlendMode = "multiply", // blend mode of lines
  voronoi, // show a Voronoi overlay? (for debugging)
  colorScale,
} = {}) {
  // Compute values.
  const X = d3.map(data, x);
  const Y = d3.map(data, y);
  const Z = d3.map(data, z);
  const O = d3.map(data, d => d);
  if (defined === undefined) defined = (d, i) => !isNaN(X[i]) && !isNaN(Y[i]);
  const D = d3.map(data, defined);

  // Compute default domains, and unique the z-domain.
  if (xDomain === undefined) xDomain = d3.extent(X);
  if (yDomain === undefined) yDomain = [0, d3.max(Y, d => typeof d === "string" ? +d : d)];
  if (zDomain === undefined) zDomain = Z;
  zDomain = new d3.InternSet(zDomain);

  // Omit any data not present in the z-domain.
  const I = d3.range(X.length).filter(i => zDomain.has(Z[i]));

  // Construct scales and axes.
  const xScale = xType(xDomain, xRange);
  const yScale = yType(yDomain, yRange);
  const xAxis = d3.axisBottom(xScale).ticks(width / 80).tickSizeOuter(0);
  const yAxis = d3.axisLeft(yScale).ticks(height / 60, yFormat);

  // Compute titles.
  const T = title === undefined ? Z : title === null ? null : d3.map(data, title);

  // Construct a line generator.
  const line = d3.line()
      .defined(i => D[i])
      .curve(curve)
      .x(i => xScale(X[i]))
      .y(i => yScale(Y[i]));

  const svg = d3.create("svg")
      .attr("width", width)
      .attr("height", height)
      .attr("viewBox", [0, 0, width, height])
      .attr("style", "max-width: 100%; height: auto; height: intrinsic;")
      .style("-webkit-tap-highlight-color", "transparent")
      .on("pointerenter", pointerentered)
      .on("pointermove", pointermoved)
      .on("pointerleave", pointerleft)
      .on("touchstart", event => event.preventDefault());

  svg.append("g")
      .attr("transform", `translate(0,${height - marginBottom})`)
      .call(xAxis);

  svg.append("g")
      .attr("transform", `translate(${marginLeft},0)`)
      .call(yAxis)
      .call(g => g.select(".domain").remove())
      .call(voronoi ? () => {} : g => g.selectAll(".tick line").clone()
          .attr("x2", width - marginLeft - marginRight)
          .attr("stroke-opacity", 0.1))
      .call(g => g.append("text")
          .attr("x", -marginLeft/2)
          .attr("y", 10)
          .attr("fill", "currentColor")
          .attr("text-anchor", "start")
          .text(yLabel)
          .attr("font-size", "13px"));

  const path = svg.append("g")
      .attr("fill", "none")
      .attr("stroke-linecap", strokeLinecap)
      .attr("stroke-linejoin", strokeLinejoin)
      .attr("stroke-width", strokeWidth)
      .attr("stroke-opacity", strokeOpacity)
    .selectAll("path")
    .data(d3.group(I, i => Z[i]))
    .join("path")
      .style("mix-blend-mode", mixBlendMode)
      .attr("d", ([, I]) => line(I))
      .style("stroke", ([, I]) => colorScale(Z[I[0]]));

  const dot = svg.append("g")
      .attr("display", "none");

  dot.append("circle")
      .attr("r", 2.5);

  dot.append("text")
      .attr("font-family", "sans-serif")
      .attr("font-size", 10)
      .attr("text-anchor", "middle")
      .attr("y", -8);

  function pointermoved(event) {
    const [xm, ym] = d3.pointer(event);
    const i = d3.least(I, i => Math.hypot(xScale(X[i]) - xm, yScale(Y[i]) - ym)); // closest point
    dot.attr("transform", `translate(${xScale(X[i])},${yScale(Y[i])})`);
    if (T) dot.select("text").text(T[i]+": "+Y[i]);
    svg.property("value", O[i]).dispatch("input", {bubbles: true});
  }

  function pointerentered() {
    dot.attr("display", null);
  }

  function pointerleft() {
    dot.attr("display", "none");
    svg.node().value = null;
    svg.dispatch("input", {bubbles: true});
  }

  return Object.assign(svg.node(), {value: null});
}

d3.json("/line_chart_data")
  .then(function(data) {

  var keys = Object.keys(data[0]); // index: 0->k1, 1->k2, 2->a1, 3->optional a2

  const optionalSet = new Set(data.map(item => item[keys[0]]));
  const orderedArray = Array.from(optionalSet).sort();
  const colorScale = d3.scaleOrdinal()
    .domain(orderedArray)
    .range(d3.schemeCategory10);

  const svg = LineChart(data, {
    x: d => d[keys[1]],
    y: d => d[keys[2]],
    z: d => d[keys[0]],
    yLabel: keys[2],
    width: 1000,
    height: 800,
    color: "steelblue",
    colorScale: optionalSet.size === 1 ? null : colorScale
  })
  if (optionalSet.size != 1) {
    key = swatches({
      colour: colorScale
    })
    d3.select("body").append(() => key);
  }
  d3.select("body").append(() => svg);
})