$(document).ready(function() {
  $.get("/options", function(tableData) {
    // Generate the checkboxes for each table
    $.each(tableData, function(index, table) {
      var $tableListItem = $("<tr></tr>").appendTo("#table-list tbody");
      $("<td>" + table.name + "</td>").appendTo($tableListItem);
      $("<td><strong>" + table.pKey + "</strong></td>").appendTo($tableListItem);
      var $attributesTd = $("<td></td>").appendTo($tableListItem);
      $.each(table.attributes, function(index, attribute) {
        $("<label><input type='checkbox' name='" + table.name + "-" + attribute + "' value='" + table.name + "." + attribute + "'> " + attribute + "</label><br>").appendTo($attributesTd);
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

    $.post("/process-selection", { attributes: JSON.stringify(selectedAttributes) }, function(data) {
      alert("status " + data);
    });
  });
});
