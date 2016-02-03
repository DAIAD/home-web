var React = require('react');

var MainSection = require('../MainSection.react');
var LoginForm = require('../LoginForm');


//var parseDate = d3.time.format("%YM%m").parse;

var History = React.createClass({
	render: function() {
		var sampling = [];
		var average = [];
		for (var i = 0; i < 200; i++) {
			sampling.push([i, Math.random() * i * 4]);
			average.push([i, 200]);
		}
		return (
			<section className="section-login">
				<MainSection title="Welcome to DAIAD Home" >
					
					<LoginForm 	action="login"
						isAuthenticated = { this.props.isAuthenticated } />

				</MainSection>
			</section>
		);
	}
});

module.exports = History;
