// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');

var injectIntl = require('react-intl').injectIntl;

//Components
var LoginForm = require('./LoginForm');
var LocaleSwitcher = require('./LocaleSwitcher');
var UserStore = require('../stores/UserStore');
var SectionCollection = require('./SectionCollection');

var ContentRoot = React.createClass({

	render: function() {
		var content = null;

		if(UserStore.isAuthenticated()) {
			content = <SectionCollection isAuthenticated = { this.props.isAuthenticated } />;
		}
		return (
			<div>
				<nav className="navbar navbar-default navbar-fixed-top">
					<div style={{ marginLeft: 'auto', marginRight: 'auto', width: 1138 }}>
						<div className="navbar-header" >
							<a className="navbar-brand" href="#" style={{ padding: 0, margin: 0}}>
								<img alt="DAIAD" src="../assets/images/daiad-transparent.png" />
							</a>
						</div>
						<div style={{ float: 'right', marginTop: 8, marginLeft: 10}}>
							<LocaleSwitcher />
						</div>
						<div style={{ float: 'right'}}>
							<div className="navbar-header" style={{marginTop: 0}}>
								<LoginForm 	action="login"
											isAuthenticated = { this.props.isAuthenticated } />
							</div>
							<div id="navbar" className="navbar-collapse collapse">
							</div>
						</div>
					</div>
				</nav>
				<div style={{ paddingTop: 70 }}>
					{content}
				</div>
			</div>
		);
  }
});

module.exports = injectIntl(ContentRoot);
