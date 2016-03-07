// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var ReduxProvider = require('react-redux').Provider;
var configureStore = require('./store/configureStore');
var Router = require('react-router').Router;
require('babel-polyfill');

const history = require('./routing/history');
var routes = require('./routing/routes');

var store = configureStore();

//Actions
var LocaleActions = require('./actions/LocaleActions');
var DeviceActions = require('./actions/DeviceActions');
var UserActions = require('./actions/UserActions');

//Components

var getDefaultDevice = require('./utils/device').getDefaultDevice;


store.dispatch(LocaleActions.setLocale(properties.locale)).then(function() {
  if (properties.reload){
    store.dispatch(UserActions.refreshProfile()).then(function(response) {
      const devices = response.profile.devices;
      const device = getDefaultDevice(devices);
      if (device){
        store.dispatch(DeviceActions.setActiveDevice(device.deviceKey));
      }
      
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
