var $ = require('jquery');
require('es6-promise').polyfill();

function updateCsrfToken(crsf) {
  $('meta[name=_csrf]').attr('content', crsf);
  $('input[name=_csrf]').val(crsf);
}

var sendRequest = function(url, data, contentType) {
  var request = {
    type : data ? 'POST' : 'GET',
    dataType : 'json',
    contentType : contentType,
    data : data,
    url : url,
    beforeSend : function(xhr) {
      xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr('content'));
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
        case 400:
        case 403:
        case 404:
          status = jqXHR.status;
          break;
        default:
          status = 500;
          break;
      }

      var errors = [
        {
          code : 'Error.' + status,
          description : 'Error.' + status,
          details : errorThrown
        }
      ];

      reject(errors);
    });
  });
};

var api = {
  submit : function(url, data) {
    if ((data) && (typeof data === 'object')) {
      data._csrf = $('meta[name=_csrf]').attr('content');
    }

    return sendRequest(url, data, 'application/x-www-form-urlencoded; charset=UTF-8');
  },
  json : function(url, data) {
    var serializedData = data;

    if ((data) && (typeof data === 'object')) {
      serializedData = JSON.stringify(data);
    }

    return sendRequest(url, serializedData, 'application/json; charset=UTF-8');
  }
};

module.exports = api;