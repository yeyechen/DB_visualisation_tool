$(document).ready(function() {
  $.get("/options", function(tableData) {
    // Generate the checkboxes for each table
    $.each(tableData, function(index, table) {
      var $tableListItem = $("<li></li>").appendTo("#table-list");
      $("<h3>" + table.name + "</h3>").appendTo($tableListItem);
      $.each(table.attributes, function(index, attribute) {
        $("<label><input type='checkbox' name='" + table.name + "-" + attribute + "' value='" + table.name + "." + attribute + "'> " + attribute + "</label><br>").appendTo($tableListItem);
      });
    });
  });

  $("#attribute-form").submit(function(event) {
    event.preventDefault();

    // Get the selected checkboxes
    var selectedAttributes = [];
    $("input:checked").each(function() {
      selectedAttributes.push($(this).val());
    });

    $.post("/selection", { attributes: JSON.stringify(selectedAttributes) }, function(data) {
      alert("status " + data);
    });
  });
});
