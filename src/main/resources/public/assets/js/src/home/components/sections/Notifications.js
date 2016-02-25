var React = require('react');

var Link = require('react-router').Link;

var Constant = require('../../constants/HomeConstants');

var Topbar = require('../Topbar.react');
var Sidebar = require('../Sidebar.react');
var MainSection = require('../MainSection.react');
var NotificationList = require('../Header.react').NotificationList;


var NotificationMessage = React.createClass({
	render: function() {
		if (!this.props.notification){
			return (<div />);
		}
		return (
			<div className="notification">
				<h3>{this.props.notification.title}</h3>
				<p>{this.props.notification.description}</p>
			</div>
		);
		}
});
var Notifications = React.createClass({
	//Constant.data.notifications
	
	render: function() {
		var notId = this.props.params.id;
		var notificationItem = null;
		if (notId){
			Constant.data.notifications.forEach(function(notification) {
				if (notId === notification.id) {
					notificationItem = notification;
				}
			});
		}

		return (
			<section>
				<Topbar> 
					<ul className="list-unstyled">
						<li><Link to="/notifications">All</Link></li>
						<li><Link to="/notifications/insights">Insights</Link></li>
						<li><Link to="/notifications/alerts">Alerts</Link></li>
						<li><Link to="/notifications/tips">Tips</Link></li>
					</ul>
				</Topbar>

				<MainSection id="section.notifications">
					<Sidebar>
						<NotificationList notifications={Constant.data.notifications} />		
					</Sidebar>

					<input 
						type="hidden" 
						ref= {
							function(i) { if (i!==null){ i.click(); } } 
						} />
					<NotificationMessage notification={notificationItem} />
				</MainSection>
			</section>
		);
	}
});

module.exports = Notifications;
