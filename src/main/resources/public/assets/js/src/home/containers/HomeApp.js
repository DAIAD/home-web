// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var ReactIntlProvider = require('react-intl').IntlProvider;
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

// Components
var HomeRoot = require('../components/layout/HomeRoot');

// Actions
var { login, logout } = require('../actions/UserActions');
var { setLocale } = require('../actions/LocaleActions');

var { getDeviceCount } = require('../utils/device');


function mapStateToProps(state) {
    return {
      user: state.user,
      locale: state.locale,
      loading: state.user.status.isLoading || state.locale.status.isLoading || state.query.isLoading
    };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(Object.assign({}, {login, logout, setLocale}), dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  const isAuthed = stateProps.user.isAuthenticated;
  const devices = stateProps.user.profile.devices;
  return Object.assign({}, 
                       ownProps, 
                       dispatchProps,
                       Object.assign({},
                                     stateProps,
                                     { deviceCount: isAuthed?getDeviceCount(devices):0 }
                                    )
                      );
}


var HomeApp = connect(mapStateToProps, mapDispatchToProps, mergeProps)(HomeRoot);
module.exports = HomeApp; 
