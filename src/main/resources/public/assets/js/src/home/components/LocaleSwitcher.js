// Dependencies
var React = require('react');
var { injectIntl } = require('react-intl');

// Actions
var LocaleActions = require('../actions/LocaleActions');

const LOCALES = require('../constants/HomeConstants').LOCALES;

// Components
var bs = require('react-bootstrap');
var Link = require('react-router').Link;


function LocaleSwitcher (props) {
  const { locale, intl } = props;
  const _t = intl.formatMessage;
  const translationKey = `locale.${locale}`;
  return (
    <div className="language-switcher">
      <bs.DropdownButton
        title={_t({ id: translationKey})}
        id="language-switcher"
        defaultValue={locale}
        onSelect={(e, val) => props.setLocale(val)}>
        {
          LOCALES.map(function(locale) {
            const translationKey = `locale.${locale}`;
            return (
              <bs.MenuItem key={locale} eventKey={locale} value={locale} >{_t({ id: translationKey})}</bs.MenuItem>
            );
        })
        } 
      </bs.DropdownButton>
    </div>  
  );
}

module.exports = LocaleSwitcher;
