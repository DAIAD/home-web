var redux = require('redux');

var createStore = redux.createStore;
var applyMiddleware = redux.applyMiddleware;

var thunkMiddleware = require('redux-thunk');
var logger = require('redux-logger');

var rootReducer = require('../reducers/root');
//var initialState = require('../initialState');

module.exports = createStore(rootReducer,
														applyMiddleware(thunkMiddleware, logger()) 
														);
