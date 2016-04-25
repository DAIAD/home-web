var React = require('react');
var ReactDOM = require('react-dom');
var { syncHistoryWithStore } = require('react-router-redux');

var history = require('./routing/history');
var configureStore = require('./store/configureStore');

var Root = require('./containers/Root');

var { setLocale } = require('./actions/LocaleActions');
var { refreshProfile } = require('./actions/SessionActions');

var store = configureStore(history);
history = syncHistoryWithStore(history, store);

var init = function() {
	ReactDOM.render(
		<Root store={store} history={history} />,
		document.getElementById('root')
	);
};

store.dispatch(setLocale(properties.locale, true)).then(function() {
	if (properties.reload){
		store.dispatch(refreshProfile()).then(function() {
			init();
	 	});
	} else {
		init();
	}
});
