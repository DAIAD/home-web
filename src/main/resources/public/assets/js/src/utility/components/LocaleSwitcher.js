var React = require('react');
var injectIntl = require('react-intl').injectIntl;
var Bootstrap = require('react-bootstrap');

Bootstrap.Select = require('./Select');

var LocaleStore = require('../stores/LocaleStore');

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
			<Bootstrap.Select defaultValue={LocaleStore.getLocale()} onChange={onChangeHandler.bind(this)}  data-width='110px'>
				{options}
			</Bootstrap.Select>
		);
	}
});

module.exports = injectIntl(LocaleSwitcher);
