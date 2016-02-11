var React = require('react');
var assign = require('object-assign');
var bs = require('react-bootstrap');
var injectIntl = require('react-intl').injectIntl;
var connect = require('react-redux').connect;


var ProfileForm = React.createClass({

	handleClick: function(e) {
		e.preventDefault();
		//HomeActions.updateProfile(this.state.profile);
	},
	render: function() {
		var profile = this.props.profile;
		
		return (
			<form id="form-profile" className="col-xs-5" >
					<bs.Input type="text" label="Username" defaultValue={profile.username} readOnly={true} />
					<bs.Input type="email" label="Email" defaultValue={profile.email} ref="email" hasFeedback={true} help="Please enter your email address" />
					<bs.Input type="text" label="First name" defaultValue={profile.firstname} ref="firstname" disabled={false} />
					<bs.Input type="text" label="Last name" defaultValue={profile.lastname} ref="lastname" disabled={false} />
					<bs.ButtonInput type="submit" value="Update" onClick={this.handleClick} />
				</form>

		);
	}
});
var Profile = React.createClass({
	render: function() {
		return (
			<div className="section-profile">
				<h3>Profile</h3>
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
