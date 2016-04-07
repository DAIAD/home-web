var api = require('./base');

var LocaleAPI = {
	fetchMessages: function(locale) {
		return api.json('/assets/js/build/utility/i18n/' + locale + '.js');
	}	
};

module.exports = LocaleAPI;
