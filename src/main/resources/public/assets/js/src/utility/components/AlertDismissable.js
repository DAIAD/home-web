var React = require('react');
var ReactDOM = require('react-dom');
var FormattedMessage = require('react-intl').FormattedMessage;
var Alert = require('react-bootstrap').Alert;

var MessageAlert = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },

  propTypes : {
    messages : React.PropTypes.array
  },

  getDefaultProps: function() {
    return {
      show: true,
      messages: [],
      i18nNamespace: '',
      bsStyle: 'danger',
      title: 'Errors detected: ',
      format: 'paragraph',
      dismissFunc : null
     };
  },

  render: function() {
    var self = this;
    
    
    if(!this.props.show || !this.props.messages || this.props.messages.length ===0) {
      return null;
    }
    
    var messages;
    var title = (<h4>{this.props.title}</h4>);
    
    if (this.props.format === 'list') {
      messages = this.props.messages.map(function(m, index) {
        return(<li key={m.code}><FormattedMessage id={self.props.i18nNamespace + m.code}/></li>);
      });
      
      messages = (<div>{title}<ul>{messages}</ul></div>);
    } else {
      messages = this.props.messages.map(function(m, index) {
        return(<p key={m.code}><FormattedMessage id={self.props.i18nNamespace + m.code}/></p>);
      });
      
    }
    
    
    if (this.props.dismissFunc){
      return (
        <Alert bsStyle={this.props.bsStyle} onDismiss={this.props.dismissFunc}>
          {messages}
        </Alert>
      );
    } else {
      return (
        <Alert bsStyle={this.props.bsStyle}>
          {messages}
        </Alert>
      );
    }
  }
});

module.exports = MessageAlert;
