var callAPI = require('./base');

var LocaleAPI = {
	fetchMessages: function(locale) {
		return callAPI('/assets/js/build/utility/i18n/' + locale + '.js');
	}	
};

module.exports = LocaleAPI;
