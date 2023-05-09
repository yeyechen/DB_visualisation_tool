$(document).ready(function() {

  // Disable all checkboxes except for the ones in the clicked table
  function disableCheckboxes(tables) {
    $("input[type='checkbox']").not(function() {
      return tables.includes($(this).attr("name").split("-")[0]);
    }).attr("disabled", true);
  }

  function disableAllCheckboxes() {
    $("input[type='checkbox']").not(":checked").attr("disabled", true);
  }

  function enableCheckboxes(tables) {
    $("input[type='checkbox']").filter(function() {
      return tables.includes($(this).attr("name").split("-")[0]);
    }).attr("disabled", false);
  }

  // Enable all checkboxes in all tables
  function enableAllCheckboxes() {
    $("input[type='checkbox']").attr("disabled", false);
  }

  function countSelectedCheckboxes(tables) {
    var count = 0;
    $.each(tables, function(index, table) {
      count += $("input[name^='" + table + "']:checked").length;
    });
    return count;
  }

  $.get("/attr-options", function(tableData) {
    // Generate the checkboxes for each table
    $.each(tableData, function(index, table) {
      var $tableListItem = $("<tr></tr>").appendTo("#table-list tbody");
      $("<td>" + table.name + "</td>").appendTo($tableListItem);
      $("<td><u>" + table.pKey + "</u></td>").appendTo($tableListItem);
      var $attributesTd = $("<td></td>").appendTo($tableListItem);
      $.each(table.attributes, function(index, attribute) {
        $("<label><input type='checkbox' name='" + table.name + "-" + attribute + "' value='" + table.name + "." + attribute + "'> " + attribute + "</label><br>").appendTo($attributesTd);
      });
    });

  // Disable checkboxes in other tables when a checkbox is clicked
  $("input[type='checkbox']").click(function() {
    var checkedBoxes = $("input[type='checkbox']:checked");
    var checkedTables = [...new Set(checkedBoxes.map(function() {
      return $(this).attr("name").split("-")[0];
    }).toArray())];

    if (countSelectedCheckboxes(checkedTables) >= 4) {
      disableAllCheckboxes();
    }

    if (checkedTables.length >= 2) {
      disableCheckboxes(checkedTables);
    } else if (checkedTables.length < 2 && countSelectedCheckboxes(checkedTables) < 4) {
      enableAllCheckboxes();
    }

    if (countSelectedCheckboxes(checkedTables) < 4) {
      enableCheckboxes(checkedTables);
    }

  });
});

  $("#attribute-form").submit(function(event) {
    event.preventDefault();

    // Get the selected checkboxes
    var selectedAttributes = [];
    $("input:checked").each(function() {
      selectedAttributes.push($(this).val());
    });

		$.ajax({
			url: "/process-attr-selection",
			type: "POST",
			contentType: "application/json",
			data: JSON.stringify(selectedAttributes),
			success: function(data) {
			  if ($("#vis-form").children().length > 0) {
			    $("#vis-form").empty();
			  }
			  $("<h2>").text("Select visualisation").appendTo("#vis-form");
				$("<script>").attr("type", "module").attr("src", "select_vis.js?v=" + new Date().getTime()).appendTo("#right");
			},
			error: function(xhr, status, error) {
				alert(xhr.responseText);
			}
		});
  });
});
