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
    $("<br>").appendTo("#filter-list");
    $("<button>").text("Apply").attr("type", "submit").appendTo("#filter-list");

    $("#filter-list input[type='checkbox']").click(function() {
      var checkboxValue = $(this).val();

      $.ajax({
        url: "/filter-click",
        method: "POST",
        data: checkboxValue,
        success: function(data) {

          var filterConditionTable = $("#filter-condition");
          // Append header row if it doesn't exist yet
          if (filterConditionTable.find("thead").children().length === 0) {
            $("<tr>")
              .append($("<th>").text("Conditions"))
              .appendTo(filterConditionTable.find("thead"));
          }
//          filterConditionTable.find("tbody").empty();

          var $row = $("<tr>").appendTo(filterConditionTable.find("tbody"));
          var $optionsTd = $("<td>").appendTo($row);
          $.each(data, function(index, value) {
            var checkbox = $("<input>")
              .attr("type", "checkbox")
              .attr("name", "filterCondition")
              .attr("value", value)
              .attr("data-filter-option", checkboxValue);
            var label = $("<label>").text(value).append(checkbox);
            $optionsTd.append(label);
            $("<br>").appendTo($optionsTd);
          });

        }
      });
    });
  });

  var options = {};

  $(document).on("click", "#filter-condition input[type='checkbox']", function(event) {

    var filterOption = $(this).data("filter-option");
    var checkboxValue = $(this).val();

    if ($(this).is(":checked")) {
      // Checkbox is checked, add the option to the object
      if (!options[filterOption]) {
        options[filterOption] = [];
      }
      options[filterOption].push(checkboxValue);
    } else {
      // Checkbox is unchecked, remove the option from the object
      if (options[filterOption]) {
        var index = options[filterOption].indexOf(checkboxValue);
        if (index !== -1) {
          options[filterOption].splice(index, 1);
        }
      }
    }

  });

  $(document).on("click", "#filter-list button[type='submit']", function(event) {
    event.preventDefault();

    $.ajax({
      url: "/process-filter",
      type: "POST",
      contentType: "application/json",
      data: JSON.stringify(options),
      success: function(data) {
        console.log(data);
      },
      error: function(xhr, status, error) {
        alert(xhr.responseText);
      }
    });
  });
});