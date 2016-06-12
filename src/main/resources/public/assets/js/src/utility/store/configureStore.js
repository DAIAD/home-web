const develop = (process.env.NODE_ENV !== 'production');

var {createStore, applyMiddleware} = require('redux');
var thunkMiddleware = require('redux-thunk');
var ReduxLogger = require('redux-logger');
var {routerMiddleware}  = require('react-router-redux');

var rootReducer = require('../reducers/index');

var middleware = [
  thunkMiddleware,
  routerMiddleware(history),
];

if (develop) {
  // The logger middleware should always be last
  middleware.push(ReduxLogger());
}

function configureStore(history, initialState) {
  return createStore(rootReducer, initialState, applyMiddleware(...middleware));
}

module.exports = configureStore;
