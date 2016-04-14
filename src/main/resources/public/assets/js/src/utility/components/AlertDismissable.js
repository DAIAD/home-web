var React = require('react');
var ReactDOM = require('react-dom');
var FormattedMessage = require('react-intl').FormattedMessage;
var Alert = require('react-bootstrap').Alert;

var ErrorAlert = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

	propTypes : {
		errors : React.PropTypes.array
	},

	getInitialState() {
		return {
			alertVisible: true
	    };
	},

	getDefaultProps: function() {
		return {
			errors: [],
			i18nNamespace: '',
			success: false,
			title: '',
			format: 'paragraph'
	    };
	},

	render: function() {
	  var self = this;
	  
		if((!this.props.errors) || (this.props.errors.length ===0)) {
			return null;
		}
		
		if(!(this.state.alertVisible || this.props.show )) {
			return null;
		}
		var errors;
		var title = (<h4>{this.props.title}</h4>);
		
		if (this.props.format === 'list') {
  		errors = this.props.errors.map(function(e, index) {
  		  return(<li key={e.code}><FormattedMessage id={self.props.i18nNamespace + e.code}/></li>);
  		});
  		
  		errors = (<div>{title}<ul>{errors}</ul></div>);
		} else {
		  errors = this.props.errors.map(function(e, index) {
        return(<p key={e.code}><FormattedMessage id={self.props.i18nNamespace + e.code}/></p>);
      });
      
		}
		
		
		return (
		<Alert bsStyle={this.props.success ? 'success' : 'danger'} onDismiss={this.handleAlertDismiss}>
			{errors}
	  </Alert>
	);
	},
	
	handleAlertDismiss() {
	  if (this.props.hasOwnProperty('hideAlert')){
	    this.props.hideAlert();
	  }
		this.setState({alertVisible: false});
	}
});

module.exports = ErrorAlert;
