var { createStore, compose, applyMiddleware } = require('redux');

var thunkMiddleware = require('redux-thunk');
var logger = require('redux-logger');

var { routerMiddleware } = require('react-router-redux');

var rootReducer = require('../reducers/root');

var configureStore = function(history, initialState) {
  return createStore(rootReducer,
                     initialState,
                     //applyMiddleware(thunkMiddleware, routerMiddleware(history), logger())
                     applyMiddleware(thunkMiddleware, logger(), routerMiddleware(history))
                    );
};

module.exports = configureStore;
