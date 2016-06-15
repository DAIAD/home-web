// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

// Components
var HomeRoot = require('../components/layout/HomeRoot');

// Actions
var { login, logout } = require('../actions/UserActions');
var { setLocale } = require('../actions/LocaleActions');
var { linkToMessage:linkToNotification } = require('../actions/MessageActions');
var { dismissError } = require('../actions/QueryActions');

var { getDeviceCount } = require('../utils/device');
var { combineMessages } = require('../utils/messages');

function mapStateToProps(state) {
    return {
      user: state.user,
      locale: state.locale,
      errors: state.query.errors,
      loading: state.user.status.isLoading || state.locale.status.isLoading || state.query.isLoading,
      messages: combineMessages([
        {name: 'alerts', values: state.messages.alerts}, 
        {name: 'announcements', values: state.messages.announcements}, 
        {name:'recommendations', values: state.messages.recommendations}, 
        {name: 'tips', values: state.messages.tips}
      ]),
    };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(Object.assign({}, {login, logout, setLocale, linkToNotification, dismissError}), dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  const isAuthed = stateProps.user.isAuthenticated;
  const devices = stateProps.user.profile.devices;
  return Object.assign({}, 
                       ownProps, 
                       dispatchProps,
                       stateProps,
                       { 
                         deviceCount: isAuthed?getDeviceCount(devices):0,
                         unreadNotifications: stateProps.messages.reduce(((prev, curr) => !curr.acknowledgedOn ? prev+1 : prev), 0)
                       }
                      );
}


var HomeApp = connect(mapStateToProps, mapDispatchToProps, mergeProps)(HomeRoot);
module.exports = HomeApp; 
