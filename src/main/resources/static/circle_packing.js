// Copyright 2021 Observable, Inc.
// Released under the ISC license.
// https://observablehq.com/@d3/pack
function Pack(data, { // data is either tabular (array of objects) or hierarchy (nested objects)
  path, // as an alternative to id and parentId, returns an array identifier, imputing internal nodes
  id = Array.isArray(data) ? d => d.id : null, // if tabular data, given a d in data, returns a unique identifier (string)
  parentId = Array.isArray(data) ? d => d.parentId : null, // if tabular data, given a node d, returns its parent’s identifier
  children, // if hierarchical data, given a d in data, returns its children
  value, // given a node d, returns a quantitative value (for area encoding; null for count)
  sort = (a, b) => d3.descending(a.value, b.value), // how to sort nodes prior to layout
  label, // given a leaf node d, returns the display name
  title, // given a node d, returns its hover text
  link, // given a node d, its link (if any)
  linkTarget = "_blank", // the target attribute for links, if any
  width = 640, // outer width, in pixels
  height = 400, // outer height, in pixels
  margin = 1, // shorthand for margins
  marginTop = margin, // top margin, in pixels
  marginRight = margin, // right margin, in pixels
  marginBottom = margin, // bottom margin, in pixels
  marginLeft = margin, // left margin, in pixels
  padding = 3, // separation between circles
  fill = "#ddd", // fill for leaf circles
  fillOpacity, // fill opacity for leaf circles
  stroke = "#bbb", // stroke for internal circles
  strokeWidth, // stroke width for internal circles
  strokeOpacity, // stroke opacity for internal circles
  colorScale
} = {}) {

  // If id and parentId options are specified, or the path option, use d3.stratify
  // to convert tabular data to a hierarchy; otherwise we assume that the data is
  // specified as an object {children} with nested objects (a.k.a. the “flare.json”
  // format), and use d3.hierarchy.
  const root = path != null ? d3.stratify().path(path)(data)
      : id != null || parentId != null ? d3.stratify().id(id).parentId(parentId)(data)
      : d3.hierarchy(data, children);

  // Compute the values of internal nodes by aggregating from the leaves.
  value == null ? root.count() : root.sum(d => Math.max(0, value(d)));

  // Compute labels and titles.
  const descendants = root.descendants();
  const leaves = descendants.filter(d => !d.children);
  leaves.forEach((d, i) => d.index = i);
  const L = label == null ? null : leaves.map(d => label(d.data, d));
  const T = title == null ? null : descendants.map(d => title(d.data, d));

  // Sort the leaves (typically by descending value for a pleasing layout).
  if (sort != null) root.sort(sort);

  // Compute the layout.
  d3.pack()
      .size([width - marginLeft - marginRight, height - marginTop - marginBottom])
      .padding(padding)
    (root);

  const svg = d3.create("svg")
      .attr("viewBox", [-marginLeft, -marginTop, width, height])
      .attr("width", width)
      .attr("height", height)
      .attr("style", "max-width: 100%; height: auto; height: intrinsic;")
      .attr("font-family", "sans-serif")
      .attr("font-size", 10)
      .attr("text-anchor", "middle");

  const node = svg.selectAll("a")
    .data(descendants)
    .join("a")
      .attr("xlink:href", link == null ? null : (d, i) => link(d.data, d))
      .attr("target", link == null ? null : linkTarget)
      .attr("transform", d => `translate(${d.x},${d.y})`);

  node.append("circle")
      .attr("fill", d => d.children ? "#fff" : (colorScale ? colorScale(d.data.color) : fill))
      .attr("fill-opacity", d => d.children ? null : fillOpacity)
      .attr("stroke", d => d.children ? stroke : null)
      .attr("stroke-width", d => d.children ? strokeWidth : null)
      .attr("stroke-opacity", d => d.children ? strokeOpacity : null)
      .attr("r", d => d.r);

  if (T) node.append("title").text((d, i) => T[i]);

  if (L) {
    // A unique identifier for clip paths (to avoid conflicts).
    const uid = `O-${Math.random().toString(16).slice(2)}`;

    const leaf = node
      .filter(d => !d.children && d.r > 10 && L[d.index] != null);

    leaf.append("clipPath")
        .attr("id", d => `${uid}-clip-${d.index}`)
      .append("circle")
        .attr("r", d => d.r);

    leaf.append("text")
        .attr("clip-path", d => `url(${new URL(`#${uid}-clip-${d.index}`, location)})`)
      .selectAll("tspan")
      .data(d => `${L[d.index]}`.split(/\n/g))
      .join("tspan")
        .attr("x", 0)
        .attr("y", (d, i, D) => `${(i - D.length / 2) + 0.85}em`)
        .attr("fill-opacity", (d, i, D) => i === D.length - 1 ? 0.7 : null)
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

  svg.selectAll("circle")
    .on("mouseover", function(event, d, i) {
      const [x, y] = d3.pointer(event, chartElement);
      tooltip.transition()
        .duration(50)
        .style("opacity", .8);
      tooltip.html(L[d.index] ? L[d.index].split(/\n/g).join("<br>") : "")
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

  return svg.node();
}

d3.json("/circle_packing_data")
  .then(function(data) {

  var keys = Object.keys(data[0]); // index: 0->k_p, 1->k_c, 2->a1, 3->optional

  const groupedData = data.reduce((acc, curr) => {
    const code = curr[keys[0]];
    const existingGroup = acc.children.find(group => group.name === code);
    if (existingGroup) {
      existingGroup.children.push({ "name": curr[keys[1]], "value": curr[keys[2]], "color":curr[keys[3]] });
    } else {
      acc.children.push({ "name": code, "children": [{ "name": curr[keys[1]], "value": curr[keys[2]], "color":curr[keys[3]] }] });
    }
    return acc;
  }, { name: "", children: [] });

  const optionalSet = new Set(data.map(item => item[keys[3]]));
  const orderedArray = Array.from(optionalSet).sort();
  const colorScale = d3.scaleOrdinal()
    .domain(orderedArray)
    .range(d3.schemeCategory10);

  const svg = Pack(groupedData, {
    value: d => d.value,
    label: (d, n) => [...d.name.split(/(?=[A-Z][a-z])/g), n.value.toLocaleString("en")].join("\n"),
    title: (d, n) => `${n.ancestors().reverse().map(({data: d}) => d.name).join(".")}\n`+keys[2]+`: ${n.value.toLocaleString("en")}`,
    width: 1152,
    height: 780,
    colorScale: optionalSet.size === 1 ? null : colorScale
  })

  if (optionalSet.size != 1) {
    key = swatches({
      colour: colorScale
    })
    d3.select("#chart").append(() => key);
  }
  d3.select("#chart").append(() => svg);
})