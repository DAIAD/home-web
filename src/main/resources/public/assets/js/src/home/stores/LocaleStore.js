// Dependencies
var events = require('events');
var assign = require('object-assign');
var $ = require('jquery');

// Dispatcher
var AppDispatcher = require('../dispatcher/AppDispatcher');

// Constants
var HomeConstants = require('../constants/HomeConstants');

var LOCALE_CHANGE = 'LOCALE_CHANGE';
var _defaultLocale = 'en';

function flattenMessages(nestedMessages, prefix) {
    return Object.keys(nestedMessages).reduce((messages, key) => {
        var value = nestedMessages[key];
        var prefixedKey = prefix ? `${prefix}.${key}` : key;

        if (typeof value === 'string') {
            messages[prefixedKey] = value;
        } else {
            Object.assign(messages, flattenMessages(value, prefixedKey));
        }

        return messages;
    }, {});
}

var _model = {
	locale: _defaultLocale,
	data: {
		en: {
			isLoaded: false,
			messages: {}
		},
		el: {
			isLoaded: false,
			messages: {}
		},
		es: {
			isLoaded: false,
			messages: {}
		},
		de: {
			isLoaded: false,
			messages: {}
		}
	}
};
function updateCsrfToken(crsf) {
	$('meta[name=_csrf]').attr('content', crsf);
	$('input[name=_csrf]').val(crsf);
}

function loadLocale(locale) {
	//TODO: fix this
	if (locale===undefined){
		locale = _defaultLocale;
	}
	if(_model.data[locale].isLoaded) {
		_model.locale = locale;
		
		LocaleStore.emitLocaleChange();
	} else {
	    var request = {
	        type : 'GET',
	        dataType: 'json',
	        url : '/assets/js/build/home/i18n/' + locale + '.js',
	        beforeSend : function(xhr) {
	            xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
	                    'content'));
	        }
	    };
	
	    $.ajax(request).done(function(data, textStatus, request) {
	    	updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));

	        _model.locale = locale;
	        _model.data[locale].isLoaded = true;
	        _model.data[locale].messages = flattenMessages(data);

	        LocaleStore.emitLocaleChange();
	    }).fail(function(jqXHR, textStatus, errorThrown) {
	    	updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));
	    	
	        switch (jqXHR.status) {
	        case 403:
	            break;
	        }
	    });
	}
}

var LocaleStore = assign({}, events.EventEmitter.prototype, {
		
    getLocale : function() {
        return _model.locale;
		},
		getDefaultLocale: function() {
			return _defaultLocale;
		},

    getLocales : function() {
        return Object.keys(_model.data);
    },

		getMessages : function() {
			// merge with default locale messages to avoid missing translations
			return assign({}, _model.data[this.getDefaultLocale()].messages, _model.data[_model.locale].messages);
			//return _model.data[_model.locale].messages;
    },
    
    isLoaded: function() {
    	return _model.data[_model.locale].isLoaded;
    },

    addLocaleChangeListener : function(callback) {
        this.on(LOCALE_CHANGE, callback);
    },

    removeLocaleChangeListener : function(callback) {
        this.removeListener(LOCALE_CHANGE, callback);
    },
  
    emitLocaleChange: function() {
        this.emit(LOCALE_CHANGE, { locale : _model.locale});
    }
        
});

AppDispatcher.register(function(message) {
    switch (message.action) {
			case HomeConstants.LOCALE_CHANGE:
    		loadLocale(message.data.locale);
        break;

    default:
        break;
    }
});

module.exports = LocaleStore;
