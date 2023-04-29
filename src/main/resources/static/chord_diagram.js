function ChordDiagram(data, {
  names = data.names === undefined ? d3.range(data.length) : data.names,
  colors = data.colors === undefined ? d3.quantize(d3.interpolateRainbow, names.length) : data.colors,
  width = 1600,
  height = width,
  outerRadius = Math.min(width, height) * 0.5 - 60,
  innerRadius = outerRadius - 10,
  color = d3.scaleOrdinal(names, colors),
  ribbon = d3.ribbon()
      .radius(innerRadius - 1)
      .padAngle(1 / innerRadius),
  arc = d3.arc()
      .innerRadius(innerRadius)
      .outerRadius(outerRadius),
  chord = d3.chord()
        .padAngle(10 / innerRadius)
        .sortSubgroups(d3.descending)
        .sortChords(d3.descending),
  tickStep = d3.tickStep(0, d3.sum(data.flat()), 100),
  formatValue = d3.format("d"),

} = {}) {
  const svg = d3.create("svg")
      .attr("viewBox", [-width / 2, -height / 2, width, height]);

  const chords = chord(data);

  const group = svg.append("g")
      .attr("font-size", 10)
      .attr("font-family", "sans-serif")
    .selectAll("g")
    .data(chords.groups)
    .join("g");

  group.append("path")
      .attr("fill", d => color(names[d.index]))
      .attr("d", arc);

  group.append("title")
      .text(d => `${names[d.index]} ${formatValue(d.value)}`);

  const groupTick = group.append("g")
    .selectAll("g")
    .data(ticks)
    .join("g")
      .attr("transform", d => `rotate(${d.angle * 180 / Math.PI - 90}) translate(${outerRadius},0)`);

  groupTick.append("line")
      .attr("stroke", "currentColor")
      .attr("x2", 6);

  groupTick.append("text")
      .attr("x", 8)
      .attr("dy", "0.35em")
      .attr("transform", d => d.angle > Math.PI ? "rotate(180) translate(-16)" : null)
      .attr("text-anchor", d => d.angle > Math.PI ? "end" : null)
      .text(d => formatValue(d.value));

  group.select("text")
      .attr("font-weight", "bold")
      .text(function(d) {
        return this.getAttribute("text-anchor") === "end"
            ? `↑ ${names[d.index]}`
            : `${names[d.index]} ↓`;
      });

  svg.append("g")
      .attr("fill-opacity", 0.8)
    .selectAll("path")
    .data(chords)
    .join("path")
      .style("mix-blend-mode", "multiply")
      .attr("fill", d => color(names[d.source.index]))
      .attr("d", ribbon)
    .append("title")
      .text(d => `${formatValue(d.source.value)} ${names[d.target.index]} → ${names[d.source.index]}${d.source.index === d.target.index ? "" : `\n${formatValue(d.target.value)} ${names[d.source.index]} → ${names[d.target.index]}`}`);

  function ticks({startAngle, endAngle, value}) {
    const k = (endAngle - startAngle) / value;
    return d3.range(0, value, tickStep).map(value => {
      return {value, angle: value * k + startAngle};
    });
  }

  return svg.node();
}

d3.json("/chord_diagram_data")
  .then(function(data) {

  var keys = Object.keys(data[0]); // index: 0->k_1, 1->k_2, 2->a1, 3->optional color

  const classes = Array.from(new Set(data.flatMap(({ [keys[0]]: x, [keys[1]]: y }) => [x, y])));
  const matrix = Array.from({ [keys[2]]: classes[keys[2]] }, () => new Array(classes[keys[2]]).fill(0));

  data.forEach(({ [keys[0]]: x, [keys[1]]: y, [keys[2]]: value }) => {
    const row = classes.indexOf(y);
    const col = classes.indexOf(x);
    matrix[row][col] = value;
  });

  const colorScale = d3.scaleOrdinal()
    .domain(classes)
    .range(d3.schemeCategory10);

  const colors = classes.map(colorScale);

  var processedData = {
    names: classes,
    colors: colors
  }

  processedData = Object.assign(matrix,
  {
   names: classes,
   colors: colors
  });
  const svg = ChordDiagram(processedData, {})
  d3.select("body").append(() => svg);
})


