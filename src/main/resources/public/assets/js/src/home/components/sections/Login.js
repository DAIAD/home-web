var React = require('react');
var { injectIntl, FormattedMessage } = require('react-intl');

var MainSection = require('../layout/MainSection');
var { Login } = require('../LoginForm');


var LoginPage = React.createClass({
  render: function() {
    return (
      <MainSection id="section.login">
        <Login {...this.props} />
      </MainSection>        
    );
  }
});

LoginPage = injectIntl(LoginPage);
module.exports = LoginPage;
