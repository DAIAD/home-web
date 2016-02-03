var React = require('react');

var MainSection = React.createClass({
	render: function() {
		return (
				<section className="main-section" >
					<div className="container">
						{this.props.children}
					</div>
				</section>
		);
	}
});

module.exports = MainSection;
