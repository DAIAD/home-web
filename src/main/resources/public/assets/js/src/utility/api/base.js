var $ = require('jquery');
require('es6-promise').polyfill();

function updateCsrfToken(crsf) {
	$('meta[name=_csrf]').attr('content', crsf);
	$('input[name=_csrf]').val(crsf);
}

var callAPI = function(url, data) {
	if (data) {
		data._csrf = $('meta[name=_csrf]').attr('content');
	}

	var request = {
		type : data ? 'POST' : 'GET',
		dataType : 'json',
		data : data,
		url : url,
		beforeSend : function(xhr) {
			xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
					'content'));
		}
	};

	return new Promise(function(resolve, reject) {
		$.ajax(request).done(function(data, textStatus, request) {
			updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));

			resolve(data);
		}).fail(function(jqXHR, textStatus, errorThrown) {
			updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));

			var status;

			switch (jqXHR.status) {
			case 400: case 403: case 404: case 500:
				status = jqXHR.status;
				break;
			default:
				status = 500;
				break;
			}
			
			var errors = [{
				code: 'Error.' + status,
				description: 'Error.' + status,
				details: errorThrown
			}];

			reject(errors);
		});
	});
};

module.exports = callAPI;
