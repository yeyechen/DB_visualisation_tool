// Copyright 2021 Observable, Inc.
// Released under the ISC license.
// https://observablehq.com/@d3/choropleth
function Choropleth(data, {
  id = d => d.id, // given d in data, returns the feature id
  value = () => undefined, // given d in data, returns the quantitative value
  title, // given a feature f and possibly a datum d, returns the hover text
  format, // optional format specifier for the title
  scale = d3.scaleSequential, // type of color scale
  domain, // [min, max] values; input of color scale
  range = d3.interpolateBlues, // output of color scale
  width = 640, // outer width, in pixels
  height, // outer height, in pixels
  projection, // a D3 projection; null for pre-projected geometry
  features, // a GeoJSON feature collection
  featureId = d => d.id, // given a feature, returns its id
  borders, // a GeoJSON object for stroking borders
  outline = projection && projection.rotate ? {type: "Sphere"} : null, // a GeoJSON object for the background
  unknown = "#ccc", // fill color for missing data
  fill = "white", // fill color for outline
  stroke = "white", // stroke color for borders
  strokeLinecap = "round", // stroke line cap for borders
  strokeLinejoin = "round", // stroke line join for borders
  strokeWidth, // stroke width for borders
  strokeOpacity, // stroke opacity for borders
} = {}) {
  // Compute values.
  const N = d3.map(data, id);
  const V = d3.map(data, value).map(d => d == null ? NaN : +d);
  const Im = new d3.InternMap(N.map((id, i) => [id, i]));
  const If = d3.map(features.features, featureId);

  // Compute default domains.
  if (domain === undefined) domain = d3.extent(V);

  // Construct scales.
  const color = scale(domain, range);
  if (color.unknown && unknown !== undefined) color.unknown(unknown);

  // Compute titles.
  if (title === undefined) {
    format = color.tickFormat(100, format);
    title = (f, i) => `${f.properties.name}\n${format(V[i])}`;
  } else if (title !== null) {
    const T = title;
    const O = d3.map(data, d => d);
    title = (f, i) => T(f, O[i]);
  }

  // Compute the default height. If an outline object is specified, scale the projection to fit
  // the width, and then compute the corresponding height.
  if (height === undefined) {
    if (outline === undefined) {
      height = 400;
    } else {
      const [[x0, y0], [x1, y1]] = d3.geoPath(projection.fitWidth(width, outline)).bounds(outline);
      const dy = Math.ceil(y1 - y0), l = Math.min(Math.ceil(x1 - x0), dy);
      projection.scale(projection.scale() * (l - 1) / l).precision(0.2);
      height = dy;
    }
  }

  // Construct a path generator.
  const path = d3.geoPath(projection);

  const svg = d3.create("svg")
      .attr("width", width)
      .attr("height", height)
      .attr("viewBox", [0, 0, width, height])
      .attr("style", "width: 100%; height: auto; height: intrinsic;");

  if (outline != null) svg.append("path")
      .attr("fill", fill)
      .attr("stroke", "currentColor")
      .attr("d", path(outline));

  svg.append("g")
    .selectAll("path")
    .data(features.features)
    .join("path")
      .attr("fill", (d, i) => color(V[Im.get(If[i])]))
      .attr("d", path)
    .append("title")
      .text((d, i) => title(d, Im.get(If[i])));

  if (borders != null) svg.append("path")
      .attr("pointer-events", "none")
      .attr("fill", "none")
      .attr("stroke", stroke)
      .attr("stroke-linecap", strokeLinecap)
      .attr("stroke-linejoin", strokeLinejoin)
      .attr("stroke-width", strokeWidth)
      .attr("stroke-opacity", strokeOpacity)
      .attr("d", path(borders));

  return Object.assign(svg.node(), {scales: {color}});
}

d3.json("countries-50m.json").then(function(world) {
  d3.json("country_code_name_map.json").then(function(mapData) {
    rename = new Map([
      ["Antigua and Barbuda", "Antigua and Barb."],
      ["Bolivia (Plurinational State of)", "Bolivia"],
      ["Bosnia and Herzegovina", "Bosnia and Herz."],
      ["Brunei Darussalam", "Brunei"],
      ["Central African Republic", "Central African Rep."],
      ["Cook Islands", "Cook Is."],
      ["Democratic People's Republic of Korea", "North Korea"],
      ["Democratic Republic of the Congo", "Dem. Rep. Congo"],
      ["Dominican Republic", "Dominican Rep."],
      ["Equatorial Guinea", "Eq. Guinea"],
      ["Iran (Islamic Republic of)", "Iran"],
      ["Lao People's Democratic Republic", "Laos"],
      ["Marshall Islands", "Marshall Is."],
      ["Micronesia (Federated States of)", "Micronesia"],
      ["Republic of Korea", "South Korea"],
      ["Republic of Moldova", "Moldova"],
      ["Russian Federation", "Russia"],
      ["Saint Kitts and Nevis", "St. Kitts and Nevis"],
      ["Saint Vincent and the Grenadines", "St. Vin. and Gren."],
      ["Sao Tome and Principe", "São Tomé and Principe"],
      ["Solomon Islands", "Solomon Is."],
      ["South Sudan", "S. Sudan"],
      ["Swaziland", "eSwatini"],
      ["Syrian Arab Republic", "Syria"],
      ["The former Yugoslav Republic of Macedonia", "Macedonia"],
      ["United Republic of Tanzania", "Tanzania"],
      ["Venezuela (Bolivarian Republic of)", "Venezuela"],
      ["Viet Nam", "Vietnam"],
      ["United States", "United States of America"],
      ["Congo", "Dem. Rep. Congo"],
      ["Czech Republic", "Czechia"],
      ["Cote d'Ivoire", "Côte d'Ivoire"],
      ["Western Sahara", "W. Sahara"]
    ])

    var codeNameMap = new Map();
    mapData.forEach(function(item) {
      codeNameMap.set(item.code, item.name);
    })

    d3.json("/choropleth_map_data").then(function(data) {
      var keys = Object.keys(data[0]); // index: 0->k, 1->scalar
      data = data.map(d => ({name: codeNameMap.get(d[keys[0]]) || d[keys[0]], value: +d[keys[1]]}));
      data = data.map(d => ({name: rename.get(d.name) || d.name, value: +d.value}));
      countries = topojson.feature(world, world.objects.countries);
      countrymesh = topojson.mesh(world, world.objects.countries, (a, b) => a !== b);

      const svg = Choropleth(data, {
        id: d => d.name, // country name, e.g. Zimbabwe
        value: d => d.value, // health-adjusted life expectancy
        range: d3.interpolateYlGnBu,
        features: countries,
        featureId: d => d.properties.name, // i.e., not ISO 3166-1 numeric
        borders: countrymesh,
        projection: d3.geoEqualEarth()
      })
      const legend = Legend(svg.scales.color, {title: keys[1], marginLeft: 40, width: 500})
      d3.select("body").append(() => legend);
      d3.select("body").append(() => svg);
    });
  });
});
