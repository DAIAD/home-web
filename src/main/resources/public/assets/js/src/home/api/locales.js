var callAPI = require('./base');
const { LOCALES } = require('../constants/HomeConstants');

var LocaleAPI = {
  fetchLocaleMessages: function(data) {
    const { locale } = data;
    delete data.locale;
    if (!LOCALES.includes(locale)) throw new Error(`locale ${locale} not supported`);
    return callAPI(`/assets/js/build/home/i18n/${locale}.js`, data, "GET");
  } 
};

module.exports = LocaleAPI;

