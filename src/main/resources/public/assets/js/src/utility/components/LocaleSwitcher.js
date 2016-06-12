// Dependencies
var React = require('react');
var injectIntl = require('react-intl').injectIntl;

// Actions
var LocaleActions = require('../actions/LocaleActions');

var LOCALES = require('../constants/Constants').LOCALES;

// Components
var bs = require('react-bootstrap');
var Link = require('react-router').Link;


var LocaleSwitcher = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

	handleChange: function(e, value) {
		this.props.onLocaleSwitch(value);
	},
	render: function() {	
		var locale = this.props.locale;
		var _t = this.context.intl.formatMessage;
		var translationKey = 'locale.' + locale;

		return (
				<bs.DropdownButton
					title={_t({ id: translationKey})}
					id="language-switcher"
					defaultValue={locale}
					onSelect={this.handleChange}>
					{
						LOCALES.map(function(locale) {
							var translationKey = 'locale.' + locale;
							return (
								<bs.MenuItem key={locale} eventKey={locale} value={locale} >{_t({ id: translationKey})}</bs.MenuItem>
							);
					})
					}	
        </bs.DropdownButton>
		);
	}
});

module.exports = LocaleSwitcher;
