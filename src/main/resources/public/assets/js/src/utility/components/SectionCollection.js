var React = require('react');
var ReactDOM = require('react-dom');

var FormattedMessage = require('react-intl').FormattedMessage;
var Bootstrap = require('react-bootstrap');

var Section = {};
Section.Dashboard = require('./section/Dashboard');
Section.Demographics = require('./section/Demographics');
Section.Settings = require('./section/Settings');

var SectionCollection = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getInitialState() {
		return {
			key: 1
    	};
	},

	selectSection(key) {
		this.setState({key});
  	},

  	render: function() {
  		var _t = this.context.intl.formatMessage;

  		const dashboardTitle = (
			<span>
				<i className="fa fa-dashboard fa-fw"></i>
				{_t({ id: 'Section.Dashboard'})}
  			</span>
		);

  		const analyticsTitle = (
  				<span>
  				<i className="fa fa-bar-chart fa-fw"></i>
  				{_t({ id: 'Section.Analytics'})}
  	  			</span>
		);
  		
  		const forecastingTitle = (
  				<span>
  				<i className="fa fa-line-chart fa-fw"></i>
  				{_t({ id: 'Section.Forecasting'})}
  	  			</span>
		);
  		
  		const demographicTitle = (
  				<span>
  				<i className="fa fa-group fa-fw"></i>
  				{_t({ id: 'Section.Demographics'})}
  	  			</span>
		);

  		const searchTitle = (
  				<span>
  				<i className="fa fa-search fa-fw"></i>
  				{_t({ id: 'Section.Search'})}
  	  			</span>
		);
  		
  		const alertTitle = (
			<span>
			<i className="fa fa-bell fa-fw"></i>
			{_t({ id: 'Section.Alerts_Notifications'})} <Bootstrap.Badge className='danger'>4</Bootstrap.Badge>
  			</span>
		);

  		const settingTitle = (
  				<span>
  				<i className="fa fa-cogs fa-fw"></i>
  				{_t({ id: 'Section.Settings'})}
  	  			</span>
		);

  		const reportTitle = (
  				<span>
  				<i className="fa fa-database fa-fw"></i>
  				{_t({ id: 'Section.Reporting'})}
  	  			</span>
		);
  		
  		const exportTitle = (
  				<span>
  				<i className="fa fa-download fa-fw"></i>
  				{_t({ id: 'Section.Export'})}
  	  			</span>
		);
  		return (
  			<div style={{ marginLeft: 'auto', marginRight: 'auto', maxWidth: this.props.width }}>
		 	<Bootstrap.Tabs activeKey={this.state.key} onSelect={this.selectSection}>
		        <Bootstrap.Tab eventKey={1} title={dashboardTitle}>
		        	<Section.Dashboard />
	        	</Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={2} disabled title={analyticsTitle}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={3} disabled title={forecastingTitle}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={4} title={demographicTitle}>
		        	<Section.Demographics />
		        </Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={5} disabled title={searchTitle}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={6} disabled title={alertTitle}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={7} disabled title={settingTitle}>
		        	<Section.Settings />
		        </Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={8} disabled title={reportTitle}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={9} disabled title={exportTitle}></Bootstrap.Tab>
		    </Bootstrap.Tabs>
		    </div>
 		);
  	}
});

module.exports = SectionCollection;
