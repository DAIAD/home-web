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
  		var _t = this.props.intl.formatMessage;

  		return (
  			<div style={{ padding: 10, marginLeft: 'auto', marginRight: 'auto', maxWidth: 1157 }}>
		 	<Bootstrap.Tabs activeKey={this.state.key} onSelect={this.selectSection}>
		        <Bootstrap.Tab eventKey={1} title={_t({ id: 'Section.Dashboard'})}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={2} title={_t({ id: 'Section.Analytics'})}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={3} title={_t({ id: 'Section.Forecasting'})}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={4} title={_t({ id: 'Section.Demographics'})}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={5} title={_t({ id: 'Section.Search'})}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={6} title={_t({ id: 'Section.Alerts_Notifications'})}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={7} title={_t({ id: 'Section.Settings'})}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={8} title={_t({ id: 'Section.Reporting'})}></Bootstrap.Tab>
		        <Bootstrap.Tab eventKey={9} title={_t({ id: 'Section.Export'})}></Bootstrap.Tab>
		    </Bootstrap.Tabs>
		    </div>
 		);
  	}
});

module.exports = injectIntl(SectionCollection);
