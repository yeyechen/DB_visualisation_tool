function swatches({
  colour,
  swatchRadius = 6,
  swatchPadding = swatchRadius * (2/3),
  labelFont = "12px sans-serif",
  labelFormat = x => x,
  labelPadding = swatchRadius * 1.5,
  marginLeft = 0
} = {}) {

  const spacing = colour
    .domain()
    .map(d => labelFormat(d))
    .map(d => getLabelLength(d, labelFont) + (swatchRadius * 2) + swatchPadding + labelPadding)
    .map((_, i, g) => d3.cumsum(g)[i] + marginLeft)

  const width = d3.max(spacing)
  const height = swatchRadius * 2 + swatchPadding * 2

  const svg = d3.create("svg")
    .attr("width", width)
    .attr("height", height)
    .attr("viewBox", [0, 0, width, height])
    .style("overflow", "visible")
    .style("display", "block");

  const g = svg
    .append("g")
      .attr("transform", `translate(0, ${height / 2})`)
    .selectAll("g")
    .data(colour.domain())
    .join("g")
      .attr("transform", (d, i) => `translate(${spacing[i - 1] || marginLeft}, 0)`);

  g.append("circle")
      .attr("fill", colour)
      .attr("r", swatchRadius)
      .attr("cx", swatchRadius)
      .attr("cy", 0);

  g.append("text")
      .attr("x", swatchRadius * 2 + swatchPadding)
      .attr("y", 0)
      .attr("dominant-baseline", "central")
      .style("font", labelFont)
      .text(d => labelFormat(d));

  return svg.node()

}

const getLabelLength = (label, labelFont = '12px sans-serif') => {
  const id = `label-${Math.random().toString(36).substring(2)}`;

  // Create SVG element
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
  svg.setAttribute('version', '1.1');
  svg.innerHTML = `
    <style> .${id} { font: ${labelFont} } </style>
    <g id="${id}">
      <text class="${id}">${label}</text>
    </g>
  `;

  // Add the SVG element to the DOM so we can determine its size.
  document.body.appendChild(svg);

  // Compute the bounding box of the content.
  const textElement = svg.querySelector(`.${id}`);
  const textBoundingBox = textElement.getBBox();
  const width = textBoundingBox.width;

  // Remove the SVG element from the DOM.
  document.body.removeChild(svg);

  return width;
};

