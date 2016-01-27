// Dependencies
var React = require('react');
var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;
var Modal = require('react-bootstrap').Modal;
var Button = require('react-bootstrap').Button;

// Stores
var UserStore = require('../stores/UserStore');

// Actions
var HomeActions = require('../actions/HomeActions');

function login(event) {
	event.preventDefault();

	HomeActions.login(this.refs.username.value, this.refs.password.value);
}

function logout(event) {
	event.preventDefault();

	HomeActions.logout();
}

var LoginForm = React.createClass({

	getInitialState: function() {
	    return {
	    	isModalOpen: false
	    };
	},

	componentDidMount: function() {
		UserStore.addLoginListener(this._onLogin);
	},

	componentWillUnmount: function() {
		UserStore.removeLoginListener(this._onLogin);
	},

	render: function() {
		var _t = this.props.intl.formatMessage;

		var children = [];
		if(this.props.isAuthenticated) {
			children.push(
				<div key="logout"	className="">
			   		<button id="logout"
			   				type="submit"
			   				className="btn btn-primary"
	   						style={{ width: 80, height: 33 }}
		   					onClick={logout.bind(this)}>
			   			<FormattedMessage id="LoginForm.button.signout" />
   					</button>
		   		</div>
	   		);
		} else {
			children.push(
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
							onClick={login.bind(this)}
							style={{width: 80, height: 33}}>
						<FormattedMessage id="LoginForm.button.signin" />
					</button>
				</form>
			);
		}

		if(this.state.isModalOpen) {
			children.push(
				<Modal key="modal" show={true} onHide={this.close}>
					<Modal.Header closeButton>
				    	<Modal.Title>
							<img alt="DAIAD" src="images/daiad-transparent.png" />
						</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						<FormattedMessage
							id="LoginForm.login.failure"
						/>
			  		</Modal.Body>
			  		<Modal.Footer>
			  			<Button onClick={this.close}>Close</Button>
		  			</Modal.Footer>
				</Modal>
			);
		}
		return (
			<div className="login-out">
				{children}
   			</div>
		);
	},

	close: function() {
		this.setState({ isModalOpen: false });
	},

	_onLogin: function(args) {
		this.setState({
	    	isModalOpen: !args.success
	    });
	},

	_onLogout: function() {
		this.setState({
	    	isModalOpen: false
	    });
	}
});

module.exports = injectIntl(LoginForm);
