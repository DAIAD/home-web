const develop = (process.env.NODE_ENV !== 'production');

var { createStore, compose, applyMiddleware } = require('redux');

var thunkMiddleware = require('redux-thunk');
var logger = require('redux-logger');

var { routerMiddleware } = require('react-router-redux');

var rootReducer = require('../reducers/root');

var middleware = [
  thunkMiddleware,
  routerMiddleware(history),
];

if (develop) {
  middleware.push(logger());
}

var configureStore = function(history, initialState) {
  return createStore(rootReducer,
                     initialState,
                     applyMiddleware(...middleware)
                    );
};

module.exports = configureStore;
