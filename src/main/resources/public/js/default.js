var daiad = {};

function suspendUI() {

}

function resumeUI() {

}

$(function() {
	$('.action-login').click(function(e) {
		e.preventDefault();
		login();
	});
});


function login() {
	var url = $('.form-login').attr('action');

	var data = {
		username : $('#username').val(),
		password : $('#password').val(),
		_csrf : $('meta[name=_csrf]').attr('content')
	};

	suspendUI();
	
	$.ajax(
			{
				type : "POST",
				url : url,
				data : data,
				beforeSend : function(xhr) {
					xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]')
							.attr('content'));
				}
			}).done(
			function(data, textStatus, request) {
				if (data.connected) {
					var crsf = request.getResponseHeader('X-CSRF-TOKEN');

					$('meta[name=_csrf]').attr('content', crsf);
					$('input[name=_csrf]').val(crsf);

					window.location.reload(); 
				} else {
					$('#password').val('');

					$('#authFailedModal').modal('show')
				}
			}).fail(function(jqXHR, textStatus, errorThrown) {
		switch (jqXHR.status) {
		case 403:
			$('#password').val('');

			$('#authFailedModal').modal('show')
			break;
		}
	}).always(function() {
		resumeUI();
	});
}

