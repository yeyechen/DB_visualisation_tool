$(document).ready(function() {
  $.get("/vis-options", function(tableData) {
    $.each(tableData.option, function(index, attribute) {
      $("<label><input type='checkbox' name='" + attribute + "'> " + attribute + "</label><br>").appendTo("#right");
    });
  });
});
