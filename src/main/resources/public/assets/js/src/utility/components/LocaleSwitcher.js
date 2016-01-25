// Dependencies
var React = require('react');

var Bootstrap = require('react-bootstrap');

var Select = require('./Select');

var injectIntl = require('react-intl').injectIntl;

// Components

// Stores
var LocaleStore = require('../stores/LocaleStore');

// Actions
var UtilityActions = require('../actions/UtilityActions');

var onChangeHandler =  function(event) {
	UtilityActions.setLocale(event.target.value);
};

var LocaleSwitcher = React.createClass({

	render: function() {
		var _t = this.props.intl.formatMessage;
		
		var options = LocaleStore.getLocales().map(function(locale) {
			var translationKey = 'locale.' + locale;
			return (
				<option key={locale} value={locale}>
					{_t({ id: translationKey})}
				</option>
			);
		});
		
		return (
			<Select defaultValue={LocaleStore.getLocale()} onChange={onChangeHandler.bind(this)}  data-width='110px'>
				{options}
			</Select>
		);
	}
});

module.exports = injectIntl(LocaleSwitcher);
