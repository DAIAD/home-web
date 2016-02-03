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
			firstname: this.props.profile.firstname,
			lastname: this.props.profile.lastname,
			email: this.props.profile.email,
			};
		},
	handleClick: function(e) {
		e.preventDefault();

		var profile = assign({}, this.props.profile);
		profile.firstname = this.state.firstname;
		profile.lastname = this.state.lastname;
		profile.email = this.state.email;

		if (this.isFormValid()){
			HomeActions.updateProfile(profile);
		}
	},
	_onChange: function() {
		this.setState({
			firstname:this.refs.firstname.getValue(),
			lastname:this.refs.lastname.getValue(),
			email:this.refs.email.getValue()
			});
	},
	isFormValid: function() {
		if (this.isEmailValid()){
			return true;
		}
		return false;
	},
	isEmailValid: function() {
		return validateEmail(this.state.email);
	},
	render: function() {
		return (
			<form id="form-profile" className="col-xs-5" >
					<bs.Input type="text" label="Username" value={this.props.profile.username} readOnly={true} />
					<bs.Input type="email" label="Email" value={this.state.email} onChange={this._onChange} ref="email" bsStyle={this.isEmailValid()?'success':'error'} hasFeedback={true} help="Please enter your email address" />
					<bs.Input type="text" label="First name" defaultValue={this.state.firstname} ref="firstname" onChange={this._onChange} disabled={false} />
					<bs.Input type="text" label="Last name" value={this.state.lastname} ref="lastname" onChange={this._onChange} disabled={false} />
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
				<ProfileForm profile={UserStore.getProfile()}/>	
			</div>
		);
	}
});

module.exports = Profile;
