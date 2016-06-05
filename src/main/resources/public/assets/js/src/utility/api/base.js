var $ = require('jquery');
require('es6-promise').polyfill();

function updateCsrfToken(crsf) {
  $('meta[name=_csrf]').attr('content', crsf);
  $('input[name=_csrf]').val(crsf);
}

var sendFile = function(url, files, data) {
  var form = new FormData();

  for ( var prop in data) {
    form.append(prop, data[prop]);
  }

  if (files) {
    for (var f = 0; f < files.length; f++) {
      form.append('files', files[f]);
    }
  }

  var request = {
    url : url,
    type : 'POST',
    data : form,
    enctype : 'multipart/form-data',
    processData : false,
    contentType : false,
    cache : false,
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
        case 401:
          if (jqXHR.getResponseHeader("X-Require-Authentication")) {
            document.cookie = 'daiad-utility-session=false; path=/;expires=Thu, 01 Jan 1970 00:00:01 GMT;';
          }
          break;
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

var sendRequest = function(url, data, contentType, method) {
  var request = {
    type : method || (data ? 'POST' : 'GET'),
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
        case 401:
          if (jqXHR.getResponseHeader("X-Require-Authentication")) {
            document.cookie = 'daiad-utility-session=false; path=/;expires=Thu, 01 Jan 1970 00:00:01 GMT;';
          }
          break;
        case 400:
        case 403:
        case 404:
          status = jqXHR.status;
          break;
        default:
          status = 500;
          break;
      }

      var errorCodeSuffix = status;

      if ((jqXHR.responseJSON) && (jqXHR.responseJSON.errors) && (jqXHR.responseJSON.errors.length > 0)) {
        errorCodeSuffix = jqXHR.responseJSON.errors[0].code;
      }

      var errors = [
        {
          code : 'Error.' + errorCodeSuffix,
          description : 'Error.' + errorCodeSuffix,
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
  json : function(url, data, method) {
    var serializedData = data;

    if ((data) && (typeof data === 'object')) {
      serializedData = JSON.stringify(data);
    }

    return sendRequest(url, serializedData, 'application/json; charset=UTF-8', method);
  },
  sendFile : function(url, files, data) {
    return sendFile(url, files, data);
  }
};

module.exports = api;
