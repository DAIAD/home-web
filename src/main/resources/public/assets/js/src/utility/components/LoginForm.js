var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;
var Button = require('react-bootstrap').Button;

var AlertDismissable = require('./AlertDismissable');

var LoginForm = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

	onLogin: function(e) {
		e.preventDefault();
		this.props.onLogin(this.refs.username.value, this.refs.password.value);
	},

	render: function() {
		var _t = this.context.intl.formatMessage;
		
		var component = null;
		
		if(!this.props.isAuthenticated) {
			component = (
				<form key="login" action={this.props.action}>
					<div className="form-group">
						<input id="username" name="username" type="text" ref="username" autofocus 
							placeholder={_t({ id: 'LoginForm.placehoder.username'})} className="form-control" />
					</div>
					<div className="form-group" >
						<input id="password" name="password" type="password" ref="password"
							placeholder={_t({ id: 'LoginForm.placehoder.password'})} className="form-control" />
					</div>
					<button type="submit"
							className="btn btn-success action-login"
							onClick={this.onLogin}>
						<FormattedMessage id="LoginForm.button.signin" />
					</button>
					<div style={{marginTop: 15}}>
						<AlertDismissable errors={this.props.errors} />
					</div>
				</form>
			);
		}

		return (
			<div className="login-out">
				{component}
   			</div>
		);
	},

});

module.exports = LoginForm;
