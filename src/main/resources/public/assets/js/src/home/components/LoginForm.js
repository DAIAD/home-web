// Dependencies
var React = require('react');
var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;
var Modal = require('react-bootstrap').Modal;
var Button = require('react-bootstrap').Button;
var connect = require('react-redux').connect;




var LoginForm = React.createClass({
	onLogin: function(e) {
		e.preventDefault();
		this.props.onLogin(this.refs.username.value, this.refs.password.value);

	},
	onLogout: function(e) {
		e.preventDefault();
		this.props.onLogout();

	},

	render: function() {
		var _t = this.props.intl.formatMessage;
		var component;
		if(this.props.isAuthenticated) {
			component = (
				<div key="logout"	className="">
			   		<button id="logout"
			   				type="submit"
			   				className="btn btn-primary"
	   						style={{ width: 80, height: 33 }}
		   					onClick={this.onLogout}>
			   			<FormattedMessage id="LoginForm.button.signout" />
   					</button>
		   		</div>
	   		);
		} else {
			component = (
				<form key="login" className="form-login" action={this.props.action}>
					<div className="form-group">
						<input id="username" name="username" type="text" ref="username"
							placeholder={_t({ id: 'LoginForm.placehoder.username'})} className="form-control" />
					</div>
					<div className="form-group" >
						<input id="password" name="password" type="password" ref="password"
							placeholder={_t({ id: 'LoginForm.placehoder.password'})} className="form-control" />
					</div>
					<button type="submit"
							className="btn btn-primary action-login"
							onClick={this.onLogin}
							style={{width: 80, height: 33}}>
						<FormattedMessage id="LoginForm.button.signin" />
					</button>
					<br/>
					<div className="login-errors">
						{
							this.props.errors?(<FormattedMessage id={"Errors."+this.props.errors} />):(<div/>)
					}
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

LoginForm = injectIntl(LoginForm);
module.exports = LoginForm;
