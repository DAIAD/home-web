var React = require('react');

var MainSection = require('../MainSection.react');
var LoginForm = require('../LoginForm');

var LoginPage = React.createClass({

	render: function() {
	return (
			<MainSection title="Welcome to DAIAD Home" >
				<section className="section-login">
				<LoginForm 	
					action="login"
					user = {this.context.user}
				/>
			</section>
		</MainSection>
	);
}
});


module.exports = LoginPage;
