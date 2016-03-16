// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var ReduxProvider = require('react-redux').Provider;
var configureStore = require('./store/configureStore');
var Router = require('react-router').Router;
require('babel-polyfill');

var history = require('./routing/history');
var routes = require('./routing/routes');
var store = configureStore(history);

var { syncHistoryWithStore } = require('react-router-redux');
history = syncHistoryWithStore(history, store);


//Actions
var LocaleActions = require('./actions/LocaleActions');
var UserActions = require('./actions/UserActions');


store.dispatch(LocaleActions.setLocale(properties.locale))
  .then((response) => {
    if (properties.reload){
      store.dispatch(UserActions.refreshProfile())
        .then((response) =>{
          init();
        });
    }
    else {
      init();
    }
});
    
const init = function() {
  ReactDOM.render(
    <ReduxProvider store={store}>
      <Router 
        history={history}
        routes={routes}
      />
    </ReduxProvider>,
    document.getElementById('app'));
};
