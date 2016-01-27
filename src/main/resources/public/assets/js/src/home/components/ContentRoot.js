var React = require('react');


var Constant = require('../constants/HomeConstants');
var injectIntl = require('react-intl').injectIntl;

var Header = require('./Header.react');
var MainSection = require('./MainSection.react');
var Footer = require('./Footer.react');
var History = require('./sections/History');
var LocaleSwitcher = require('./LocaleSwitcher');
var Login = require('./sections/Login');

var UserStore = require('../stores/UserStore');

var ContentRoot = React.createClass({
	getInitialState: function() {
		return {
			locale: 'el',
		};
	},
	render: function() {
		isAuthenticated = UserStore.isAuthenticated();
		profile = UserStore.getProfile();
		return (
			<div>
				<Header data={Constant.data} profile={profile} isAuthenticated={isAuthenticated}/>
				<section className="main-section" >
					<div className="container">
						{(() => {
							if (isAuthenticated) {
								return (
									this.props.children
								);
							}
							else {
								return (
									<Login />	
								);
							}
						})()
						}
					</div>
				</section>
				<Footer isAuthenticated={isAuthenticated}/>
			</div>
		);
	}
});

module.exports = injectIntl(ContentRoot);
