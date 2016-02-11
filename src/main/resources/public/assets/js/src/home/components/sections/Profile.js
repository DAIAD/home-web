var React = require('react');
var assign = require('object-assign');
var bs = require('react-bootstrap');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;

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
			<div className="section-profile">
				<h3><FormattedMessage id="section.profile" /></h3>
				<ProfileForm {...this.props} />	
			</div>
		);
	}
});

function mapStateToProps(state) {
	//don't know if this will rerender on update
	var profile = Object.assign({}, state.user.profile);
	delete profile.devices;
	return {
		profile: profile
	};
}

Profile = connect(mapStateToProps)(Profile);
Profile = injectIntl(Profile);
module.exports = Profile;
