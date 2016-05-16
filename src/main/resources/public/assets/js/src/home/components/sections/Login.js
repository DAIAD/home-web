var React = require('react');
var { injectIntl, FormattedMessage } = require('react-intl');

var MainSection = require('../MainSection');
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

LoginPage = injectIntl(LoginPage);
module.exports = LoginPage;
