function WordCloud(data, {
  size = group => group.length, // Given a grouping of words, returns the size factor for that word
  word = d => d, // Given an item of the data array, returns the word
  marginTop = 0, // top margin, in pixels
  marginRight = 0, // right margin, in pixels
  marginBottom = 0, // bottom margin, in pixels
  marginLeft = 0, // left margin, in pixels
  width = 1200, // outer width, in pixels
  height = 600, // outer height, in pixels
  fontFamily = "sans-serif", // font family
  maxFontSize = 250, // base font size
  padding = 0, // amount of padding between the words (in pixels)
  rotate = 0, // a constant or function to rotate the words
  invalidation // when this promise resolves, stop the simulation
} = {}) {

  const svg = d3.create("svg")
      .attr("viewBox", [0, 0, width, height])
      .attr("width", width)
      .attr("font-family", fontFamily)
      .attr("text-anchor", "middle")
      .attr("style", "max-width: 100%; height: auto; height: intrinsic;");

  const g = svg.append("g").attr("transform", `translate(${marginLeft},${marginTop})`);

  const maxSize = data[0].size;
  const fontScale = maxFontSize/maxSize;

  const cloud = d3.layout.cloud()
      .size([width - marginLeft - marginRight, height - marginTop - marginBottom])
      .words(data)
      .padding(padding)
      .rotate(rotate)
      .font(fontFamily)
      .fontSize(d => (d.size * fontScale))
      .on("word", ({size, x, y, rotate, text}) => {
        g.append("text")
            .attr("font-size", size)
            .attr("transform", `translate(${x},${y}) rotate(${rotate})`)
            .text(text);
      });

  cloud.start();
  invalidation && invalidation.then(() => cloud.stop());
  return svg.node();
}

d3.json("/word_cloud_data")
  .then(function(data) {

  var keys = Object.keys(data[0]); // index: 0->k, 1->a1
  const processedData = data.map(obj => ({
    text: obj[keys[0]],
    size: obj[keys[1]]
  }))
  .sort((a, b) => d3.descending(a.size, b.size));
  const svg = WordCloud(processedData)

  d3.select("body").append(() => svg);
})