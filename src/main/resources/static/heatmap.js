function Heatmap(data, classes, {
  margin = ({top: 30, right: 20, bottom: 100, left: 120}),
  height = 3000,
  width = 3000,
  legendHeight = 20,
  valRange = [0, d3.max(data.map(d => d.value), d => d3.max(d))],
  legendBins = [...Array(9).keys()].map(x => d3.quantile(valRange, x * 0.1)),
  legendElementWidth = Math.round( ((width - (margin.left + margin.right))/ 2) / legendBins.length)
}={}){

  const svg = d3.create("svg")
    .attr("width", width)
    .attr("height", height)
    .attr("viewBox", [0, 0, width, height])
    .attr("style", "max-width: 100%; height: auto; height: intrinsic;");

  x = d3.scaleBand()
    .domain(classes)
    .range([margin.left, width - margin.right])
    .padding(0.1)

  y = d3.scaleBand()
    .domain(data.map(d => d.key))
    .range([margin.top, height - margin.bottom])
    .padding(0.1)

  z = d3.scaleSequential(d3.interpolateOrRd)
    .domain([0, d3.max(data, d => d3.max(d.value))])

  xAxis = g => g
  .attr("transform", `translate(${x.bandwidth()/2},${height - margin.bottom})`)
  .call(d3.axisBottom(x).tickSize(0).tickPadding(4))
  .call(g => g.select(".domain").remove())
  .selectAll("text")
    .attr("y", -9)
    .attr("x", -9)
    .attr("dy", ".35em")
    .attr("transform", "rotate(270)")
    .style("text-anchor", "end")
    .style("fill", "black")

  yAxis = g => g
  .attr("transform", `translate(${margin.left},0)`)
  .call(d3.axisLeft(y).tickSize(0).tickPadding(4))
  .call(g => g.select(".domain").remove())
  .selectAll("text")
    .style("fill", "black")

  svg.append("g")
      .call(xAxis);

  svg.append("g")
      .call(yAxis);

  const serie = svg.append("g")
    .selectAll("g")
    .data(data)
    .enter().append("g")
      .attr("transform", d => `translate(0,${y(d.key) + 1})`);

  const tip = d3.tip().attr('class', 'd3-tip').html(d => (100*d).toFixed(1));

  svg.call(tip)

  serie.append("g")
    .selectAll("rect")
    .data(d => d.value)
    .enter().append("rect")
      .attr("fill"  , d => z(d))
      .attr("x"     , (d,i) => x(classes[i]))
      .attr("y"     , 0)
      .attr("height", y.bandwidth())
      .attr("width" , x.bandwidth())
      .on('mouseover', tip.show)
      .on('mouseout' , tip.hide);

  const legend = svg.append("g")
      .attr("transform", d => `translate(${margin.left},0)`);

  legend
    .selectAll("rect")
    .data(legendBins)
    .enter()
    .append("rect")
    .attr("x", (d, i) => legendElementWidth * i)
    .attr("y", height - (2*legendHeight))
    .attr("width", legendElementWidth)
    .attr("height", legendHeight)
    .style("fill", d => z(d));

  legend
    .selectAll("text")
    .data(legendBins)
    .enter()
    .append("text")
    .text(d => "≥ " + (100*d).toFixed(1))
    .attr("x", (d, i) => legendElementWidth * i)
    .attr("y", height - (legendHeight / 2))
    .style("font-size", "9pt")
    .style("font-family", "Consolas, courier")
    .style("fill", "black");

  return svg.node();
}

d3.json("/heatmap_data")
  .then(function(data) {

  var keys = Object.keys(data[0]); // index: 0->k_1, 1->k_2, 2->a1, 3->optional color

  const classes = Array.from(new Set(data.flatMap(({ [keys[0]]: x, [keys[1]]: y }) => [x, y])));
  const matrix = classes.reduce((acc, cur) => {
    acc.push({ key: cur, value: new Array(classes.length).fill(0) });
    return acc;
  }, []);

  data.forEach(({ [keys[0]]: x, [keys[1]]: y, [keys[2]]: value }) => {
    const row = classes.indexOf(y);
    const col = classes.indexOf(x);
    matrix[row].value[col] = value;
  });

  const svg = Heatmap(matrix, classes, {})

  d3.select("body").append(() => svg);
})
