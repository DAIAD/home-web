var React = require('react');
var FormattedMessage = require('react-intl').FormattedMessage;
var bs = require('react-bootstrap');

var MainSection = require('../MainSection.react');
var Sidebar = require('../Sidebar.react');

//sub-containers
var HistoryChart = require('../../containers/HistoryChart');
var HistoryList = require('../../containers/HistoryList');

//actions
var DeviceActions = require('../../actions/DeviceActions');

var getDefaultDevice = require('../../utils/device').getDefaultDevice;
var getDeviceByKey = require('../../utils/device').getDeviceByKey;

var timeUtil = require('../../utils/time');

var History = React.createClass({
	
	handleTypeSelect: function(key){
		this.props.setQueryFilter(key);	
	},
	handleTimeSelect: function(key){
		var time = {};
		if (key==="always"){
			time = {
				startDate: new Date("2000-02-18").getTime(),
				endDate: new Date("2016-02-25").getTime(),
				granularity: 0
			};
		}
		else if (key==="year"){
			time = timeUtil.thisYear();
			time.granularity = 4;
		}
		else if (key==="month"){
			time = timeUtil.thisMonth();
			time.granularity = 2;
		}
		else if (key==="week"){
			time = timeUtil.thisWeek();
			time.granularity = 0;
		}
		else if (key==="day"){
			time = timeUtil.today();
			time.granularity = 0;
		}
		else{
			return;
		}
		this.props.setTimeFilter(key);
		this.props.setTime(time);
		this.props.querySessions(this.props.activeDevice, time);

	},
	handleDeviceChange: function(e, value) {
		this.props.setActive(value);
		this.props.querySessions(value, this.props.time);
	},
	render: function() {
		const activeDevice = this.props.activeDevice;
		const device = getDeviceByKey(this.props.devices, activeDevice);
		const activeDeviceName = device?device.name:"None";
		
		var _t = this.props.intl.formatMessage;
		return (
			<MainSection id="section.history">
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
						<bs.Tabs style={{marginTop: '50px'}} position='left' tabWidth={20} activeKey={this.props.metricFilter} onSelect={this.handleTypeSelect}>
							<bs.Tab eventKey="showers" title={_t({id: "history.showers"})}/>
							<bs.Tab eventKey="duration" title={_t({id: "history.duration"})} />
							<bs.Tab eventKey="volume" title={_t({id: "history.volume"})}/>
							<bs.Tab eventKey="temperature" title={_t({id: "history.temperature"})}/>
							<bs.Tab eventKey="energy" title={_t({id: "history.energy"})}/>
						</bs.Tabs>
					</Sidebar>
					
					<div className="primary">
						
						<div className="pull-right">
							<bs.DropdownButton
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
						</div>

						<bs.Tabs  position='top' tabWidth={3} activeKey={this.props.timeFilter} onSelect={this.handleTimeSelect}>
							<bs.Tab eventKey="always" title={_t({id: "history.always"})} />
							<bs.Tab eventKey="year" title={_t({id: "history.year"})}/>
							<bs.Tab eventKey="month" title={_t({id: "history.month"})}/>
							<bs.Tab eventKey="week" title={_t({id: "history.week"})}/>
							<bs.Tab eventKey="day" title={_t({id: "history.day"})}/>
						</bs.Tabs>

						<HistoryChart />
						
						<HistoryList />

					</div>
				</div>

				</MainSection>
		);
	}
});

module.exports = History;
