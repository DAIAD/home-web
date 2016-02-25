var React = require('react');
var injectIntl = require('react-intl').injectIntl;
var bs = require('react-bootstrap');
//var link = require('react-router').link;
var FormattedMessage = require('react-intl').FormattedMessage;
var FormattedTime = require('react-intl').FormattedTime;
var FormattedRelative = require('react-intl').FormattedRelative;


var SessionsChart = require('./SessionsChart');

var MainSection = require('./MainSection.react');
var Sidebar = require('./Sidebar.react');
var timeUtil = require('../utils/time');

var SessionItem = React.createClass({
	render: function() {
		if (!this.props.data) return (null);
		return (
			<li className="session-item-inner">
				<h4>{this.props.title}</h4>
				<h4 style={{float:'right'}}>{this.props.data} <span>{this.props.mu}</span></h4>
				{
					this.props.details?(<p>{this.props.details}</p>):null
				}
			</li>
		);
	}
});

var SessionInfo = React.createClass({
	render: function() {
		var data = this.props.data;
		if (!data) return <div/>;
		const metrics = [
			{id:'volume', mu:'lt',title:'Water', details:'Total water used for this shower'}, 
			{id:'temperature', mu:'C', title:'Temperature', details: 'Average water temperature'}, 
			{id:'energy',mu:'W', title:'Energy', details: 'Estimated energy used for hot water'}, 
			{id:'count',mu:'', title:'Aggregated Showers', details:'The total number of showers aggregated'}];	
		return (
			<div style={{marginTop: '50px'}}>
				<h4><FormattedMessage id="shower.details"/></h4>
				<b><FormattedTime value={new Date(data.timestamp)} date={{day:"numeric", month:"long", year:"numeric"}} time={{hours:"numeric", minutes:"numeric"}} /></b>
				<ul className="sessions-list" style={{marginTop:'30px'}}>
					{
						metrics.map(function(metric) {
							return (<SessionItem key={metric.id} title={metric.title} data={data[metric.id]} mu={metric.mu} details={metric.details} />);
						})
					}
				</ul>
		</div>
		);
	}
});


var Shower = React.createClass({
	
	handleTypeSelect: function(key){
		this.props.setSessionFilter(key);	
	},
	render: function() {
		const history = this.props.listData?this.props.listData.history:null;
		var _t = this.props.intl.formatMessage;
		
		if (!this.props.listData || this.props.loading){
			return (
				<MainSection id="section.shower">
					<span style={{position:'absolute'}} >Loading....</span>
				</MainSection>
			);
		
		}	
		else if (history === false){
			return (
				<div style={{overflow:'auto'}}>
					<Sidebar>
						<bs.Tabs position='left' tabWidth={20} activeKey={this.props.filter} onSelect={this.handleTypeSelect}>
							<bs.Tab eventKey="volume" title="Volume"/>
							<bs.Tab eventKey="temperature" title="Temperature"/>
							<bs.Tab eventKey="energy" title="Energy"/>
						</bs.Tabs>
					</Sidebar>
					<div className="primary">
					<SessionsChart
								height='350px'
								width='95%'	
								title="Shower"
								subtitle=""
								mu=""
								type="line"
								formatter={(x) => this.props.intl.formatTime(x, { hour: 'numeric', minute: 'numeric'})}
								data={this.props.chartData}
							/>
							
					<SessionInfo
						style={{marginTop: '50px'}}
						data={this.props.listData} />
					</div>

					
				</div>
			);
		}
		else {
			return (
				<div style={{overflow:'auto'}}>
					<Sidebar>
					</Sidebar>
					<div className="primary">
					
					<section>
						{ (() => 
							 {
								if (this.props.listData.count) {
									return (
										<h3>Aggregated data</h3>
										);
								}
								else {
									return (
										<h3>Oops, limited data...</h3>
										);
								}
							})()
						}

							
					<SessionInfo
						style={{marginTop: '50px'}}
						data={this.props.listData} />

					</section>
					</div>
				</div>
			);
		}
	}
		
});

Shower = injectIntl(Shower);
module.exports = Shower;
