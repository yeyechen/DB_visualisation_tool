$(document).ready(function() {

  // Disable all checkboxes that are in tables not related to the ones in the clicked table,
  // and also enable all checkboxes that are in tables related
  function disableUnrelatedCheckboxes(tables) {
    $("#table-list input[type='checkbox']").each(function() {
      var checkbox = $(this);
      var tableName = checkbox.attr("name").split("-")[0];

      if (tables.includes(tableName)) {
        checkbox.prop("disabled", false);  // Enable checkbox
      } else {
        checkbox.prop("disabled", true);   // Disable checkbox
      }
    });
  }

  // Disable all checkboxes if the maximum number of checkboxes is checked
  function disableAllCheckboxes() {
    $("#table-list input[type='checkbox']").not(":checked").attr("disabled", true);
  }

  // Enable all checkboxes in all tables
  function enableAllCheckboxes() {
    $("#table-list input[type='checkbox']").attr("disabled", false);
  }

  // Count the number of all checkboxes
  function countCheckboxes() {
    return $("#table-list input[type='checkbox']:checked").length;
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

    var selectedTables = [];
    var relatedTables = [];
    // Disable checkboxes in other tables when a checkbox is clicked
    $("#table-list input[type='checkbox']").click(function() {
      var table = $(this).attr("name").split("-")[0];
      var isChecked = $(this).prop("checked");
      if (isChecked) {
        selectedTables.push(table);
      } else {
        var index = selectedTables.indexOf(table);
        if (index !== -1) {
          selectedTables.splice(index, 1);
        }
      }
      $.ajax({
        url: "/get-related-tables",
        type: "POST",
        data: JSON.stringify(selectedTables),
        success: function(response) {
          relatedTables = JSON.parse(response);
          disableUnrelatedCheckboxes(relatedTables);

          if (countCheckboxes() >= 4) {
            disableAllCheckboxes();
          }

          if (countCheckboxes() === 0) {
            enableAllCheckboxes();
          }
        }
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

		$.ajax({
			url: "/process-attr-selection",
			type: "POST",
			contentType: "application/json",
			data: JSON.stringify(selectedAttributes),
			success: function(data) {
			  if ($("#vis-form").children().length > 0) {
			    $("#vis-form").empty();
			  }
				$("<script>").attr("type", "module").attr("src", "select_vis.js?v=" + new Date().getTime()).appendTo("#right");
        $("<script>").attr("type", "module").attr("src", "filter.js?v=" + new Date().getTime()).appendTo("#mid");
			},
			error: function(xhr, status, error) {
				alert(xhr.responseText);
			}
		});
  });
});
