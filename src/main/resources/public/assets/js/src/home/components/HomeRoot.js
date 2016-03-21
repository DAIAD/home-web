// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var { IntlProvider } = require('react-intl');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

//Constants
var Constant = require('../constants/HomeConstants');

// Components
var Header = require('../components/Header');
var MainSection = require('../components/MainSection');
var Footer = require('../components/Footer');
var LoginPage = require('../components/sections/Login');

// Actions
var { login, logout } = require('../actions/UserActions');
var { setLocale } = require('../actions/LocaleActions');

var { getDeviceCount } = require('../utils/device');


var HomeRoot = React.createClass({
  render: function() {
    return (
      <IntlProvider 
        locale={this.props.locale.locale}
        messages={this.props.locale.messages} >
        <div>
          {
            (() => {
              if (this.props.loading){
                return (
                  <div>
                    <img className="preloader" src="/assets/images/png/preloader-counterclock.png" />
                    <img className="preloader-inner" src="/assets/images/png/preloader-clockwise.png" />
                  </div>
                  );
              }
              })()
          }
          <Header
            data={Constant.data}
            firstname={this.props.user.profile.firstname}
            deviceCount={this.props.deviceCount}
            isAuthenticated={this.props.user.isAuthenticated}
            locale={this.props.locale.locale}
            logout={this.props.logout} 
            setLocale={this.props.setLocale}
          />
          
          {
            (() => {
              if (this.props.user.isAuthenticated) {
                    return (
                        this.props.children
                      );    
              }
              else {
                return (
                  <LoginPage 
                    isAuthenticated = {this.props.user.isAuthenticated}
                    errors = {this.props.user.status.errors}
                    login = {this.props.login}
                    logout = {this.props.logout} />
                  );
              }
          
              })()
            }

            <Footer />
          </div>
        </IntlProvider>
      );
  }

});

module.exports = HomeRoot;
