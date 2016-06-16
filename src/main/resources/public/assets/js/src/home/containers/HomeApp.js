// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

// Components
var HomeRoot = require('../components/layout/HomeRoot');

// Actions
var { login, logout, refreshProfile } = require('../actions/UserActions');
var { letTheRightOneIn, setReady } = require('../actions/InitActions');
var { setLocale } = require('../actions/LocaleActions');
var { linkToMessage:linkToNotification } = require('../actions/MessageActions');
var { dismissError } = require('../actions/QueryActions');

var { getDeviceCount } = require('../utils/device');
var { combineMessages } = require('../utils/messages');

function mapStateToProps(state) {
    return {
      user: state.user,
      ready: state.user.ready,
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
  return bindActionCreators(Object.assign({}, {login, logout, refreshProfile, letTheRightOneIn, setLocale, linkToNotification, dismissError, setReady }), dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  return Object.assign({}, 
                       ownProps, 
                       dispatchProps,
                       stateProps,
                       { 
                         init: () => {
                           //init locale
                           dispatchProps.setLocale(properties.locale)
                           .then(() => {
                             //refresh profile if session exists
                             if (properties.reload) {
                               dispatchProps.refreshProfile()
                                 .then(res => { 
                                   if (res.success) { 
                                     dispatchProps.eetReady(); 
                                     dispatchProps.letTheRightOneIn();
                                   }
                                 }); 
                             }
                             else {
                               dispatchProps.setReady();
                             }
                           });
                         },
                         login: (user, pass) => dispatchProps.login(user, pass)
                                                .then(res => res.success ? dispatchProps.letTheRightOneIn() : null), 
                         unreadNotifications: stateProps.messages.reduce(((prev, curr) => !curr.acknowledgedOn ? prev+1 : prev), 0)
                       }
                      );
}


var HomeApp = connect(mapStateToProps, mapDispatchToProps, mergeProps)(HomeRoot);
module.exports = HomeApp; 
