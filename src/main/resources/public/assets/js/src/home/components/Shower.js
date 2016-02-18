var React = require('react');
var FormattedMessage = require('react-intl').FormattedMessage;
var injectIntl = require('react-intl').injectIntl;
var bs = require('react-bootstrap');
var Link = require('react-router').Link;

var SessionsChart = require('./SessionsChart');

var MainSection = require('./MainSection.react');
var Sidebar = require('./Sidebar.react');

var DeviceActions = require('../actions/DeviceActions');

var SessionInfo = React.createClass({
	render: function() {
		var data = this.props.data;
		if (!data) return <div/>;
		
		var array = Array.from(entries(data));

		return (
			<div>
			<h3>Shower info</h3>
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
		</div>
		);
	}
});

function* entries(obj) {
	   for (var key of Object.keys(obj)) {
			      yield [key, obj[key]];
		}
}

var Shower = React.createClass({
	
	handleTypeSelect: function(key){
		this.props.setSessionFilter(key);	
	},
	render: function() {
		const history = this.props.listData?this.props.listData.history:null;
		var _t = this.props.intl.formatMessage;
		
		if (!this.props.listData || this.props.loading){
			return (
				<span style={{position:'absolute'}} >Loading....</span>
			);
		
		}	
		else if (history === false){
			return (
				<div>
					<Sidebar>
						<bs.Tabs position='left' tabWidth={20} activeKey={this.props.filter} onSelect={this.handleTypeSelect}>
							<bs.Tab eventKey="volume" title="Volume"/>
							<bs.Tab eventKey="temperature" title="Temperature"/>
							<bs.Tab eventKey="energy" title="Energy"/>
						</bs.Tabs>
					</Sidebar>
					
					<section className="section-history primary">
					<SessionsChart
								height='350px'
								width='100%'	
								title="Chart"
								subtitle=""
								mu=""
								type="line"
								data={this.props.chartData}
							/>
							
					<SessionInfo
						style={{marginTop: '50px'}}
						data={this.props.listData}
					/>

					<Link to="/history">Back to all showers</Link>
				</section>
					
				</div>
			);
		}
		else {
			return (
				<div>
					<Sidebar>
					</Sidebar>
					
					<section className="section-history primary">
						<h3>Oops, limited data...</h3>
							
					<SessionInfo
						style={{marginTop: '50px'}}
						data={this.props.listData}
					/>

					<Link to="/history">Back to all showers</Link>
				</section>
				</div>
			);
		}
	}
		
});

Shower = injectIntl(Shower);
module.exports = Shower;
