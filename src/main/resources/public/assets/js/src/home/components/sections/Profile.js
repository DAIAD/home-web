var React = require('react');
var assign = require('object-assign');
var bs = require('react-bootstrap');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { injectIntl, FormattedMessage } = require('react-intl');

var MainSection = require('../layout/MainSection');
var LocaleSwitcher = require('../LocaleSwitcher');

var { setLocale } = require('../../actions/LocaleActions');
var { saveToProfile, fetchProfile } = require('../../actions/UserActions');
var { setForm } = require('../../actions/FormActions');

const { COUNTRIES, TIMEZONES } = require('../../constants/HomeConstants');

function ProfileForm (props) {
  const { intl, profile, setLocale, locale, setForm, saveToProfile, fetchProfile, status, setStatus } = props;
  const setProfileForm = (data) => setForm('profileForm', data);
  const _t = intl.formatMessage;
  return (
    <form id="form-profile" style={{width: 400, padding: '30px 50px'}} >
      <div className="form-group">
        <label className="control-label">
          <span><FormattedMessage id="profile.locale"/></span>
        </label>
        <LocaleSwitcher
          intl={intl}
          setLocale={(val) => { 
            setLocale(val); 
            setProfileForm({locale: val});
          }}
          locale={locale}
        />
      </div>    
        <bs.Input type="text" label={_t({id:"profile.username"})} defaultValue={profile.username} readOnly={true} />
        {
          //        <bs.Input type="email" label={_t({id:"profile.email"})} value={profile.email} hasFeedback={true} help="Please enter your email address" onChange={(e) => setProfileForm({email: e.target.value})}/>
        }
        <bs.Input type="text" label={_t({id:"profile.firstname"})} value={profile.firstname} onChange={(e) => setProfileForm({firstname: e.target.value})}/>
        <bs.Input type="text" label={_t({id:"profile.lastname"})} value={profile.lastname}  onChange={(e) => setProfileForm({lastname: e.target.value})}/>
        <bs.Input type="text" label={_t({id:"profile.address"})} value={profile.address} onChange={(e) => setProfileForm({address: e.target.value})} />
        <bs.Input type="text" label={_t({id:"profile.zip"})} value={profile.zip}  onChange={(e) => setProfileForm({zip: e.target.value})}/>

        <div className="form-group">
          <label className="control-label col-md-3" style={{ paddingLeft: 0 }}>
            <span><FormattedMessage id="profile.country"/></span>
          </label>
          <bs.DropdownButton
            title={profile.country ? _t({id: `countries.${profile.country}`}) : 'Select country'}
            id="country-switcher"
            onSelect={(e, val) => { 
              setProfileForm({country: val});
              //this.setState({country: val}); 
            } }>
            {
              COUNTRIES.map(country => 
                  <bs.MenuItem key={country} eventKey={country} value={country} >{_t({id: `countries.${country}`})}</bs.MenuItem>)
            }	
          </bs.DropdownButton>
        </div>
  
        <div className="form-group">
          <label className="control-label col-md-3" style={{ paddingLeft: 0 }}>
            <span><FormattedMessage id="profile.timezone"/></span>
          </label>
          <bs.DropdownButton
            title={profile.timezone ? _t({id: `timezones.${profile.timezone}`}) : 'Select timezone'}
            id="timezone-switcher"
            onSelect={(e, val) => { 
              setProfileForm({timezone: val});
              //this.setState({timezone: val}); 
            } }>
            {
              TIMEZONES.map(timezone => 
                  <bs.MenuItem key={timezone} eventKey={timezone} value={timezone} >{_t({id: `timezones.${timezone}`})}</bs.MenuItem>)
            }	
          </bs.DropdownButton>
        </div>
  
        <hr/>

        <div className='pull-left'>
        <bs.ButtonInput type="submit" value={_t({id:"forms.submit"})} onClick={(e) => {
          e.preventDefault(); 
          saveToProfile(JSON.parse(JSON.stringify(profile)))
          .then((res => {
            setStatus(res.success != null ? (res.success ? true : false) : null);
            setTimeout(() => setStatus(null), 2000);
            fetchProfile();
          }));
        }}  />
    </div>
      
        {
          (() => {
            if (status === true) return <i className="fa fa-check" style={{float: 'right', marginTop: 10, fontSize:'1.5em', color: '#7AD3AB'}}></i>;
            else if (status === false) return <i className="fa fa-times" style={{float: 'right', marginTop: 10, fontSize: '1.5em', color:'#CD4D3E'}}></i>;
          })()
        }

      </form>

  );
}

var Profile = React.createClass({
  getInitialState: function() {
    return {
      status: null 
    };
  },
  render: function() {
    return (
      <MainSection id="section.profile"> 
        <ProfileForm status={this.state.status} setStatus={(v) => this.setState({status: v})} {...this.props} /> 
      </MainSection>
    );
  }
});

function mapStateToProps(state) {
  return {
    profile: state.forms.profileForm,
    locale: state.locale.locale
  };
}

function mapDispatchToProps(dispatch) {
  return bindActionCreators({ setLocale, setForm, saveToProfile, fetchProfile }, dispatch);
}

Profile = connect(mapStateToProps, mapDispatchToProps)(Profile);
Profile = injectIntl(Profile);
module.exports = Profile;
