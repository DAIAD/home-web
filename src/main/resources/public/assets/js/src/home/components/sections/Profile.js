var React = require('react');
var assign = require('object-assign');
var bs = require('react-bootstrap');

var UserStore = require('../../stores/UserStore');

// Actions
var HomeActions = require('../../actions/HomeActions');

//http://stackoverflow.com/questions/46155/validate-email-address-in-javascript
function validateEmail(email) {
		 var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		return re.test(email);
}
var ProfileForm = React.createClass({
	getInitialState: function() {
		return {
			//ATTENTION CHANGING ON EVERY CHANGE
			//profile: assign({}, UserStore.getProfile()) 
			profile: UserStore.getProfile() 

		};
	},
	handleClick: function(e) {
		e.preventDefault();
		if (this.isFormValid()){
			HomeActions.updateProfile(this.state.profile);
		}
	},
	_onChange: function() {
		var profile = this.state.profile;
		profile.firstname = this.refs.firstname.getValue();
		profile.lastname = this.refs.lastname.getValue();
		profile.email = this.refs.email.getValue();
		this.setState({
			profile: profile
		});
	},
	isFormValid: function() {
		if (this.isEmailValid()){
			return true;
		}
		return false;
	},
	isEmailValid: function() {
		return validateEmail(this.state.profile.email);
	},
	render: function() {
		var profile = this.state.profile;
		return (
			<form id="form-profile" className="col-xs-5" >
					<bs.Input type="text" label="Username" value={profile.username} readOnly={true} />
					<bs.Input type="email" label="Email" value={profile.email} onChange={this._onChange} ref="email" bsStyle={this.isEmailValid()?'success':'error'} hasFeedback={true} help="Please enter your email address" />
					<bs.Input type="text" label="First name" defaultValue={profile.firstname} ref="firstname" onChange={this._onChange} disabled={false} />
					<bs.Input type="text" label="Last name" value={profile.lastname} ref="lastname" onChange={this._onChange} disabled={false} />
					<bs.ButtonInput type="submit" value="Submit" onClick={this.handleClick} />
				</form>

		);
	}
});
var Profile = React.createClass({
	render: function() {
		return (
			<div className="section-profile">
				<h3>Profile</h3>
				<ProfileForm />	
			</div>
		);
	}
});

module.exports = Profile;
