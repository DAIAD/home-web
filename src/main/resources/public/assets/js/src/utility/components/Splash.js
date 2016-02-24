var React = require('react');

var Splash = React.createClass({
  	render: function() {
  		return (
			<div style={{ 	position: 'absolute',
							left: '50%',
							top: '50%',
							marginLeft: -50,
							marginTop: -25 }}>
				<img alt='' src='/assets/images/daiad-transparent.png' />
				<br />
				<i className="fa fa-refresh fa-2x fa-spin" style={{ color : '#2980b9', position: 'relative', left: 38 }}></i>
			</div>
 		);
  	}
});

module.exports = Splash;
