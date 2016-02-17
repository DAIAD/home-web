var React = require('react');

var injectIntl = require('react-intl').injectIntl;
var Constants = require('../constants/HomeConstants');
var LocaleSwitcher = require('./LocaleSwitcher');
var LoginForm = require('./LoginForm');

var FormattedMessage = require('react-intl').FormattedMessage;
var bs = require('react-bootstrap');
var Link = require('react-router').Link;


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
	render: function() {
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
									title: "section.dashboard",
									image: "Icons/SVG/dashboard.svg",
									route:"/dashboard"
								},
								{
									name: "history",
									title: "section.history",
									image: "Icons/SVG/stats.svg",
									route:"/history"
								},
								{
									name: "commons",
									title: "section.commons",
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
	render: function() {
		var _t = this.props.intl.formatMessage;
		return (
			<div className="user-menu" >
					<div title={_t({id: "section.profile"})}>
					<Link to="settings/profile">
						<span>{this.props.firstname}</span>
					</Link>
				</div>
			</div>
		);
	},
});

/* Notification Area */

var SettingsMenuItem = React.createClass({
	render: function() {
		var _t = this.props.intl.formatMessage;
		return (
			<div title={_t({id: this.props.item.title})}>
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
		var _t = this.props.intl.formatMessage;
		return (
			<bs.OverlayTrigger 
				id="notifications-trigger"
				trigger="click"
				title={_t({id: this.props.item.title})}
				rootClose
				placement="bottom"
				overlay={<bs.Popover 
					id="notifications-popover"
					title={_t({id: this.props.item.title})} >
					<div className="scrollable">
						<NotificationList notifications={this.props.notifications} />
					</div>
					<div className="footer">
						<Link className="notifications-show-all" to="/notifications">{_t({id:"notifications.showAll"})}</Link>
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
					<NotificationMenuItem 
						intl={this.props.intl}
						item={{
											name: "notifications",
											title: "section.notifications",
											image: "Icons/SVG/info.svg",
											link: "#"
											}}
							unreadNotifications={unreadNotifications}
							notifications={this.props.notifications}
					/>
				</div>
				<div className="settings notification-item">
					<SettingsMenuItem 
						intl={this.props.intl}
						item={{
											name: "settings",
											title: "section.settings",
											image: "Icons/SVG/settings.svg",
											link: "#settings"
											}}
					/>
				</div>
			</div>	
		);
	}
});


var Header = React.createClass({
	render: function() {
		return (
			<header className="site-header">
					{(() => {
						if (this.props.isAuthenticated) {
							return (
								<div className="container">
									<div className="header-left">
										<MainLogo />
										<MainMenu />
									</div>
									<div className="header-right">
										<NotificationArea
											intl={this.props.intl}
											notifications={this.props.data.notifications} />
										<UserInfo
											intl={this.props.intl}
											firstname={this.props.firstname}
											/>
										<LoginForm 
											isAuthenticated={this.props.isAuthenticated}
											onLogout={this.props.onLogout}
											className="navbar logout"
											action="logout"
											 />
										 <LocaleSwitcher
											 onLocaleSwitch={this.props.onLocaleSwitch}
											 locale={this.props.locale}
											 />	
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
										<LocaleSwitcher
											 onLocaleSwitch={this.props.onLocaleSwitch}
											 locale={this.props.locale}
											 />	

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




Header = injectIntl(Header);
Header.NotificationList = NotificationList;
module.exports = Header;

