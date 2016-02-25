// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var ReduxProvider = require('react-redux').Provider;
var store = require('./store/configureStore');
var BrowserHistory = require('history');
//var browserHistory=require('react-router').browserHistory;

var getDefaultDevice = require('./utils/device').getDefaultDevice;

var Router = require('react-router').Router;
//require('babel-polyfill');

const history = BrowserHistory.useBasename(BrowserHistory.createHistory)({
	basename: '/home/'
});


//Actions
var LocaleActions = require('./actions/LocaleActions');
var DeviceActions = require('./actions/DeviceActions');
var UserActions = require('./actions/UserActions');

//Components
var routes = require('./routes');


store.dispatch(LocaleActions.setLocale(properties.locale)).then(function() {
	if (properties.reload){
		store.dispatch(UserActions.refreshProfile()).then(function(response) {

			const devices = response.profile.devices;
			const device = getDefaultDevice(devices);
			store.dispatch(DeviceActions.setActiveDevice(device.deviceKey));
			
			init();
		}, function(error) {
				console.log('refresh profile problem');
				console.log(error);
});
	}
	else {
		init();
	}
}, function(e) {
		console.log('set locale problem');
		console.log(e);
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
