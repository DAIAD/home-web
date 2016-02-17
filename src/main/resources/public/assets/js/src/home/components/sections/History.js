var React = require('react');
var FormattedMessage = require('react-intl').FormattedMessage;
var injectIntl = require('react-intl').injectIntl;
var bs = require('react-bootstrap');

var SessionsChart = require('../SessionsChart');
var SessionsList = require('../SessionsList');

var MainSection = require('../MainSection.react');
var Sidebar = require('../Sidebar.react');

var DeviceActions = require('../../actions/DeviceActions');

var getDefaultDevice = require('../../utils/device').getDefaultDevice;
var getDeviceByKey = require('../../utils/device').getDeviceByKey;

var History = React.createClass({
	componentWillMount: function() {
		if (this.props.activeDevice){
			this.props.searchSessions();
		}
	},
	componentWillReceiveProps: function(nextProps) {
		if (!this.props.activeDevice && nextProps.activeDevice){
			this.props.searchSessions();
		}
	},
	handleTypeSelect: function(key){
		this.props.setFilter(key);	
	},
	handleTimeSelect: function(key){
		var time = {};
		if (key==="always"){
			time = {
				startDate: new Date("2000-12-27T22:00:00").getTime(),
				endDate: new Date().getTime(),
				granularity: 0
			};

		}
		else if (key==="year"){
			time = {
				startDate: new Date("2014-01-01T22:00:00").getTime(),
				endDate: new Date().getTime(),
				granularity: 4
			};
		}
		else if (key==="month"){
			time = {
				startDate: new Date("2014-01-01T22:00:00").getTime(),
				endDate: new Date().getTime(),
				granularity: 3
			};
		}
		else if (key==="week"){
			time = {
				startDate: new Date("2014-01-01T22:00:00").getTime(),
				endDate: new Date().getTime(),
				granularity: 2
			};
		}
		else if (key==="day"){
			time = {
				startDate: new Date("2014-01-01T22:00:00").getTime(),
				endDate: new Date().getTime(),
				granularity: 1
			};
		}
		else{
			return;
		}
		this.props.setTime(time);

	},
	handleDeviceChange: function(e, value) {
		this.props.setActive(value);
	},
	render: function() {
		const activeDevice = this.props.activeDevice;
		const device = getDeviceByKey(this.props.devices, activeDevice);
		const activeDeviceName = device?device.name:"None selected";
		
		var _t = this.props.intl.formatMessage;
		return (
			<section className="section-history">
				<h3><FormattedMessage id="section.history"/></h3>
					{
						(() => {
							if (this.props.loading){
								return (
									<span style={{position:'absolute'}} >Loading....</span>
									);
							}
							})()
					}
						<div>
						
							<Sidebar>	
							<bs.Tabs position='left' tabWidth={20} activeKey={this.props.filter} onSelect={this.handleTypeSelect}>
								<bs.Tab eventKey="showers" title="Showers" />
								<bs.Tab eventKey="duration" title="Duration" />
								<bs.Tab eventKey="volume" title="Volume"/>
								<bs.Tab eventKey="temperature" title="Temperature"/>
								<bs.Tab eventKey="energy" title="Energy"/>
								<bs.Tab eventKey="flow" title="Flow"/>
							</bs.Tabs>
						</Sidebar>
						
						<section className="section-dashboard primary">
						
							<bs.Tabs  position='top' tabWidth={3} activeKey="always" onSelect={this.handleTimeSelect}>
								<bs.Tab eventKey="always" title="Since the beginning of time" />
								<bs.Tab eventKey="year" title="This year"/>
								<bs.Tab eventKey="month" title="This month"/>
								<bs.Tab eventKey="week" title="This week"/>
								<bs.Tab eventKey="day" title="Today"/>
							</bs.Tabs>
							
							<bs.DropdownButton
								style={{float:"right"}}
								title={activeDeviceName}
								id="device-switcher"
								defaultValue={activeDevice}
								onSelect={this.handleDeviceChange}>
								{
									this.props.devices.map(function(device) {
										return (
											<bs.MenuItem key={device.deviceKey} eventKey={device.deviceKey} value={device.deviceKey} >{device.name}</bs.MenuItem>
										);
								})
							}	
						</bs.DropdownButton>


							<SessionsChart
							 	height='350px'
								width='100%'	
								title="Chart"
								subtitle=""
								mu=""
								type="bar"
								data={this.props.chartData}
								/>
			
							<SessionsList
								fetchSession={this.props.fetchSession}
								sessions={this.props.listData}
							/>

						</section>
						</div>

			</section>
		);
	}
});

History = injectIntl(History);
module.exports = History;
