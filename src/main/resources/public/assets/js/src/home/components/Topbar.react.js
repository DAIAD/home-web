var React = require('react');

var Topbar = React.createClass({
	render: function() {
		return (
			<div className="top-bar">
				{ this.props.children }
			</div>
		);
	}
});

module.exports = Topbar;
