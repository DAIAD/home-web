var React = require('react');
var Chart = require('./Chart');

var SessionsChart = React.createClass({
	render: function() {
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
												formatter: defaultFormatter
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
													barBorderWidth: 5,
													barBorderRadius:5,
													label : {
															show: false, 
															position: 'insideTop',
															textStyle: '#666'
													},
													textStyle: '#666'
											}
            				},
										data: this.props.data,
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

const defaultFormatter = function(timestamp){
	var date = new Date(timestamp);
	return (date.getDate() + '/' +
					(date.getMonth()+1) + '/' +
					date.getFullYear());
};

module.exports = SessionsChart;
