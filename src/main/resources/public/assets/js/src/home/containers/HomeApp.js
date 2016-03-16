// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var ReactIntlProvider = require('react-intl').IntlProvider;
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

var HomeApp = React.createClass({

  componentWillMount: function() {
    if (this.props.user.isAuthenticated) {
      //this.props.setDefaultActiveDevice(this.props.user.profile.devices);
    }
  },
  render: function() {
    const devices = this.props.user.profile.devices;
    return (
      <ReactIntlProvider 
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
            intl={this.props.intl}
            data={Constant.data}
            firstname={this.props.user.profile.firstname}
            deviceCount={this.props.user.isAuthenticated?getDeviceCount(devices):0}
            isAuthenticated={this.props.user.isAuthenticated}
            locale={this.props.locale.locale}
            logout={this.props.logout} 
            setLocale={this.props.setLocale}
          />
          
          {
            (() => {
              if (this.props.user.isAuthenticated) {
                  // wait until profile is loaded 
                  if (devices === undefined){
                    return (null);
                  }
                  else {
                    return (
                        this.props.children
                      );
                  }
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

        </ReactIntlProvider>
      );
  },

});


function mapStateToProps(state) {
    return {
      user: state.user,
      locale: state.locale,
      loading: state.user.status.isLoading || state.locale.status.isLoading || state.query.isLoading
    };
}

function mapDispatchToProps(dispatch) {
  console.log('mapping');
  return bindActionCreators(Object.assign({}, {login, logout, setLocale}), dispatch);
}

HomeApp = connect(mapStateToProps, mapDispatchToProps)(HomeApp);
module.exports = HomeApp; 
