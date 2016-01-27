var React = require('react');
var Constant = require('../../../constants/HomeConstants');


var Alerts = React.createClass({
	render: function() {
		var unreadNotifications = 0;
		Constant.data.notifications.forEach(function(notification) {
			if (notification.unread){
				unreadNotifications += 1;
			}	
		});
		return (
			<div>
				<h3>Alerts</h3>
			</div>
		);
	}
});

module.exports = Alerts;
