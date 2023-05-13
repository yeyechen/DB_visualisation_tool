$(document).ready(function() {

  // Disable all checkboxes except for the ones in the clicked table
  function disableCheckboxes(table) {
    $("#table-list input[type='checkbox']").not("input[name^='" + table + "']").attr("disabled", true);
  }

  // Disable all checkboxes if the maximum number of checkboxes is checked
  function disableAllCheckboxes() {
    $("#table-list input[type='checkbox']").not(":checked").attr("disabled", true);
  }

  // Enable checkboxes in a certain table
  function enableCheckboxes(table) {
    $("#table-list input[type='checkbox'][name^='" + table + "']").attr("disabled", false);
  }

  // Enable all checkboxes in all tables
  function enableAllCheckboxes() {
    $("#table-list input[type='checkbox']").attr("disabled", false);
  }

  // Count the number of selected checkboxes in a table
  function countSelectedCheckboxes(table) {
    return $("#table-list input[name^='" + table + "']:checked").length;
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
    $("#table-list input[type='checkbox']").click(function() {
      var table = $(this).attr("name").split("-")[0]+"-";
      disableCheckboxes(table);

      if (countSelectedCheckboxes(table) >= 4) {
        disableAllCheckboxes();
      }
      if (countSelectedCheckboxes(table) < 4) {
        enableCheckboxes(table);
      }
      if (countSelectedCheckboxes(table) === 0) {
        enableAllCheckboxes();
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
				$("<script>").attr("type", "module").attr("src", "select_vis.js?v=" + new Date().getTime()).appendTo("#right");
        $("<script>").attr("type", "module").attr("src", "filter.js?v=" + new Date().getTime()).appendTo("#mid");
			},
			error: function(xhr, status, error) {
				alert(xhr.responseText);
			}
		});
  });
});
