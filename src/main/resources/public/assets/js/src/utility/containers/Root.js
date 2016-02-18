var React = require('react');
var { Provider } = require('react-redux');
var { Router } = require('react-router');

var routes = require('../routing/routes');
var history = require('../routing/history');

var Root = React.createClass({
	propTypes: {
		store: React.PropTypes.object.isRequired
	},
	
	render: function() {
		const { store } = this.props;
    
		return (
			<Provider store={store}>
				<Router history={history} routes={routes} />
			</Provider>
		);
	}
});

module.exports = Root;
