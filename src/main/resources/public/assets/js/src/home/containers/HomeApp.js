// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var ReactIntlProvider = require('react-intl').IntlProvider;
var injectIntl = require('react-intl').injectIntl;
var connect = require('react-redux').connect;

//Constants
var Constant = require('../constants/HomeConstants');

// Components
var Header = require('../components/Header.react');
var MainSection = require('../components/MainSection.react');
var Footer = require('../components/Footer.react');
var LoginPage = require('../components/sections/Login');

// Actions
var UserActions = require('../actions/UserActions');
var LocaleActions = require('../actions/LocaleActions');
var DeviceActions = require('../actions/DeviceActions');

var getDefaultDevice = require('../utils/device').getDefaultDevice;
var getDeviceCount = require('../utils/device').getDeviceCount;

var HomeApp = React.createClass({

	componentWillMount: function() {
		if (this.props.user.isAuthenticated) {
			//this.props.setDefaultActiveDevice(this.props.user.profile.devices);
		}
	},
	render: function() {
		const devices = this.props.user.profile.devices;
		return (
			<ReactIntlProvider 
				locale={this.props.locale.locale}
				messages={this.props.locale.messages} >
				
				<div>
					{
						(() => {
							if (this.props.loading){
								return (
									<span style={{position:'absolute'}} >Loading....</span>
									);
							}
							})()
					}	
					<Header 
						data={Constant.data}
						firstname={this.props.user.profile.firstname}
						deviceCount={this.props.user.isAuthenticated?getDeviceCount(devices):0}
						isAuthenticated={this.props.user.isAuthenticated}
						locale={this.props.locale.locale}
						onLogout={this.props.onLogout} 
						onLocaleSwitch={this.props.onLocaleSwitch}
					/>
					
					{
						(() => {
							if (this.props.user.isAuthenticated) {
									// wait until profile is loaded 
									if (devices === undefined){
										return (null);
									}
									else {
										return (
												this.props.children
											);
									}
								}
								else {
									return (
										<LoginPage 
											isAuthenticated = {this.props.user.isAuthenticated}
											errors = {this.props.user.status.errors}
											onLogin = {this.props.onLogin}
											onLogout = {this.props.onLogout} />
										);
								}
							})()
						}

						<Footer />

					</div>

				</ReactIntlProvider>
			);
	},

});


function mapStateToProps(state) {
		return {
			user: state.user,
			locale: state.locale,
			loading: state.user.status.isLoading || state.locale.status.isLoading,
		};
}

function mapDispatchToProps(dispatch, ownProps) {
	return {
		onLogin: function(username, password) {
			dispatch(UserActions.login(username, password)).then(function(response) {
				const devices = response.profile.devices;
				const device = getDefaultDevice(devices);
				return dispatch(DeviceActions.setActiveDevice(device.deviceKey));
			},
			function(error) {
				console.log('oops, something went wrong while logging-in');
			});
		},
		onLogout: function() {
			dispatch(UserActions.logout());
		},
		onLocaleSwitch: function(locale) {
			dispatch(LocaleActions.setLocale(locale));
		},
		
	};
}

HomeApp = connect(mapStateToProps, mapDispatchToProps)(HomeApp);
HomeApp = injectIntl(HomeApp);
module.exports = HomeApp; 
