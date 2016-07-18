const develop = (process.env.NODE_ENV !== 'production');

var { createStore, compose, applyMiddleware } = require('redux');
var { routerMiddleware } = require('react-router-redux');

var thunkMiddleware = require('redux-thunk');
var logger = require('redux-logger');

var rootReducer = require('../reducers/root');

var history = require('../routing/history');


var middleware = [
  thunkMiddleware,
  routerMiddleware(history),
];

if (develop) {
  middleware.push(logger());
}

let store = createStore(rootReducer,
                     applyMiddleware(...middleware)
                    );

module.exports = store;

