var React = require('react');

var MainSection = require('../MainSection.react');
var Login = require('../LoginForm').Login;


var LoginPage = React.createClass({
	render: function() {
		return (
			<MainSection id="section.login">
				<Login {...this.props} style={{marginTop: '50px'}}/>
			</MainSection>				
		);
	}
});

module.exports = LoginPage;
