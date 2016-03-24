var { createStore, applyMiddleware } = require('redux');
var thunkMiddleware = require('redux-thunk');

var { routerMiddleware }  = require('react-router-redux');

var rootReducer = require('../reducers');

function configureStore(history, initialState) {
	return createStore(
		rootReducer,
		initialState,
		applyMiddleware(thunkMiddleware, routerMiddleware(history))
	);
}

module.exports = configureStore;
