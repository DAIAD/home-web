var React = require('react');
var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;

var Modal = require('react-bootstrap').Modal;
var Button = require('react-bootstrap').Button;

var UserStore = require('../stores/UserStore');

var UtilityActions = require('../actions/UtilityActions');

function login(event) {
	event.preventDefault();

	UtilityActions.login(this.refs.username.value, this.refs.password.value);
}

function logout(event) {
	event.preventDefault();

	UtilityActions.logout();
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
				<p key="logout"	className="navbar-text navbar-right"
					style={{marginTop: 8, marginBottom: 0, paddingRight: 15 }} >
			   		<button id="logout"
			   				type="submit"
			   				className="btn btn-primary"
	   						style={{ width: 80, height: 33 }}
		   					onClick={logout.bind(this)}>
			   			<FormattedMessage id="LoginForm.button.signout" />
   					</button>
		   		</p>
	   		);
		} else {
			children.push(
				<form key="login" className="navbar-form navbar-right form-login" action={this.props.action}>
					<div className="form-group" style={{ marginRight: 10}}>
						<input id="username" name="username" type="text" ref="username"
							placeholder={_t({ id: 'LoginForm.placehoder.username'})} className="form-control" />
					</div>
					<div className="form-group" style={{ marginRight: 10}}>
						<input id="password" name="password" type="password" ref="password"
							placeholder={_t({ id: 'LoginForm.placehoder.password'})} className="form-control" />
					</div>
					<button type="submit"
							className="btn btn-primary action-login"
							onClick={login.bind(this)}
							style={{width: 80, height: 33, marginTop: -1}}>
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
							<img alt="DAIAD" src="../assets/images/daiad-transparent.png" />
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
			<div>
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
