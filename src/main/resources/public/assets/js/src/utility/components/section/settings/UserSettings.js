var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var FormattedMessage = require('react-intl').FormattedMessage;
var bs = require('react-bootstrap');
var LocaleSwitcher = require('../../../components/LocaleSwitcher');
var { setLocale } = require('../../../actions/LocaleActions');
var { saveToProfile } = require('../../../actions/SessionActions');

const { COUNTRIES, TIMEZONES } = require('../../../constants/Constants');

var Breadcrumb = require('../../Breadcrumb');

var UserSettings = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
    getInitialState: function() {
      const { profile:{firstname, lastname, address, zip, country, timezone, locale} } = this.props;
      return {
        firstname, 
        lastname,
        address,
        zip,
        country,
        timezone,
        locale
      };
    },
  	render: function() {
      const { profile, actions:{saveToProfile} } = this.props;

      var _t = this.context.intl.formatMessage;

      if (this.state.locale === 'en') this.setState({locale: 'en-GB'});

      const countryKey = this.state.country ? "Countries."+this.state.country : "UserSettings.CountryPlaceholder";
      const timezoneKey = this.state.timezone ? "Timezones."+this.state.timezone : "UserSettings.TimezonePlaceholder";
  		return (
			<div className="container-fluid" style={{ paddingTop: 10 }}>
				<div className="row">
					<div className="col-md-12">
						<Breadcrumb routes={this.props.routes}/>
					</div>
        </div>

        
        <form id="form-profile">
          <div className="form-group">
            <label className="control-label">
              <span><FormattedMessage id="UserSettings.Locale"/></span>
            </label>
            <LocaleSwitcher
              locale={this.state.locale} 
              onLocaleSwitch={value => { 
                this.props.actions.setLocale(value); 
                this.setState({locale: value});
              }} 
            />
					</div>
          <div className="form-group">
            <bs.Input type="text" label={_t({id:"UserSettings.Username"})} value={profile.username} readOnly={true} />
					</div>
          <div className="form-group">
            <bs.Input type="text" label={_t({id:"UserSettings.Firstname"})} value={this.state.firstname} onChange={e => this.setState({firstname: e.target.value})} />
					</div>
          <div className="form-group">
            <bs.Input type="text" label={_t({id:"UserSettings.Lastname"})} value={this.state.lastname} onChange={e => this.setState({lastname: e.target.value})} />
					</div>
           <div className="form-group">
             <bs.Input type="text" label={_t({id:"UserSettings.Address"})} value={this.state.address} onChange={e => this.setState({address: e.target.value})}/>
          </div>
          <div className="form-group">
            <bs.Input type="text" label={_t({id:"UserSettings.Zip"})} value={this.state.zip} onChange={e => this.setState({zip: e.target.value})} />
					</div>

          <div className="form-group">
            <label className="control-label">
              <span><FormattedMessage id="UserSettings.Country"/></span>
            </label>
            <bs.DropdownButton
					    title={_t({ id: countryKey})}
              id="country-switcher"
              onSelect={(e, val) => { this.setState({country: val}); } }>
              {
                COUNTRIES.map(country => 
                    <bs.MenuItem key={country} eventKey={country} value={country} >{_t({id: "Countries."+country})}</bs.MenuItem>)
              }	
            </bs.DropdownButton>
          </div>

          <div className="form-group">
            <label className="control-label">
              <span><FormattedMessage id="UserSettings.Timezone"/></span>
            </label>
            <bs.DropdownButton
					    title={_t({ id: timezoneKey})}
              id="timezone-switcher"
              onSelect={(e, val) => { this.setState({timezone: val}); } }>
              {
                TIMEZONES.map(timezone => 
                    <bs.MenuItem key={timezone} eventKey={timezone} value={timezone} >{_t({id: "Timezones."+timezone})}</bs.MenuItem>)
              }	
            </bs.DropdownButton>
          </div>

          <hr/>
          <bs.ButtonInput type="submit" value={_t({id:"UserSettings.Submit"})} onClick={(e) => {
            e.preventDefault();  
            saveToProfile(JSON.parse(JSON.stringify(this.state))); 
          }} />
				</form>
			</div>
 		);
  	}
});

UserSettings.icon = 'user';
UserSettings.title = 'Settings.User';

function mapStateToProps(state) {
	return {
    locale: state.i18n.locale,
    profile: state.session.profile
	};
}

function mapDispatchToProps(dispatch) {
	return {
		actions : bindActionCreators(Object.assign({}, { setLocale, saveToProfile }) , dispatch)
	};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(UserSettings);
