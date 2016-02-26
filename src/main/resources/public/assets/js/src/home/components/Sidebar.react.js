var React = require('react');

var Sidebar = React.createClass({
	render: function() {
		return (
			<aside className="sidebar">
				{ this.props.children }
			</aside>
		);
	}
});

module.exports = Sidebar;
