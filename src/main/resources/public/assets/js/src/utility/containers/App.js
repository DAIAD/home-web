var React = require('react');
var ReactDOM = require('react-dom');
var { connect } = require('react-redux');
var ReactIntl = require('react-intl');

var Locale = {};
Locale.en = require('react-intl/locale-data/en');
Locale.el = require('react-intl/locale-data/el');
Locale.es = require('react-intl/locale-data/es');
Locale.de = require('react-intl/locale-data/de');

ReactIntl.addLocaleData(Locale.en);
ReactIntl.addLocaleData(Locale.el);
ReactIntl.addLocaleData(Locale.es);
ReactIntl.addLocaleData(Locale.de);

var ContentRoot = require('./ContentRoot');

var App = React.createClass({
	render: function() {
		return (
			<ReactIntl.IntlProvider locale = { this.props.locale } messages = { this.props.messages } >
				<ContentRoot locale = { this.props.locale } >
					{this.props.children}
				</ContentRoot>
			</ReactIntl.IntlProvider>
		);
	}
});

function mapStateToProps(state) {
	return {
		locale: state.i18n.locale,
	    messages: state.i18n.data[state.i18n.locale].messages
	};
}

module.exports = connect(mapStateToProps)(App);
