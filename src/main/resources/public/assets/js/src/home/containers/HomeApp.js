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
var LoginForm = require('../components/LoginForm');

// Actions
var UserActions = require('../actions/UserActions');
var LocaleActions = require('../actions/LocaleActions');

var HomeApp = React.createClass({

	render: function() {
		if (this.props.locale.locale === undefined){
			return (null);
		}
		else{
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
							isAuthenticated={this.props.user.isAuthenticated}
							locale={this.props.locale.locale}
							onLogout={this.props.onLogout} 
							onLocaleSwitch={this.props.onLocaleSwitch}
						/>
						
						{
							(() => {
									if (this.props.user.isAuthenticated) {
										return (
											<MainSection>
												{this.props.children}
											</MainSection>
										);
									}
									else {
										return (
											<MainSection 
												style = {{paddingTop: '50px'}}
												>
												<LoginForm 	
													isAuthenticated = {this.props.user.isAuthenticated}
													errors = {this.props.user.errors}
													onLogin = {this.props.onLogin}
													onLogout = {this.props.onLogout}
												/>
											</MainSection>
									);
									}
								})()
						}

						<Footer />

					</div>

				</ReactIntlProvider>
			);
		}
	},

});

function mapStateToProps(state) {
		return {
			user: state.user,
			locale: state.locale,
			loading: state.user.isLoading || state.locale.isLoading

		};
}

function mapDispatchToProps(dispatch) {
	return {
		onLogin: function(username, password) {
			dispatch(UserActions.login(username, password));
		},
		onLogout: function() {
			dispatch(UserActions.logout());
		},
		onLocaleSwitch: function(locale) {
			dispatch(LocaleActions.setLocale(locale));
		}
	};
}

HomeApp = connect(mapStateToProps, mapDispatchToProps)(HomeApp);
HomeApp = injectIntl(HomeApp);
module.exports = HomeApp; 
