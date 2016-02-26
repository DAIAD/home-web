var React = require('react');
var injectIntl = require('react-intl').injectIntl;

var Chart = require('./Chart');

var SessionsChart = React.createClass({
	getDefaultProps: function() { 
		return {
			height: '350px',
			width: '100%',
			title: "History",
			subtitle: "",
			mu: "",
			type: "bar"
		};
	},
	render: function() {
		var colors = ['#2D3580', '#CD4D3E'];
		var seriesArray = this.props.data.map((x, i) => { return {
			name: 'Shower #'+(i+1),
			type: this.props.type,
			stack: this.props.type==='bar'?'name':null,
			showAllSymbol: true,
			itemStyle: {
				normal: {
					color: colors[i],
						barBorderColor: colors[i],
						barBorderWidth: 15,
						barBorderRadius:5,
						label : {
								show: false, 
								position: 'insideTop',
								textStyle: '#666'
						},
						textStyle: '#666'
				}
			},
			data: x,
			markLine : {
					data : [
							{type : 'average', name: 'Average'}
					]
			}
		};
		});
		return (
			<Chart
				style={{
					height: this.props.height,
					width: this.props.width,
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
						color: ['#2D3580', '#A45476'],
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
									scale: true,
									min: this.props.xMin,
									max: this.props.xMax,
									axisLabel : {
										formatter: this.props.formatter
										//this.props.formatter?this.props.formatter:defaultFormatter
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
						series : seriesArray
					}}	
				/>
		);
	}
});

const defaultFormatter = function(timestamp){
	var date = new Date(timestamp);
	return (date.getDate() + '/' +
					(date.getMonth()+1) + '/' +
					date.getFullYear());
};

SessionsChart = injectIntl(SessionsChart);
module.exports = SessionsChart;
