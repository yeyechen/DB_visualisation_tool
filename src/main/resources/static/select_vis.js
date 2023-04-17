$(document).ready(function() {
  $.get("/vis-options", function(tableData) {
    $.each(tableData.option, function(index, option) {
      $("<label><input type='radio' name='options' value='" + option + "'> " + option + "</label><br>").appendTo("#vis-form");
    });
    $("<br>").appendTo("#vis-form");
    $("<button>").text("Submit").attr("type", "submit").appendTo("#vis-form");
  });

  $("#vis-form").submit(function(event) {
    event.preventDefault();

    var selectedVis = $("input[name='options']:checked").val();

    $.ajax({
      url: "/process-vis-selection",
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