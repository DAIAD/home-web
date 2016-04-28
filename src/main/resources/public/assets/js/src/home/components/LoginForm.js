// Dependencies
var React = require('react');
var FormattedMessage = require('react-intl').FormattedMessage;
const { IMAGES } = require('../constants/HomeConstants');
var classNames = require('classnames');


var Login = React.createClass({
  onLogin: function(e) {
    e.preventDefault();
    this.props.login(this.refs.username.value, this.refs.password.value);
  },
  render: function() {
    const { intl, isAuthenticated, action } = this.props;
    const _t = intl.formatMessage;
    return isAuthenticated ? (
      <div/>
    ) : (
    <div className="form-login-container">
      <h5><FormattedMessage id="section.login" /></h5>
      <form key="login" className="form-login" action={this.props.action}>
        <div className="form-group">
          <input id="username" name="username" type="text" ref="username"
            placeholder={_t({ id: 'loginForm.placehoder.username'})} className="form-control" />
        </div>
        <div className="form-group" >
          <input id="password" name="password" type="password" ref="password"
            placeholder={_t({ id: 'loginForm.placehoder.password'})} className="form-control" />
        </div>
        <button type="submit"
            className="btn btn-primary action-login"
            onClick={this.onLogin} >
          <FormattedMessage id="loginForm.button.signin" />
        </button> 
      </form>
      
      <div className="login-errors">
          {
            this.props.errors?<span><img src={`${IMAGES}/warning.svg`}/><FormattedMessage id={`errors.${this.props.errors}`} /></span>:<div/>
        }
      </div>
  </div>
    );
  }

});


var Logout = React.createClass({
  
  onLogout: function(e) {
    e.preventDefault();
    this.props.logout();
  },
  render: function() {
    var _t = this.props.intl.formatMessage;
    if(!this.props.isAuthenticated) {
      return (<div/>);
    }
    return (
      <a id="logout"
        className="logout"
        title={_t({id: "loginForm.button.signout"})}
        onClick={this.onLogout}
        type="submit">
          <i className={classNames("fa", "fa-md", "fa-sign-out", "navy")}/>
        </a>
      );
    } 
});

module.exports = {
  Login,
  Logout
};
