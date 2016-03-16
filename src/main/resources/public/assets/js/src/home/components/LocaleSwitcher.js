// Dependencies
var React = require('react');
var injectIntl = require('react-intl').injectIntl;

// Actions
var LocaleActions = require('../actions/LocaleActions');

const LOCALES = require('../constants/HomeConstants').LOCALES;

// Components
var bs = require('react-bootstrap');
var Link = require('react-router').Link;


var LocaleSwitcher = React.createClass({

  handleChange: function(e, value) {
    this.props.onLocaleSwitch(value);
  },
  render: function() {
    const locale = this.props.locale;
    const _t = this.props.intl.formatMessage;
    const translationKey = `locale.${locale}`;
    return (
      <div className="language-switcher">
        <bs.DropdownButton
          title={_t({ id: translationKey})}
          id="language-switcher"
          defaultValue={locale}
          onSelect={this.handleChange}>
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
});

LocaleSwitcher = injectIntl(LocaleSwitcher);
module.exports = LocaleSwitcher;
