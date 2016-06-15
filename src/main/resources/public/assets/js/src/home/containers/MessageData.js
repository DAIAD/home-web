var React = require('react');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');


var Notifications = require('../components/sections/Notifications');

var MessageActions = require('../actions/MessageActions');

var { transformInfoboxData } = require('../utils/transformations');
var { stripTags } = require('../utils/messages');

function mapStateToProps(state, ownProps) {
  return {
    devices: state.user.profile.devices,
    activeTab: state.messages.activeTab,
    activeMessageId: state.messages.activeMessageId,
    alerts: state.messages.alerts,
    announcements: state.messages.announcements,
    recommendations: state.messages.recommendations,
    tips: state.messages.tips,
  };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators(MessageActions, dispatch);
}

function mergeProps(stateProps, dispatchProps, ownProps) {

  //joint alerts & announcements
  const alertments = stateProps.alerts.concat(stateProps.announcements);

  let messages = [];
  if (stateProps.activeTab === 'alerts') messages = alertments;
  //else if (stateProps.activeTab === 'announcements') messages = stateProps.announcements;
  else if (stateProps.activeTab === 'recommendations') messages = stateProps.recommendations;
  else if (stateProps.activeTab === 'tips') messages = stateProps.tips;

  messages = messages.map(message => stripTags(message));

  const categories = [
    {id: 'alerts', title: 'notifications.alerts', unread: alertments.reduce(((prev, curr) => ! curr.acknowledgedOn ? prev+1 : prev), 0)}, 
      //{id: 'announcements', title: 'notifications.announcements', unread: stateProps.announcements.reduce(((prev, curr) =>  !curr.acknowledgedOn ? prev+1 : prev), 0)}, 
    {id: 'recommendations', title: 'notifications.recommendations', unread: stateProps.recommendations.reduce(((prev, curr) =>  !curr.acknowledgedOn ? prev+1 : prev), 0)}, 
    {id: 'tips', title: 'notifications.tips', unread: stateProps.tips.reduce(((prev, curr) =>  !curr.acknowledgedOn ? prev+1 : prev), 0)}, 
  ];

  const unread = categories.reduce(((prev, curr) => curr.unread+prev), 0); 

  const activeMessageIndex = stateProps.activeMessageId ? messages.findIndex(x => x.id === stateProps.activeMessageId) : null;
  const activeMessage = activeMessageIndex != null ? messages[activeMessageIndex] : null;

  const infobox = activeMessage && activeMessage.extra ? transformInfoboxData(activeMessage.extra, stateProps.devices, ownProps.intl) : {}; 
  return Object.assign({}, ownProps,
               dispatchProps,
               stateProps,
               {
                 nextMessageId: activeMessageIndex != null ? ( messages[activeMessageIndex+1] ? messages[activeMessageIndex+1].id : null) : null,
                 previousMessageId: activeMessageIndex != null ? ( messages[activeMessageIndex-1] ? messages[activeMessageIndex-1].id : null) : null,
                 categories,
                 infobox,
                 messages,
                 activeMessage 
               });
}

var MessageData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Notifications);
MessageData = injectIntl(MessageData);
module.exports = MessageData;
