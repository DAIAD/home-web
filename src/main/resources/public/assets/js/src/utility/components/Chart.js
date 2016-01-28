var React = require('react');
var ReactDOM = require('react-dom');
var PortalMixin = require('./PortalMixin');
var echarts = require('echarts');

var Chart = React.createClass({

	mixins: [PortalMixin],
	
	render: function() {
		return (
			<div {...this.props} />
		);
	},

	componentDidMount: function() {
		this._chart = echarts.init(document.getElementById(this.getId())); 
 
		if(this.props.options) {
			this._chart.setOption(this.props.options);
		}
	},
	
	componentWillReceiveProps : function(nextProps) {
		if((this._chart) && (nextProps.options)) {
			this._chart.setOption(nextProps.options);
		}
	},

	componentWillUnmount : function() {

	},
	
	getChart: function() {
		return this._chart;
	}

});

module.exports = Chart;
