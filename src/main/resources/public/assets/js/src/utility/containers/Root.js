var React = require('react');
var { Provider } = require('react-redux');
var { Router } = require('react-router');

var routes = require('../routing/routes');

var Root = React.createClass({
	propTypes: {
		store: React.PropTypes.object.isRequired,
		history: React.PropTypes.object.isRequired
	},
	
	render: function() {
		const { store, history, ...others } = this.props;

		return (
			<Provider store={store}>
				<Router history={history} routes={routes} />
			</Provider>
		);
	}
});

module.exports = Root;
