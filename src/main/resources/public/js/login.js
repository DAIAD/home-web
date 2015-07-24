var model = {
	timeout : {
		error : null
	},
	suspendEvents : false
};

function suspendUI() {
	model.suspendEvents = true;
}

function resumeUI() {
	model.suspendEvents = false;
}

$(function() {
	$('.action-login').click(function(e) {
		e.preventDefault();
		login();
	});
});


function login() {
	suspendUI();
	
	$('#error-panel').hide();

	if(model.timeout.error) {
		clearTimeout(model.timeout.error);
	}
	model.timeout.error = null;

	var url = $('.form-login').attr('action');

	var data = {
		username : $('#username').val(),
		password : $('#password').val(),
		_csrf : $('meta[name=_csrf]').attr('content')
	};

	$.ajax({
		type : "POST",
		url : url,
		data : data,
		beforeSend : function(xhr) {
			xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]')
					.attr('content'));
		}
	}).done(function(data, textStatus, request) {
		if (data.connected) {
			var crsf = request.getResponseHeader('X-CSRF-TOKEN');

			$('meta[name=_csrf]').attr('content', crsf);
			$('input[name=_csrf]').val(crsf);

			window.location.href = '/'; 
		} else {
			$('#password').val('');

			$('#error-text').html(data.errors[0].description);
			$('#error-panel').show();
			model.timeout.error = setTimeout(function() {
				model.timeout.error = null;
				$('#error-panel').fadeOut(1000);
			}, 4000);
		}
	}).fail(function(jqXHR, textStatus, errorThrown) {
		switch (jqXHR.status) {
		case 403:
			$('#error-text').html('Authentication has failed. Please try again.');
			$('#error-panel').show();
			
			model.timeout.error = setTimeout(function() {
				model.timeout.error = null;
				$('#error-panel').fadeOut(1000);
			}, 4000);
			break;
		}
	}).always(function() {
		resumeUI();
	});
}

