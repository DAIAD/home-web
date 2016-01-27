var React = require('react');

var MainSection = React.createClass({
	render: function() {
		return (
			<div className="primary">
					<h2>{this.props.title}</h2>
					{this.props.children}
			</div>
		);
	}
});

module.exports = MainSection;
