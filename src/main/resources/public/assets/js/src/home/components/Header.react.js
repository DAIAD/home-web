var React = require('react');

var injectIntl = require('react-intl').injectIntl;
var Constants = require('../constants/HomeConstants');
var LocaleSwitcher = require('./LocaleSwitcher');
var LoginForm = require('./LoginForm');

var FormattedMessage = require('react-intl').FormattedMessage;
var bs = require('react-bootstrap');
var Link = require('react-router').Link;

var UserStore = require('../stores/UserStore');

/* DAIAD Logo */

var MainLogo = React.createClass({
	render: function() {
		return (
			<Link to="/" className="logo" activeClassName="selected">
				<img src= {Constants.STATIC + "Icons/SVG/daiad-logo2.svg"} alt="DAIAD Logo"
					title="DAIAD" />
			</Link>
		);
	}
});

/* Main Menu */

var MenuItem = React.createClass({
	handleClick: function() {
		//this.props.onMenuClick(this.props.item.name);
	},
	render: function() {
		//var selected = this.props.sectionShown === this.props.item.name ? " selected":"";
		//<a className="menu-item" href={this.props.item.link} onClick={this.handleClick} >
		return (
			<li>
				<Link to={this.props.item.route} className="menu-item" activeClassName="selected">
					<img className="menu-icon" src={Constants.STATIC + this.props.item.image} />
					<span className="menu-span"><FormattedMessage id={this.props.item.title}/></span>
				</Link>
			</li>
		);
	}
});

var MainMenu = React.createClass({
	render: function() {
		var items = [{
									name: "dashboard",
									title: "Section.Dashboard",
									image: "Icons/SVG/dashboard.svg",
									route:"/dashboard"
								},
								{
									name: "history",
									title: "Section.History",
									image: "Icons/SVG/stats.svg",
									route:"/history"
								},
								{
									name: "commons",
									title: "Section.Commons",
									image: "Icons/SVG/goals.svg",
									route:"/commons"
								}];

		return (
			<div className="main-menu">
				<div className="navigation" role="navigation">
					<ul className="list-unstyled">
						{
          	items.map(function (item) {
							return (
								<MenuItem key={item.name} item={item} />
							);
							})
        		}		
					</ul>
				</div>
			</div>
		);
	}
});

/* User options */

var UserInfo = React.createClass({
	getInitialState: function() {
		return this._getFirstName();
	},
	componentDidMount: function() {
		UserStore.addProfileUpdateListener(this._onUpdate);
	},
	componentWillUnmount: function() {
		UserStore.removeProfileUpdateListener(this._onUpdate);
	},
	render: function() {
		return (
			<div className="user-menu" >
					<div title="home.profile">
					<Link to="settings/profile">
						<span>{this.state.firstname}</span>
					</Link>
				</div>
			</div>
		);
	},
	_onUpdate: function() {
		if (this.state.firstname !== this._getFirstName().firstname){
			this.setState(this._getFirstName());
		}
	},
	_getFirstName: function() {
		return {
			firstname: UserStore.getProfile().firstname 
		};
	}
});

/* Notification Area */

var SettingsMenuItem = React.createClass({
	render: function() {
		return (
			<div title={this.props.item.title}>
				<Link to="/settings"> 
					<img src={Constants.STATIC + this.props.item.image} />
				</Link>
			</div>
		);
	}
});

var NotificationMenuItem = React.createClass({

	render: function() {
		var hasUnread = this.props.unreadNotifications>0?"hasUnread":"";
		var unreadNotifications = hasUnread?this.props.unreadNotifications:"";
		return (
			<bs.OverlayTrigger 
				id="notifications-trigger"
				trigger="click"
				rootClose
				placement="bottom"
				overlay={<bs.Popover 
					id="notifications-popover"
					title="Notifications" >
					<div className="scrollable">
						<NotificationList notifications={this.props.notifications} />
					</div>
					<div className="footer">
						<Link className="notifications-show-all" to="/notifications">Show all</Link>
					</div>
				</bs.Popover>}
				className="notifications-button" >
					<div>
						<i className={hasUnread}>{unreadNotifications}</i>	
						<img src={Constants.STATIC + this.props.item.image} />
					</div>
				</bs.OverlayTrigger>
		);
	}
});

var NotificationList = React.createClass({
	render: function() {
		return (
			<div className="notification-list">
			<ul className="list-unstyled">
				{
					this.props.notifications.map(function(notification) {
						var notificationClass = notification.unread?"unread":"read";
						return (
							<Link key={notification.id} to={"/notifications/"+notification.id} >
							<li className={notificationClass} >
									{notification.title}
							</li>
							</Link>
						);
						})
			}
		</ul>
		</div>
		);
		}
});

var NotificationArea = React.createClass({
	render: function() {
		var unreadNotifications = 0;
		this.props.notifications.forEach(function(notification) {
			if (notification.unread){
				unreadNotifications += 1;
			}	
		});
		return (
			<div className="notification-area">
				<div className="notifications notification-item">
					<NotificationMenuItem item={{
											name: "notifications",
											title: "home.notifications",
											image: "Icons/SVG/info.svg",
											link: "#"
											}}
							unreadNotifications={unreadNotifications}
							notifications={this.props.notifications}
					/>
				</div>
				<div className="settings notification-item">
					<SettingsMenuItem item={{
											name: "settings",
											title: "home.settings",
											image: "Icons/SVG/settings.svg",
											link: "#settings"
											}}
					/>
				</div>
				
				{//<NotificationList items={this.props.notifications} shown={this.state.notificationsShown} />
				}
			</div>	
		);
	}
});


var Header = React.createClass({
	contextTypes: {
		//profile: React.PropTypes.object,
		isAuthenticated: React.PropTypes.bool
	},
	render: function() {
		return (
			<header className="site-header">
					{(() => {
						if (this.context.isAuthenticated) {
							return (
								<div className="container">
									<div className="header-left">
										<MainLogo />
										<MainMenu />
									</div>
									<div className="header-right">
										<NotificationArea notifications={this.props.data.notifications} />
										<UserInfo />
										<LoginForm 	className="navbar logout"
											action="logout"
											isAuthenticated = { this.context.isAuthenticated } />
										<LocaleSwitcher />	
									</div>
								</div>
								);
						}
						else{
							return (
								<div className="container">
									<div className="pull-left">
										<MainLogo />
									</div>
									<div className="pull-right">
										<LocaleSwitcher />	
									</div>
								</div>
								);
						}
					})() 
					}
			</header>
		);
	}
});

Header.NotificationList = NotificationList;

module.exports = Header;
