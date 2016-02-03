var React = require('react');


var Constant = require('../constants/HomeConstants');
var injectIntl = require('react-intl').injectIntl;

var Header = require('./Header.react');
var MainSection = require('./MainSection.react');
var Footer = require('./Footer.react');
var History = require('./sections/History');
var LocaleSwitcher = require('./LocaleSwitcher');
var LoginPage = require('./sections/LoginPage');

var Login = require('./sections/Login');

var UserStore = require('../stores/UserStore');


var ContentRoot = React.createClass({
	getInitialState: function() {
		return {
			locale: 'el',
		};
	},

	childContextTypes: {
		isAuthenticated: React.PropTypes.bool
	},
	getChildContext: function() {
		return {
			isAuthenticated: UserStore.isAuthenticated() 
		};
	},
	render: function() {
		isAuthenticated = UserStore.isAuthenticated();
		profile = UserStore.getProfile();
		return (
			<div>
				<Header data={Constant.data} />

					{(() => {
							if (isAuthenticated) {
								return (
									//React.cloneElement(this.props.children, {profile:profile})
									<MainSection>
										{this.props.children}
									</MainSection>
								);
							}
							else {
								return (
									<LoginPage />
								);
							}
						})()}
				<Footer />
			</div>
		);
	}
});

module.exports = injectIntl(ContentRoot);
