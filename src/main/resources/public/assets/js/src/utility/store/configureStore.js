var Redux = require('redux');
var thunk = require('redux-thunk');

var history = require('../routing/history');
var { syncHistory}  = require('react-router-redux');

var rootReducer = require('../reducers');

function configureStore(initialState) {
	return Redux.createStore(
		rootReducer,
		initialState,
		Redux.applyMiddleware(thunk, syncHistory(history))
	);
}

module.exports = configureStore;