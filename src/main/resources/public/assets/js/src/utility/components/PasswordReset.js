var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var Bootstrap = require('react-bootstrap');
var Button = require('react-bootstrap').Button;
var Panel = require('react-bootstrap').Panel;
var ReCAPTCHA = require("react-google-recaptcha");
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');

var AlertDismissable = require('./AlertDismissable');

var {reset, setCaptchaValid, clearErrors, setErrors } = require('../actions/PasswordActions');

var PasswordReset = React.createClass({

  contextTypes: {
    intl: React.PropTypes.object
  },

  onCaptchaChange: function (value) {
    this.props.actions.setCaptchaValid(value != null);
  },

  hideErrors: function(){
    this.props.actions.clearErrors();
  },

  onResetPassword: function(e) {
    e.preventDefault();
    
    this.props.actions.clearErrors();
    
    var errors = [];

    if(!this.refs.password1.value) {
      errors.push({ code : 'PasswordErrorCode.INVALID_LENGTH' });
    } else if(this.refs.password1.value != this.refs.password2.value) {
      errors.push({ code : 'PasswordErrorCode.VERIFICATION_FAILED' });
    }
    if(!this.props.isCaptchaValid) {
      errors.push({ code : 'PasswordErrorCode.CAPTCHA_ERROR' });
    }

    if(errors.length > 0) {
      this.props.actions.setErrors(errors);
    } else {
      this.refs.recaptcha.reset();
      this.props.actions.setCaptchaValid(false);

      this.props.actions.reset(this.props.routeParams.token, this.refs.password1.value);
    }
  },

  renderForm: function() {
    var _t = this.context.intl.formatMessage;

    return (
      <div style={{ width: '100%' }}>
        <nav className='navbar navbar-default navbar-fixed-top'>
          <div className='navbar-header' style={{ paddingLeft: 15 }} >
            <a className='navbar-brand' href='#' style={{ padding: 0, margin: 0}}>
              <img alt='DAIAD' src='/assets/images/shared/daiad-logo.svg' style={{ marginTop: 15 }} />
            </a>
          </div>
        </nav>
        <div style={{marginLeft: 'auto', marginRight: 'auto', width: '328px', marginTop: '80px' }}>
          <h4 style={{textAlign: 'center'}}><FormattedMessage id='PasswordResetForm.title'/></h4>
          <Panel>
            <form key='login' action={this.props.action}>
              <div className='form-group' >
                <input  id='password1' 
                        name='password1'
                        type='password'
                        ref='password1'
                        placeholder={_t({ id: 'PasswordResetForm.placehoder.password1'})}
                        className='form-control' />
              </div>
              <div className='form-group' >
                <input  id='password2' 
                        name='password2'
                        type='password'
                        ref='password2'
                        placeholder={_t({ id: 'PasswordResetForm.placehoder.password2'})}
                        className='form-control' />
              </div>
              <div className='form-group' >
                <ReCAPTCHA ref="recaptcha"
                           sitekey={properties.captchaKey}
                           onChange={this.onCaptchaChange}
                />
              </div>
              <button type='submit'
                  className={'btn btn-success action-login' + (this.props.isLoading ? ' disabled' : '')}
                  onClick={this.onResetPassword}>
                <span>
                { this.props.isLoading ? (<i className='fa fa-refresh fa-spin' style={{ color : '#fff'}}></i>) : '' }
                { this.props.isLoading ? (<span>&nbsp;</span>) : ''}
                <FormattedMessage id='PasswordResetForm.button.reset' />
                </span>
              </button>
            </form>
          </Panel>
          <div style={{marginTop: 15}}>
            <AlertDismissable i18nNamespace='Error.'
                              show={this.props.errors != null}
                              messages={this.props.errors}
                              dismissFunc={this.hideErrors} />
          </div>
        </div>
      </div>
    );
  },
  
  renderSuccess: function() {
    var _t = this.context.intl.formatMessage;

    return (
      <div style={{ width: '100%' }}>
        <nav className='navbar navbar-default navbar-fixed-top'>
          <div className='navbar-header' style={{ paddingLeft: 15 }} >
            <a className='navbar-brand' href='#' style={{ padding: 0, margin: 0}}>
              <img alt='DAIAD' src='/assets/images/shared/daiad-logo.svg' style={{ marginTop: 15 }} />
            </a>
          </div>
        </nav>
        <div style={{marginLeft: 'auto', marginRight: 'auto', width: '400px', marginTop: '80px' }}>
          <a className='btn btn-success' style={{textAlign: 'center', fontSize: 'large', whiteSpace: 'normal'}} href='/home/'><FormattedMessage id='PasswordResetForm.message.success'/></a>
        </div>
      </div>
    );    
  },
  
  render: function() {
    console.log(this.props.success);
    if(this.props.success) {
      return this.renderSuccess();
    } else {
      return this.renderForm();
    }
  }

});

PasswordReset.icon = 'key';
PasswordReset.title = 'Page.PasswordReset';

function mapStateToProps(state) {
  return {
    isAuthenticated: state.session.isAuthenticated,
    isLoading:  state.password.isLoading,
    errors: state.password.errors,
    success: state.password.success,
    isCaptchaValid: state.password.reset.isCaptchaValid,
    routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(
      Object.assign({}, {reset , setCaptchaValid, clearErrors, setErrors }),  
      dispatch
    )
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(PasswordReset);
