// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var ReduxProvider = require('react-redux').Provider;
var store = require('./store/configureStore');
var BrowserHistory = require('history');
var Router = require('react-router').Router;

const history = BrowserHistory.useBasename(BrowserHistory.createHistory)({
	basename: '/home/'
});

//Actions
var LocaleActions = require('./actions/LocaleActions');
var UserActions = require('./actions/UserActions');

//Components
var routes = require('./routes');


//Check if already logged-in template property
if (properties.reload){
	store.dispatch(UserActions.refreshProfile());
}

//Set locale from template property
store.dispatch(LocaleActions.setLocale(properties.locale));

ReactDOM.render(
	<ReduxProvider store={store}>
		<Router 
			history={history}
			routes={routes}
		/>
	</ReduxProvider>,
document.getElementById('app'));
