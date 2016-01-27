var React = require('react');
var ReactDOM = require('react-dom');
var ReactIntl = require('react-intl');
var ContentRoot = require('./ContentRoot');

var UserStore = require('../stores/UserStore');
var LocaleStore = require('../stores/LocaleStore');

var UtilityActions = require('../actions/UtilityActions');

var UtilityApp = React.createClass({

	getInitialState: function() {
		return {
			loading: false,
			isAuthenticated: UserStore.isAuthenticated(),
			locale: LocaleStore.getLocale()
		};
	},

	componentWillMount : function() {
		UtilityActions.setLocale(this.props.locale);

		if(this.props.reload) {
			this.setState({ loading : true });

			UtilityActions.refreshProfile();
		}
	},

	componentDidMount: function() {
		UserStore.addLoginListener(this._onLogin);
		UserStore.addLogoutListener(this._onLogout);

		UserStore.addProfileRefreshListener(this._onProfileRefresh);

		LocaleStore.addLocaleChangeListener(this._onLocaleChange);
	},

	componentWillUnmount: function() {
		UserStore.removeLoginListener(this._onLogin);
		UserStore.removeLogoutListener(this._onLogout);

		UserStore.removeProfileRefreshListener(this._onProfileRefresh);

		LocaleStore.removeLocaleChangeListener(this._onLocaleChange);
	},

	render: function() {
		if((LocaleStore.isLoaded()) && (!this.state.loading)) {
			return (
				<ReactIntl.IntlProvider locale = { this.props.locale }
										messages = { LocaleStore.getMessages() } >
					<ContentRoot locale = { this.state.locale } isAuthenticated = { this.state.isAuthenticated } />
				</ReactIntl.IntlProvider>
			);
		} else {
			return null;
		}
	},

	_onLocaleChange: function(args) {
		this.setState({ locale : LocaleStore.getLocale()});
	},

	_onLogin: function(args) {
		this.setState({
			isAuthenticated : UserStore.isAuthenticated()
		});
	},

	_onLogout: function() {
		this.setState({
			isAuthenticated : false
		});
	},

	_onProfileRefresh: function() {
		this.setState({
			loading : false,
			isAuthenticated : UserStore.isAuthenticated()
		});
	}

});

module.exports = UtilityApp;
