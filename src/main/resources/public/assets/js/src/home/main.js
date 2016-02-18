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


store.dispatch(LocaleActions.setLocale(properties.locale)).then(function() {
	if (properties.reload){
		store.dispatch(UserActions.refreshProfile()).then(function() {
			init();
		});
	}
	else {
		init();
	}
});
		
var init = function() {
	ReactDOM.render(
		<ReduxProvider store={store}>
			<Router 
				history={history}
				routes={routes}
			/>
		</ReduxProvider>,
		document.getElementById('app'));
};
