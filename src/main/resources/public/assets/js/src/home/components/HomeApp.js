// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var ReactIntl = require('react-intl');

var injectIntl = require('react-intl').injectIntl;
// Stores
var UserStore = require('../stores/UserStore');
var LocaleStore = require('../stores/LocaleStore');

// Components
var ContentRoot = require('./ContentRoot');

// Actions
var HomeActions = require('../actions/HomeActions');

var Router = require('react-router').Router;
var BrowserHistory = require('history');
var Routes = require('../Routes');
const history = BrowserHistory.useBasename(BrowserHistory.createHistory)({
	basename: '/home/'
});



// HomeApp
var HomeApp = React.createClass({

	getInitialState: function() {
		return {
			loading: false,
			isAuthenticated: UserStore.isAuthenticated(),
			locale: LocaleStore.getDefaultLocale()
		};
	},

	componentWillMount : function() {
		HomeActions.setLocale(this.props.locale);

		if(this.props.reload) {
			this.setState({ loading : true });

			HomeActions.refreshProfile();
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
				<ReactIntl.IntlProvider 
					locale={properties.locale}
					messages={LocaleStore.getMessages()} >
					<Router 
						history={history} 
						routes={Routes} 
						/>
		</ReactIntl.IntlProvider>
			);
		} else {
			return null;
		}
	},
	/*
	 <ContentRoot 
						locale={this.state.locale} 
						isAuthenticated={this.state.isAuthenticated} 
					/>
					*/
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

module.exports = injectIntl(HomeApp);
