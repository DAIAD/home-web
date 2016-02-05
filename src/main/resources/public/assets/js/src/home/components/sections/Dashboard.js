var React = require('react');

var bs = require('react-bootstrap');

var MainSection = require('../MainSection.react');
var Sidebar = require('../Sidebar.react');

var Constant = require('../../constants/HomeConstants');
var UserStore = require('../../stores/UserStore');
var DeviceStore = require('../../stores/DeviceStore');

var HomeActions = require('../../actions/HomeActions');

var Chart = require('../Chart');
var defaultFormatter = function(timestamp){
	var date = new Date(timestamp);
	return (date.getDate() + '/' +
					date.getMonth() + '/' +
					date.getFullYear());
};
var yearFormatter = function(timestamp){
	var date = new Date(timestamp);
	return (date.getMonth());
};
var monthFormatter = function(timestamp){
	var date = new Date(timestamp);
	return (date.getDate());
};
var weekFormatter = function(timestamp){
	var date = new Date(timestamp);
	return (date.getDay());
};
var dayFormatter = function(timestamp){
	var date = new Date(timestamp);
	return (date.getHours() + ':' +
		date.getMinutes());
};

/* Be Polite, greet user */
var SayHello = React.createClass({
	render: function() {
		var name = UserStore.getProfile().firstname;
		return (
			<div >
				<h3>Hello {name}!</h3>
			</div>
		);
	}
});

var Sessions = React.createClass({
	getInitialState: function() {
		return {
			timeSelection: "always",
			typeSelection: "showers",
			chart:{
				title: 'Showers',
				type: 'bar',
				mu: '',
			},
			chartFormatter: defaultFormatter,
			chartData: []
		};
	},
	componentDidMount: function() {
		console.log('component mounted');
		var data = this._getSessionData({granularity:0, start:new Date("2000-01-01"), end:new Date()});
		HomeActions.searchSessions(data);
		//DeviceStore.addSessionSearchListener(this._searchDone);
		
		//HomeActions.getSession(data);
		DeviceStore.addSessionSearchListener(this._sessionsFetched);
		//DeviceStore.addSessionGetListener(this._sessionMeasurementsFetched);
	},
	componentWillUnmount: function() {

		DeviceStore.removeSessionSearchListener(this._sessionsFetched);
		//DeviceStore.removeSessionGetListener(this._sessionMeasurementsFetched);
	},
	_getSessionData: function(properties) {
		var devices = []; 
		UserStore.getProfile().devices.forEach(function(device) {
			if (device.type == 'AMPHIRO'){
				devices.push(device.deviceKey);
			}
		});
		
		return {
			deviceKey: devices,
			granularity: properties.granularity,
			startDate: properties.start.getTime(),
			endDate: properties.end.getTime()
		};
	},
	//sessions callback
	_sessionsFetched: function() {
		console.log('sessions fetched');
		//this.forceUpdate();
		console.log(this.state.typeSelection);
		this.setState({
			chartData: DeviceStore.getSessionsMetric(this.state.typeSelection)
		});
		//_setChartData('Volume', 'lt', 'line', 'volume');
		//this._setChartData('Showers', '', 'bar','count');
	},
	handleTimeSelect: function(key) {
		this.setState({
			timeSelection:key,
		});
		console.log('handling select');
		console.log(key);
		var properties = {};
		if (key==="always"){
			properties = {
				granularity: 0,
				start: new Date("2000-01-01"),
				end: new Date()
			};
		}
		else if (key==="year"){
			properties = {
				granularity: 4,
				start: new Date("2015-01-01"),
				end: new Date("2015-12-31")
			};
		}
		else if (key==="month"){
			properties = {
				granularity: 3,
				start: new Date("2015-12-01"),
				end: new Date("2015-12-31")
			};
		}
		else if (key==="week"){
			properties = {
				granularity: 2,
				start: new Date("2015-12-22"),
				end: new Date("2015-12-28")
			};
		}
		else if (key==="day"){
			properties = {
				granularity: 0,
				start: new Date("2015-12-27T22:00:00"),
				end: new Date("2015-12-28T23:59:59")
			};
		}
		else{
			return;
		}
		console.log('getting data');
		console.log(properties);
		var data = this._getSessionData(properties);
		console.log(data);
		HomeActions.searchSessions(data);
	},
	handleTypeSelect: function(key){
		this.setState({typeSelection:key});
		if (key==="showers"){
			this.setState({
				chart:{
					title: 'Showers',
					type: 'bar',
					mu: '',
				},
				chartData: DeviceStore.getSessionsShowers()
			});
		}
		else if (key==="duration"){
			this.setState({
				chart:{
					title: 'Duration',
					type: 'line',
					mu: 'sec',
				},
				chartData: DeviceStore.getSessionsDuration()
			});
		}
		else if (key==="volume"){
			this.setState({
				chart:{
					title: 'Volume',
					type: 'line',
					mu: 'lt',
				},
				chartData: DeviceStore.getSessionsVolume()
			});
		}
		else if (key==="temperature"){
			this.setState({
				chart:{
					title: 'Temperature',
					type: 'line',
					mu: ' C',
				},
				chartData: DeviceStore.getSessionsTemperature()
			});
		}
		else if (key==="energy"){
			this.setState({
				chart:{
					title: 'Energy',
					type: 'line',
					mu: 'Kwh',
				},
				chartData: DeviceStore.getSessionsEnergy()
			});
		}
		else if (key==="flow"){
			this.setState({
				chart:{
					title: 'Flow',
					type: 'line',
					mu: 'lt/min',
				},
				chartData: DeviceStore.getSessionsFlow()
			});
		}
		else{
			return;
		}

	},
	render: function() {
		console.log('component rendered');
		return (
			<div>
			
				<Sidebar>	
				<bs.Tabs style={{marginTop:'70px'}} position='left' tabWidth={20} activeKey={this.state.typeSelection} onSelect={this.handleTypeSelect}>
					<bs.Tab eventKey="showers" title="Showers" />
					<bs.Tab eventKey="duration" title="Duration" />
					<bs.Tab eventKey="volume" title="Volume"/>
					<bs.Tab eventKey="temperature" title="Temperature"/>
					<bs.Tab eventKey="energy" title="Energy"/>
					<bs.Tab eventKey="flow" title="Flow"/>
				</bs.Tabs>
			</Sidebar>
			
			<section className="section-dashboard primary">
				<SayHello />
			
				<bs.Tabs  position='top' tabWidth={3} activeKey={this.state.timeSelection} onSelect={this.handleTimeSelect}>
					<bs.Tab eventKey="always" title="Since the beginning of time" />
					<bs.Tab eventKey="year" title="This year"/>
					<bs.Tab eventKey="month" title="This month"/>
					<bs.Tab eventKey="week" title="This week"/>
					<bs.Tab eventKey="day" title="Today"/>
				</bs.Tabs>


				<ChartSessions 
					title={this.state.chart.title}
					subtitle=""
					mu={this.state.chart.mu}
					type={this.state.chart.type}
					chartData={this.state.chartData} 
					formatter={this.state.chartFormatter}
					/>
			
			</section>
			</div>

		);
	}
});
var ChartSessions = React.createClass({
	propTypes: {
		title: React.PropTypes.string,
		subtitle: React.PropTypes.string,
		type: React.PropTypes.string,
		mu: React.PropTypes.string,
		chartData: React.PropTypes.array,
		formatter: React.PropTypes.function
	},
	render: function() {
		console.log('rendering chart');
		console.log(this.props);
		return (
			<Chart 
				style={{
					height:'400px',
					width: '100%',
				}} 
				options = {{
						title : {
								text: this.props.title,
								subtext: this.props.subtitle
						},
						tooltip : {
								//trigger: 'axis'
								trigger: 'item'
						},
						legend: {
							//data:[this.props.title]
						},
						toolbox: {
								show : false,
						},
						backgroundColor: 'rgba(55,230,123,0.0)',
						color: ['#666', '#A45476'],
						calculable : false,
						dataZoom: {
							show: true,
							realtime: true,
							start: 0,
							end: 100
						},
						xAxis : [
								{
									type : 'time',
									splitNumber: 0,
									axisLabel : {
												formatter: this.props.formatter
										},
										boundaryGap: [0, 0.1]
								},
						],
						yAxis : [
								{
										type : 'value',
										axisLabel : {
											formatter: '{value} ' + this.props.mu
												/*
												function(value){
												if (value > 3600){
													return Math.floor(value/3600) + ' h';
												}
												else if (value > 60){
													return Math.floor(value/60) + ' min';
												}
												else{
													return value + ' sec';
												}
												}
												*/
										},
										boundaryGap: [0, 0.1]
								}
						],
						series : [
								{
										name:this.props.title,
										type:this.props.type,
										showAllSymbol: true,
										itemStyle: {
											normal: {
													color: '#666',
													barBorderColor: '#666',
													barBorderWidth: 2,
													barBorderRadius:5,
													label : {
															show: false, 
															position: 'insideTop',
															textStyle: '#666'
													},
													textStyle: '#666'
											}
            				},
										data: this.props.chartData,
										markLine : {
												data : [
														{type : 'average', name: 'Average'}
												]
										}
								},
						]
					}}
				/>
		);
	}
});
var Dashboard = React.createClass({
	render: function() {
		return (
			<Sessions />
		);
	}
});

module.exports = Dashboard;
