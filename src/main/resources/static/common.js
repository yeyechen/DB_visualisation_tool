export const margin = { top: 20, right: 30, bottom: 30, left: 40 };
export const width = 960 - margin.left - margin.right;
export const height = 600 - margin.top - margin.bottom;

export function createSvg() {
  const svg = d3.select("body").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
  return svg;
}