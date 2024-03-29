// Copyright 2021 Observable, Inc.
// Released under the ISC license.
// https://observablehq.com/@d3/treemap
function Treemap(data, { // data is either tabular (array of objects) or hierarchy (nested objects)
  path, // as an alternative to id and parentId, returns an array identifier, imputing internal nodes
  id = Array.isArray(data) ? d => d.id : null, // if tabular data, given a d in data, returns a unique identifier (string)
  parentId = Array.isArray(data) ? d => d.parentId : null, // if tabular data, given a node d, returns its parent’s identifier
  children, // if hierarchical data, given a d in data, returns its children
  value, // given a node d, returns a quantitative value (for area encoding; null for count)
  sort = (a, b) => d3.descending(a.value, b.value), // how to sort nodes prior to layout
  label, // given a leaf node d, returns the name to display on the rectangle
  group, // given a leaf node d, returns a categorical value (for color encoding)
  title, // given a leaf node d, returns its hover text
  link, // given a leaf node d, its link (if any)
  linkTarget = "_blank", // the target attribute for links (if any)
  tile = d3.treemapBinary, // treemap strategy
  width = 640, // outer width, in pixels
  height = 400, // outer height, in pixels
  margin = 0, // shorthand for margins
  marginTop = margin, // top margin, in pixels
  marginRight = margin, // right margin, in pixels
  marginBottom = margin, // bottom margin, in pixels
  marginLeft = margin, // left margin, in pixels
  padding = 1, // shorthand for inner and outer padding
  paddingInner = padding, // to separate a node from its adjacent siblings
  paddingOuter = padding, // shorthand for top, right, bottom, and left padding
  paddingTop = paddingOuter, // to separate a node’s top edge from its children
  paddingRight = paddingOuter, // to separate a node’s right edge from its children
  paddingBottom = paddingOuter, // to separate a node’s bottom edge from its children
  paddingLeft = paddingOuter, // to separate a node’s left edge from its children
  round = true, // whether to round to exact pixels
  colors = d3.schemeTableau10, // array of colors
  zDomain, // array of values for the color scale
  fill = "#4682B4", // fill for node rects (if no group color encoding)
  fillOpacity = group == null ? null : 0.6, // fill opacity for node rects
  stroke, // stroke for node rects
  strokeWidth, // stroke width for node rects
  strokeOpacity, // stroke opacity for node rects
  strokeLinejoin, // stroke line join for node rects
  valueLabel,
} = {}) {

  // If id and parentId options are specified, or the path option, use d3.stratify
  // to convert tabular data to a hierarchy; otherwise we assume that the data is
  // specified as an object {children} with nested objects (a.k.a. the “flare.json”
  // format), and use d3.hierarchy.

  const root = d3.hierarchy(data, children);

  // Compute the values of internal nodes by aggregating from the leaves.
  value == null ? root.count() : root.sum(d => Math.max(0, value(d)));
  // Prior to sorting, if a group channel is specified, construct an ordinal color scale.
  const leaves = root.leaves();
  const G = group == null ? null : leaves.map(d => group(d.data, d));
  if (zDomain === undefined) zDomain = G;
  zDomain = new d3.InternSet(zDomain);

  const color = group == null ? null : d3.scaleOrdinal(zDomain, colors);

  // Compute labels and titles.
  const L = label == null ? null : leaves.map(d => label(d.data, d));
  const T = title === undefined ? L : title == null ? null : leaves.map(d => title(d.data, d));

  // Sort the leaves (typically by descending value for a pleasing layout).
  if (sort != null) root.sort(sort);

  // Compute the treemap layout.
  d3.treemap()
      .tile(tile)
      .size([width - marginLeft - marginRight, height - marginTop - marginBottom])
      .paddingInner(paddingInner)
      .paddingTop(paddingTop)
      .paddingRight(paddingRight)
      .paddingBottom(paddingBottom)
      .paddingLeft(paddingLeft)
      .round(round)
    (root);

  const svg = d3.create("svg")
      .attr("viewBox", [-marginLeft, -marginTop, width, height])
      .attr("width", width)
      .attr("height", height)
      .attr("style", "max-width: 100%; height: auto; height: intrinsic;")
      .attr("font-family", "sans-serif")
      .attr("font-size", 10);

  const node = svg.selectAll("a")
    .data(leaves)
    .join("a")
      .attr("xlink:href", link == null ? null : (d, i) => link(d.data, d))
      .attr("target", link == null ? null : linkTarget)
      .attr("transform", d => `translate(${d.x0},${d.y0})`);

  node.append("rect")
      .attr("fill", color ? (d, i) => color(G[i]) : fill)
      .attr("fill-opacity", fillOpacity)
      .attr("stroke", stroke)
      .attr("stroke-width", strokeWidth)
      .attr("stroke-opacity", strokeOpacity)
      .attr("stroke-linejoin", strokeLinejoin)
      .attr("width", d => d.x1 - d.x0)
      .attr("height", d => d.y1 - d.y0);

  if (L) {
    // A unique identifier for clip paths (to avoid conflicts).
    const uid = `O-${Math.random().toString(16).slice(2)}`;

    node.append("clipPath")
       .attr("id", (d, i) => `${uid}-clip-${i}`)
     .append("rect")
       .attr("width", d => d.x1 - d.x0)
       .attr("height", d => d.y1 - d.y0);

    node.append("text")
        .attr("clip-path", (d, i) => `url(${new URL(`#${uid}-clip-${i}`, location)})`)
      .selectAll("tspan")
      .data((d, i) => `${L[i]}`.split(/\n/g))
      .join("tspan")
        .attr("x", 3)
        .attr("y", (d, i, D) => `${(i === D.length - 1) * 0.3 + 1.1 + i * 0.9}em`)
        .attr("fill-opacity", (d, i, D) => i === D.length - 1 ? 0.7 : null)
        .style("font-size", "15px")
        .text(d => d);
  }

  var tooltip = d3.select("#tooltip")
    .append("div")
    .style("opacity", 0)
    .attr("class", "tooltip")
    .style("background-color", "white")
    .style("border", "solid")
    .style("border-radius", "1px")
    .style("padding", "1px");

  const chartElement = d3.select("#chart").node();

  svg.selectAll("rect")
    .on("mouseover", function(event, d) {
      const [x, y] = d3.pointer(event, chartElement);
      tooltip.transition()
        .duration(50)
        .style("opacity", .8);
      tooltip.html(`${d.parent.data.name}<br>${d.data.name}<br>${valueLabel}: ${d.value.toLocaleString("en")}`)
        .style("transform", `translate(${x + 10}px, ${y + 10}px)`);
    })
    .on("mousemove", function(event, i) {
      const [x, y] = d3.pointer(event, chartElement);
      tooltip
        .style("transform", `translate(${x + 10}px, ${y + 10}px)`);
    })
    .on("mouseout", function(event, d) {
      tooltip.transition()
        .duration(500)
        .style("opacity", 0);
    });

  return Object.assign(svg.node(), {scales: {color}});
}

d3.json("/tree_map_data")
  .then(function(data) {

  var keys = Object.keys(data[0]); // index: 0->k_p, 1->k_c, 2->a1, 3->optional

  const groupedData = data.reduce((acc, curr) => {
    const code = curr[keys[0]];
    const existingGroup = acc.children.find(group => group.name === code);
    if (existingGroup) {
      existingGroup.children.push({ "name": curr[keys[1]], "value": curr[keys[2]] });
    } else {
      acc.children.push({ "name": code, "children": [{ "name": curr[keys[1]], "value": curr[keys[2]] }] });
    }
    return acc;
  }, { name: "data", children: [] });

  const svg = Treemap(groupedData, {
    value: d => d.value, // size of each node (file); null for internal nodes (folders)
    group: (d, n) => n.ancestors().slice(-2)[0].data.name, // e.g., "animate" in flare/animate/Easing; color
    label: (d, n) => [d.name.split(/(?=[A-Z][a-z])/g), n.value.toLocaleString("en")].join("\n"),
    title: (d, n) => `${n.parent.data.name}\n${n.data.name}\n`+keys[2]+`: ${n.value.toLocaleString("en")}`,
    valueLabel: keys[2],
    width: 1152,
    height: 700
  })
  key = swatches({
    colour: svg.scales.color
  })
  d3.select("#chart").append(() => key);
  d3.select("#chart").append(() => svg);
})