// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');

var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;

var Bootstrap = require('react-bootstrap');


var SectionCollection = React.createClass({
	getInitialState() {
		return {
			key: 1
    	};
	},
	
	selectSection(key) {
		this.setState({key});
  	},
  
  	render: function() {	 
  		return (
  			<div style={{ padding: 10, marginLeft: 'auto', marginRight: 'auto', maxWidth: 1157 }}>
		 	<Bootstrap.Tabs activeKey={this.state.key} onSelect={this.selectSection}>
		        <Bootstrap.Tab eventKey={1} title="Dashboard"></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={2} title="Analytics"></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={3} title="Forecasting"></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={4} title="Demographics"></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={5} title="Search"></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={6} title="Alerts / Notifications"></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={7} title="Settings"></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={8} title="Reporting"></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={9} title="Export"></Bootstrap.Tab>
		    </Bootstrap.Tabs>
		    </div>
 		);
  	}
});

module.exports = injectIntl(SectionCollection);
