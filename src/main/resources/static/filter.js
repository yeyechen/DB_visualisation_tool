var options = {};
$(document).ready(function() {

  $("#filter-list thead").empty();
  $("#filter-list tbody").empty();
  $("#filter-form br").remove();
  $("#filter-form button").remove();

  $.get("/filter-options", function(tableData) {
    console.log("filter-options:");
    console.log(tableData);
    if (Array.isArray(tableData) && tableData.length !== 0) {
      $("#mid-heading").text("Select filter:");
      $("<tr>")
        .append($("<th>").text("Table Name"))
        .append($("<th>").text("Filtering Attributes"))
        .appendTo("#filter-list thead")

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
      if ($(this).is(":checked")) {
        if (!options[checkboxValue]) {
          options[checkboxValue] = [];
        }
      } else {
        if (options[checkboxValue]) {
          delete options[checkboxValue];
        }
      }
      console.log(options);

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

          if (typeof data[0] === "string") {
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

          if (typeof data[0] === "number") {
            var dragBarContainer = $('<div id="drag-bar-container"></div>');
            var dragBar = $('<div id="drag-bar"></div>');
            var minHandle = $('<div id="min-handle" class="handle"></div>');
            var maxHandle = $('<div id="max-handle" class="handle"></div>');
            dragBar.append(minHandle, maxHandle);
            dragBarContainer.append(dragBar);

            $optionsTd.append(dragBarContainer);

            var dragBar = $('#drag-bar');
            var minHandle = $('#min-handle');
            var maxHandle = $('#max-handle');

            var dragBarWidth = dragBar.width();
            var handleWidth = minHandle.width();

            var minValue = data[0];
            var maxValue = data[1];
            var currentMinValue = minValue;
            var currentMaxValue = maxValue;

            // Calculate the initial positions of the handles
            var minPosition = 0;
            var maxPosition = dragBarWidth - handleWidth;

            minHandle.css('left', minPosition + 'px');
            maxHandle.css('left', maxPosition + 'px');

            // Set up event listeners for dragging the handles
            minHandle.on('mousedown', startDragging);
            maxHandle.on('mousedown', startDragging);

            var minText = $("<span>").text("min");
            var maxText = $("<span>").text("max");
            var realTimeMinValue = $("<span>").text(currentMinValue);
            var realTimeMaxValue = $("<span>").text(currentMaxValue);
            var minMaxValue = $("<div>").addClass("min-max-container");
            minMaxValue.append(realTimeMinValue, realTimeMaxValue);
            var minMaxText = $("<div>").addClass("min-max-container");
            minMaxText.append(minText, maxText);


            $optionsTd.css("width", "200");
            $optionsTd.append(minMaxText);
            $optionsTd.append(minMaxValue);

            // Function to start dragging the handles
            function startDragging() {
              var handle = $(this);

              // Get the initial position of the handle
              var initialPosition = parseFloat(handle.css('left')) || 0;

              // Get the initial mouse position
              var initialMousePosition = event.clientX;

              // Calculate the offset between the mouse position and the handle position
              var offset = initialMousePosition - initialPosition;

              // Set up event listeners for dragging
              $(document).on('mousemove', drag);
              $(document).on('mouseup', stopDragging);

              // Function to handle dragging
              function drag(event) {
                // Calculate the new position of the handle based on the mouse position and the offset
                var newPosition = event.clientX - offset;

                // Limit the position within the bounds of the drag bar
                newPosition = Math.max(0, Math.min(dragBarWidth - handleWidth, newPosition));

                // Update the position of the handle
                handle.css('left', newPosition + 'px');

                // Update the current values based on the new position
                if (handle.is(minHandle)) {
                  currentMinValue = calculateValue(newPosition, dragBarWidth, minValue, maxValue);
                } else if (handle.is(maxHandle)) {
                  currentMaxValue = calculateValue(newPosition + handleWidth, dragBarWidth, minValue, maxValue);
                }

                // Update any other UI elements or perform additional actions based on the new values
                realTimeMinValue.text(Math.round(currentMinValue));
                realTimeMaxValue.text(Math.round(currentMaxValue));
                if (!options[checkboxValue]) {
                  options[checkboxValue] = [];
                }
                options[checkboxValue][0] = Math.round(currentMinValue);
                options[checkboxValue][1] = Math.round(currentMaxValue);
              }

              // Function to stop dragging
              function stopDragging() {
                // Remove the event listeners for dragging
                $(document).off('mousemove', drag);
                $(document).off('mouseup', stopDragging);

                // Perform any final actions or updates based on the new values
                // ...
              }
            }

            // Function to calculate the value based on the position within the drag bar
            function calculateValue(position, dragBarWidth, minValue, maxValue) {
              var range = maxValue - minValue;
              var normalizedPosition = position / dragBarWidth;
              return minValue + range * normalizedPosition;
            }
          }
        }
      });
    });
  });

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
    console.log(options);
  });

  $(document).on("click", "#filter-list button[type='submit']", function(event) {
    event.preventDefault();
    console.log(options);
    $.ajax({
      url: "/process-filter",
      type: "POST",
      contentType: "application/json",
      data: JSON.stringify(options),
      success: function(data) {
        showAlert("Filter Applied!");
        // reload visualisation options
        if ($("#vis-form").children().length > 0) {
          $("#vis-form").empty();
        }
        $("#select-vis-script").remove();
        $("<script>")
          .attr("type", "text/javascript")
          .attr("src", "select_vis.js?v=" + new Date().getTime())
          .attr("id", "select-vis-script")
          .appendTo("body");
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