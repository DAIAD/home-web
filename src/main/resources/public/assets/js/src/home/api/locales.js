var callAPI = require('./base');

var LocaleAPI = {
	fetchLocaleMessages: function(locale, cb, fl) {
		return callAPI('/assets/js/build/home/i18n/' + locale + '.js', null, cb, fl);
	}	
};

module.exports = LocaleAPI;

