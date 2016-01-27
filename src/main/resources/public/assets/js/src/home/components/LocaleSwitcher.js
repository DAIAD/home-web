// Dependencies
var React = require('react');

var Bootstrap = require('react-bootstrap');

var injectIntl = require('react-intl').injectIntl;

var IntlMixin = require('react-intl').IntlMixin;

// Stores
var LocaleStore = require('../stores/LocaleStore');

// Actions
var HomeActions = require('../actions/HomeActions');


// Components
var bs = require('react-bootstrap');

var Link = require('react-router').Link;

/* Locale switcher */
var LocaleSwitcher = React.createClass({
	//mixins: [IntlMixin],

	handleChange: function(e, value) {
		HomeActions.setLocale(value);

	},
	render: function() {
		var _t = this.props.intl.formatMessage;
		var translationKey = 'locale.' + LocaleStore.getLocale();
		return (
			<div className="language-switcher">
				<bs.DropdownButton
					title={_t({ id: translationKey})}
					id="language-switcher"
					defaultValue={LocaleStore.getLocale()}
					onSelect={this.handleChange}>
					{
						LocaleStore.getLocales().map(function(locale) {
							var translationKey = 'locale.' + locale;
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

module.exports = injectIntl(LocaleSwitcher);
