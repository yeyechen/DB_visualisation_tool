$(document).ready(function() {
	$("#database-form").submit(function(event) {
		event.preventDefault();

		var formData = {
			dbType: $("#db-type").val(),
			host: $("#host").val(),
			port: $("#port").val(),
			databaseName: $("#database-name").val(),
			username: $("#username").val(),
			password: $("#password").val()
		};

		$.ajax({
			url: "/database-info",
			type: "POST",
			contentType: "application/json",
			data: JSON.stringify(formData),
			success: function() {
				window.history.pushState(null, null, "/selection");
				location.reload();
			},
			error: function(xhr, status, error) {
				alert(xhr.responseText);
			}
		});
	});
});
