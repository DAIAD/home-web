var React = require('react');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;
var Button = require('react-bootstrap').Button;
var Panel = require('react-bootstrap').Panel;

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

		return (
			<div>
				<h4 style={{textAlign: 'center'}}><FormattedMessage id='LoginForm.title'/></h4>
				<Panel>
					<form key='login' action={this.props.action}>
						<div className='form-group'>
							<input id='username' name='username' type='text' ref='username' autofocus 
								placeholder={_t({ id: 'LoginForm.placehoder.username'})} className='form-control' />
						</div>
						<div className='form-group' >
							<input id='password' name='password' type='password' ref='password'
								placeholder={_t({ id: 'LoginForm.placehoder.password'})} className='form-control' />
							<a style={{ float: 'right', color: '#337ab7', fontSize: 12, marginTop: 10 }} href='#'>
								<FormattedMessage id='LoginForm.password.reset'/>
							</a>
						</div>
						<br />
						<button type='submit'
								className={'btn btn-success action-login' + (this.props.isLoading ? ' disabled' : '')}
								onClick={this.onLogin}>
							<span>
							{ this.props.isLoading ? (<i className='fa fa-refresh fa-spin' style={{ color : '#fff'}}></i>) : '' }
							{ this.props.isLoading ? (<span>&nbsp;</span>) : ''}
							<FormattedMessage id='LoginForm.button.signin' />
							</span>
						</button>
					</form>
				</Panel>
				<div style={{marginTop: 15}}>
					<AlertDismissable errors={this.props.errors} />
				</div>
			</div>
		);
	},

});

module.exports = LoginForm;
