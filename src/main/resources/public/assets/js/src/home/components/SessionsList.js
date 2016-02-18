var React = require('react');
var Link = require('react-router').Link;

var Chart = require('./Chart');

//Actions
var DeviceActions = require('../actions/DeviceActions');

var SessionLink = React.createClass({
	
	render: function() {
		return (
			<li>	
					{
						(() => {
								return (
									<Link to={"/history/"+ this.props.data.id} >
										<h4>
										{
											this.props.data.id 
										}
										{ 
											this.props.data.history?" (h)":""
										}
										
									</h4>
								</Link>
								);

						})()
					}
			</li>
		);
	}
});

var SessionInfo = React.createClass({
	render: function() {
		var data = this.props.data;
		var array = Array.from(entries(data));

		return (
			<ul>
				{
					this.props.activeSession?
				(<li>
					<b>measurements:</b><span>{data.measurements?data.measurements.length:'null'}</span>
				</li>):<div/>
				}
				{
					array.map(function(dato) {
						const prop = dato[0];
						const value = dato[1];
							
						if (typeof(value)==="object") return;
					return (
						<li key={prop}>
							<b>{prop}:</b> <span>{value}</span>
						</li>
					);
					})
				}
		</ul>
		);
	}
});

function* entries(obj) {
	   for (var key of Object.keys(obj)) {
			      yield [key, obj[key]];
		}
}


var SessionsList = React.createClass({
	
	render: function() {
		return (
			<div style={{marginTop: '50px'}}>
				<h3>List</h3>
				<ul>
					{
						this.props.sessions.map((session) => (
							<SessionLink
								key={session.id}
								data={session}
								fetchSession={this.props.fetchSession}
							/>	
							))
					}
				</ul>
					
			</div>
		);
	}
});



module.exports = SessionsList;
