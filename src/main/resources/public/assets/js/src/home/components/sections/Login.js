var React = require('react');
var { FormattedMessage } = require('react-intl');

var MainSection = require('../MainSection.react');
var Login = require('../LoginForm').Login;


var LoginPage = React.createClass({
	render: function() {
		return (
      <MainSection id="section.login">
        <h2><FormattedMessage id="section.login" /></h2>
				<Login {...this.props} style={{marginTop: '50px'}}/>
			</MainSection>				
		);
	}
});

module.exports = LoginPage;
