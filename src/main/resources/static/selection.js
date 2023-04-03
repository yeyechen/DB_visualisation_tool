$(document).ready(function() {
  $.ajax({
    url: '/mondial/options',
    success: function(options) {
      var optionSelect = $('#option-select');
      $.each(options, function(index, option) {
        optionSelect.append($('<option></option>').val(option).text(option));
      });
    }
  });
});