$(document).ready(function() {

  // Disable all checkboxes except for the ones in the clicked table
  function disableCheckboxes(table) {
    $("input[type='checkbox']").not("input[name^='" + table + "']").attr("disabled", true);
  }

  // Disable all checkboxes if the maximum number of checkboxes is checked
  function disableAllCheckboxes() {
    $("input[type='checkbox']").not(":checked").attr("disabled", true);
  }

  // Enable checkboxes in a certain table
  function enableCheckboxes(table) {
    $("input[type='checkbox'][name^='" + table + "']").attr("disabled", false);
  }

  // Enable all checkboxes in all tables
  function enableAllCheckboxes() {
    $("input[type='checkbox']").attr("disabled", false);
  }

  // Count the number of selected checkboxes in a table
  function countSelectedCheckboxes(table) {
    return $("input[name^='" + table + "']:checked").length;
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
      var table = $(this).attr("name").split("-")[0];
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

//    $.post("/process-selection", { attributes: JSON.stringify(selectedAttributes) }, function(data) {
//      alert("status " + data);
//    });

		$.ajax({
			url: "/process-selection",
			type: "POST",
			contentType: "application/json",
			data: JSON.stringify(selectedAttributes),
			success: function(data) {
			  alert("status " + data);
				$("<script>").attr("type", "module").attr("src", "select_vis.js").appendTo("body");
			},
			error: function(xhr, status, error) {
				alert(xhr.responseText);
			}
		});

  });
});
