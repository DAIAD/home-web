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
	    };
	},

  	render: function() {
  		if((!this.props.errors) || (this.props.errors.length ===0)) {
  			return null;
  		}
  		if(!this.state.alertVisible) {
  			return null;
  		}
  		var errors = this.props.errors.map(function(e, index) {
  			return (<p key={{index}}><FormattedMessage id={e.code}/></p>);
  		});
  		
  		return (
			<Alert bsStyle='danger' onDismiss={this.handleAlertDismiss}>
				{errors}
  	        </Alert>
 		);
  	},
  	
  	handleAlertDismiss() {
  		this.setState({alertVisible: false});
  	}
});

module.exports = ErrorAlert;
