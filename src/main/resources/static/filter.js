$(document).ready(function() {

  $("#filter-list thead").empty();
  $("#filter-list tbody").empty();
  $("#filter-form br").remove();
  $("#filter-form button").remove();

  $("#mid-heading").text("Select filter:");
  $.get("/filter-options", function(tableData) {
    $("<tr>")
      .append($("<th>").text("Table Name"))
      .append($("<th>").text("Filtering Attributes"))
      .appendTo("#filter-list thead");
    $.each(tableData, function(index, table) {
      var $tableListItem =$("<tr></tr>").appendTo("#filter-list tbody");
      $("<td>" + table.name + "</td>").appendTo($tableListItem);
      var $attributesTd = $("<td></td>").appendTo($tableListItem);
      $.each(table.attributes, function(index, attribute) {
        $("<label><input type='checkbox' name='" + table.name + "-" + attribute + "' value='" + table.name + "." + attribute + "'> " + attribute + "</label><br>").appendTo($attributesTd);
      });
    });
    $("<br>").appendTo("#filter-form");
    $("<button>").text("Apply").attr("type", "submit").appendTo("#filter-form");

    $("#filter-list input[type='checkbox']").click(function() {

    });
  });

  $("#filter-form").submit(function(event) {
    event.preventDefault();

    var selectedVis = $("input[name='options']:checked").val();

    $.ajax({
      url: "/process-filter",
      type: "POST",
      contentType: "application/json",
      data: JSON.stringify(selectedVis),
      success: function(data) {
				window.history.pushState(null, null, data);
				location.reload();
      },
      error: function(xhr, status, error) {
        alert(xhr.responseText);
      }
    });
  });
});