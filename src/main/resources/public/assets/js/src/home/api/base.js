//var assign = require('object-assign');
var $ = require('jquery');

function updateCsrfToken(crsf) {
	$('meta[name=_csrf]').attr('content', crsf);
	$('input[name=_csrf]').val(crsf);
}

var callAPI = function(url, data, done, fail) {
	if (data){
		data._csrf = $('meta[name=_csrf]').attr('content');
	}
	var request = {
				type : data?'POST':'GET',
				dataType: 'json',
				data: data,
				url : url, 
				beforeSend : function(xhr) {
						xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
										'content'));
				}
		};
	
	    $.ajax(request).done(function(data, textStatus, request) {
				updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));
				done(data);			

			}).fail(function(jqXHR, textStatus, errorThrown) {
	    	updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));
					fail(errorThrown);
	        switch (jqXHR.status) {
	        case 403:
	            break;
	        }
	    });

};
module.exports = callAPI;
