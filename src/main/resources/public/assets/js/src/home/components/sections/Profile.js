var React = require('react');
var assign = require('object-assign');
var bs = require('react-bootstrap');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { injectIntl, FormattedMessage } = require('react-intl');

var MainSection = require('../layout/MainSection');
var LocaleSwitcher = require('../LocaleSwitcher');

var { setLocale } = require('../../actions/LocaleActions');


function ProfileForm (props) {
  const { intl, profile, setLocale, locale } = props;
  const _t = intl.formatMessage;
  return (
    <form id="form-profile" style={{width: '60%', margin: '40px auto'}} >
      <div className="form-group">
        <label className="control-label">
          <span>Select language</span>
        </label>
        <LocaleSwitcher
          intl={intl}
          setLocale={setLocale}
          locale={locale}
        />
      </div>    
        <bs.Input type="text" label={_t({id:"profile.username"})} defaultValue={profile.username} readOnly={true} />
        <bs.Input type="email" label={_t({id:"profile.email"})} defaultValue={profile.email} hasFeedback={true} help="Please enter your email address" />
        <bs.Input type="text" label={_t({id:"profile.firstname"})} defaultValue={profile.firstname} disabled={false} />
        <bs.Input type="text" label={_t({id:"profile.lastname"})} defaultValue={profile.lastname} disabled={false} />
        <bs.ButtonInput type="submit" value={_t({id:"forms.submit"})} onClick={(e) => {e.preventDefault();} } />
      </form>

  );
}

var Profile = React.createClass({
  render: function() {
    return (
      <MainSection id="section.profile"> 
        <ProfileForm {...this.props} /> 
      </MainSection>
    );
  }
});

function mapStateToProps(state) {
  return {
    profile: state.user.profile,
    locale: state.locale.locale
  };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators({ setLocale }, dispatch);
}

Profile = connect(mapStateToProps, mapDispatchToProps)(Profile);
Profile = injectIntl(Profile);
module.exports = Profile;
