$(document).ready(function() {
  $.get("/vis-options", function(tableData) {
    var $tableListItem = $("<tr></tr>").appendTo("#table-list tbody");
    $("<td>" + tableData.option + "</td>").appendTo($tableListItem);
    var $attributesTd = $("<td></td>").appendTo($tableListItem);
    $.each(tableData.option, function(index, attribute) {
      $("<label><input type='checkbox' name='" + attribute + "'> " + attribute + "</label><br>").appendTo($attributesTd);
    });
  });
});
