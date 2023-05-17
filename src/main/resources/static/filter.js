$(document).ready(function() {

  $("#filter-list thead").empty();
  $("#filter-list tbody").empty();
  $("#filter-form br").remove();
  $("#filter-form button").remove();

  $.get("/filter-options", function(tableData) {
    console.log(tableData);
    if (Array.isArray(tableData) && tableData.length !== 0) {
      $("#mid-heading").text("Select filter:");
      $("<tr>")
        .append($("<th>").text("Table Name"))
        .append($("<th>").text("Filtering Attributes"))
        .appendTo("#filter-list thead");

      $("<br>").appendTo("#filter-list");
      $("<button>").text("Apply").attr("type", "submit").appendTo("#filter-list");
    }
    $.each(tableData, function(index, table) {
      var $tableListItem =$("<tr></tr>").appendTo("#filter-list tbody");
      $("<td>" + table.name + "</td>").appendTo($tableListItem);
      var $attributesTd = $("<td></td>").appendTo($tableListItem);
      $.each(table.attributes, function(index, attribute) {
        $("<label><input type='checkbox' name='" + table.name + "-" + attribute + "' value='" + table.name + "." + attribute + "'> " + attribute + "</label><br>").appendTo($attributesTd);
      });
    });

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

          filterConditionTable.find("tbody").empty();

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
        showAlert("Filter Applied!");
      },
      error: function(xhr, status, error) {
        alert(xhr.responseText);
      }
    });
  });

  function showAlert(message) {
    var container = document.getElementById("notification-container");
    var notification = document.createElement("div");
    notification.classList.add("notification");
    notification.textContent = message;

    container.appendChild(notification);
    container.style.display = "block";

    setTimeout(function() {
      container.style.display = "none";
      notification.remove();
    }, 3000);
  }

});