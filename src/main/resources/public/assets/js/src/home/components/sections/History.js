var React = require('react');
var FormattedMessage = require('react-intl').FormattedMessage;

var Chart = require('../Chart');

var MainSection = require('../MainSection.react');


//var parseDate = d3.time.format("%YM%m").parse;

var History = React.createClass({
	render: function() {
		var sampling = [];
		var average = [];
		for (var i = 0; i < 200; i++) {
			sampling.push([i, Math.random() * i * 4]);
			average.push([i, 200]);
		}

		var dataStyle = {
				normal: {
						label: {show:false},
						labelLine: {show:false}
				}
			};
		var placeHolderStyle = {
			normal : {
        color: 'rgba(0,0,0,0)',
        label: {show:false},
        labelLine: {show:false}
    },
    emphasis : {
        color: 'rgba(0,0,0,0)'
    }
		};
		return (
			<section className="section-history">
				<h3><FormattedMessage id="section.history"/></h3>
					<Chart 
						style={{
							height:'400px',
							width: '100%'
						}} 
						options={{
							title: {
									text: 'February',
									subtext: 'budget',
									x: 'center',
									y: 'center',
									itemGap: 20,
									textStyle : {
											color : '#666',
											//fontFamily : '',
											fontSize : 25,
											fontWeight : 'bold'
									}
							},
							tooltip : {
									show: true,
							},
							legend: {
									orient : 'horizontal',
									x : 45,
									y : 45,
									itemGap:12,
									data:['Total','Last month','Today']
							},
							toolbox: {
									show : true,
									feature : {
											mark : {show: true},
											dataView : {show: true, readOnly: false},
											restore : {show: true},
											saveAsImage : {show: true}
									}
							},
							series : [
									{
											name:'Total',
											type:'pie',
											radius : [125, 150],
											itemStyle : dataStyle,
											data:[
													{
															value:120,
															name:'Total'
													}
													
											]
									},
									{
											name:'Last month',
											type:'pie',
											radius : [100, 125],
											itemStyle : dataStyle,
											data:[
													{
															value:29, 
															name:'Last month'
													},
													{
															value:71,
															name:'invisible',
															itemStyle : placeHolderStyle
													}
											]
									},
									{
											name:'Today',
											type:'pie',
											//clockWise:false,
											radius : [75, 100],
											itemStyle : dataStyle,
											data:[
													{
															value:3, 
															name:'Today'
													},
													{
															value:97,
															name:'invisible',
															itemStyle : placeHolderStyle
													}
											]
									}
							]
						}}
					/>

					<Chart 
						style={{
							height:'400px',
							width: '100%'
						}} 
						options = {{
								title : {
										text: 'Consumption',
										subtext: ''
								},
								tooltip : {
										trigger: 'axis'
								},
								legend: {
										data:['Consumption']
								},
								toolbox: {
										show : true,
										feature : {
												mark : {show: true},
												dataView : {show: true, readOnly: false},
												magicType : {show: true, type: ['line', 'bar']},
												restore : {show: true},
												saveAsImage : {show: true}
										}
								},
								backgroundColor: 'rgba(55,230,123,0.3)',
								color: ['#45654F', '#A45476'],
								calculable : false,
								dataZoom: {
									show: true,
									realtime: true,
									height: 40,
									y: 0,
									start: 0,
									end: 100
								},
								xAxis : [
										{
												type : 'category',
												boundaryGap : false,
												data : ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
										}
								],
								yAxis : [
										{
												type : 'value',
												axisLabel : {
														formatter: '{value} lt'
												}
										}
								],
								series : [
										{
												name:'Consumption',
												type:'line',
												data:[0, 11, 15, 13, 12, 13, 10],
												markPoint : {
														data : [
															//	{type : 'max', name: 'min'},
														//		{type : 'min', name: 'max'}
														]
												},
												markLine : {
														data : [
																{type : 'average', name: 'Average'}
														]
												}
										},
								]
							}}
						/>
					{
						/*
					<Chart
						{...{
							title: {
								text: 'Hello',
								subtext: 'world'
							},
							series: [
								{
									name:	'consumption',
									type: 'line',
									data: sampling,
									markLine: {
										data: [
											{type: 'min', name:'Min'},
											{type: 'max', name:'Max'}
										]
									}
								}
							],
						}
						}

					/>
					*/
					}
			</section>
		);
	}
});

module.exports = History;
