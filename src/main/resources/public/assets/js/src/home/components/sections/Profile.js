var React = require('react');
var assign = require('object-assign');
var bs = require('react-bootstrap');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;
var MainSection = require('../MainSection.react');

var ProfileForm = React.createClass({

	handleClick: function(e) {
		e.preventDefault();
		//HomeActions.updateProfile(this.state.profile);
	},
	render: function() {
		var profile = this.props.profile;
		var _t = this.props.intl.formatMessage;
		return (
			<form id="form-profile" className="col-xs-5" >
					<bs.Input type="text" label={_t({id:"profile.username"})} defaultValue={profile.username} readOnly={true} />
					<bs.Input type="email" label={_t({id:"profile.email"})} defaultValue={profile.email} ref="email" hasFeedback={true} help="Please enter your email address" />
					<bs.Input type="text" label={_t({id:"profile.firstname"})} defaultValue={profile.firstname} ref="firstname" disabled={false} />
					<bs.Input type="text" label={_t({id:"profile.lastname"})} defaultValue={profile.lastname} ref="lastname" disabled={false} />
					<bs.ButtonInput type="submit" value={_t({id:"forms.submit"})} onClick={this.handleClick} />
				</form>

		);
	}
});

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
		profile: state.user.profile
	};
}

Profile = connect(mapStateToProps)(Profile);
Profile = injectIntl(Profile);
module.exports = Profile;
