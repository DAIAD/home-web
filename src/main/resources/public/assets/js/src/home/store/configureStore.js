var redux = require('redux');

var createStore = redux.createStore;
var applyMiddleware = redux.applyMiddleware;

var thunkMiddleware = require('redux-thunk');
var logger = require('redux-logger');
var history = require('../routing/history');

var { syncHistory } = require('react-router-redux');

var rootReducer = require('../reducers/root');

var configureStore = function(initialState) {
	return createStore(rootReducer,
										 initialState,
										 applyMiddleware(thunkMiddleware, logger()),
										 applyMiddleware(thunkMiddleware, syncHistory(history))
										);
};

module.exports = configureStore;
