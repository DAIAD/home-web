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
	$('#tabs').tab();

	var today = new Date();
	$('div.input-group.date input')
			.each(
					function(index, elem) {
						$(elem).datepicker({
							format : "dd/mm/yyyy",
							autoclose : true,
							toggleActive : true,
							orientation : 'top auto'
						});
					});

	$('#export-from').datepicker(
			'setDate',
			new Date(
					today
							.getFullYear(),
					today
							.getMonth(),
					1));

	$('#export-to').datepicker(
			'setDate',
			new Date(
					today
							.getFullYear(),
					today
							.getMonth() + 1,
					0));

	jQuery('#action-logout').click(function(e) {
		jQuery('#logout-form').submit();
	});

	$('#tabs a').click(function(e) {
		$(this).blur();
	});

	$('input.datetime-interval').click(function(e) {
		$(this).blur();
	});

	$('#export-timezone').selectpicker().change(function () {
        $('[data-id="export-timezone"]').blur();
    });
	
	jQuery('#action-export').click(function(e) {
		if(!model.suspendEvents) {
			exportData();
		}
	});
});

function exportData() {
	suspendUI();
	
	$('#export').prop('disabled', 'disabled');
	
	if(model.timeout.error) {
		clearTimeout(model.timeout.error);
	}
	model.timeout.error = null;
	
	$('#error-panel').hide();
	
	var data = {
		'from': $('#export-from').datepicker('getDate').toJSON(),
		'to': $('#export-to').datepicker('getDate').toJSON(),
    	'type':'SESSION',
		'username': null,
		'properties': [],
		'timezone': $('#export-timezone option:selected').val(),
		'properties': []
	}

	$('.export-property').each(function(index, elem) {
		if($(elem).is(':checked')) {
			data.properties.push($(elem).val());
		}
	});
	
	var options = prepareAjaxRequest('/data/export', JSON.stringify(data));
	
	$.ajax(options)
		.done(function(data) {
			if (data.success) {
				downloadData(data.token);
			} else {
				$('#error-text').html(data.errors[0].description);
				$('#error-panel').show();
				model.timeout.error = setTimeout(function() {
					model.timeout.error = null;
					$('#error-panel').fadeOut(1000);
				}, 4000);
			}
		}).fail(function(jqXHR, textStatus, errorThrown) {
			// TODO : show error message
		}).always(function() {
			$('#export').prop('disabled', null);
			resumeUI();
		});
}

function downloadData(token) {
	var content = [];
	content.push('<div id="export-download-frame" style="display: none">');
	content.push('<iframe src="/data/download/' + token + '"></iframe>');
	content.push('</div>');
	
	jQuery('#export-download-frame').remove();
    jQuery('body').append(content.join(''));
}
function prepareAjaxRequest(url, data) {
	return {
		type : 'POST',
		url: url,
		data: data,
		dataType : 'json',
		contentType : 'application/json',
		beforeSend : function(xhr) {
			xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr('content'))
		}
	};
}
