var React = require('react');
var ReactDOM = require('react-dom');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../Breadcrumb');
var Counter = require('../Counter');
var Message = require('../Message');
var Chart = require('../Chart');
var LeafletMap = require('../LeafletMap');
var Table = require('../Table');
var Select = require('react-select');
var DateRangePicker = require('react-bootstrap-daterangepicker');
var TimeDimensionSelect = require('../chart/dimension/TimeDimensionSelect');
var FilterTag = require('../chart/dimension/FilterTag');
var Timeline = require('../Timeline');

var WidthProvider = require('react-grid-layout').WidthProvider;
var ResponsiveReactGridLayout = require('react-grid-layout').Responsive;

ResponsiveReactGridLayout = WidthProvider(ResponsiveReactGridLayout);

var createPoints = function() {
	var points = [];
	
	for(var i=0; i<50; i++) {
		points.push([38.35 + 0.02 * Math.random(), -0.521 + 0.05 * Math.random(), Math.random()]);
	}
	
	return points;
};

var createSeries = function(ref, days, baseConsumption, offset) {
	var series = [];
	for(var d=0; d < days; d++) {
		series.push({
			volume: (baseConsumption + Math.random() * offset).toFixed(0),
			date: ref.clone().toDate()
		});
		ref.add(1, 'days');
	}

	return series;
};

var createDateLabels = function(ref, days) {
	var series = [];

	for(var d=0; d < days; d++) {
		series.push(ref.clone().toDate());
		ref.add(1, 'days');
	}

	return series;
};

var chartData = {
    series: [{
        legend: 'Smart Meter',
        xAxis: 'date',
        yAxis: 'volume',
        data: createSeries(moment(new Date()).subtract(28, 'days'), 29, 5000, 700)
    }, {
        legend: 'Amphiro',
        xAxis: 'date',
        yAxis: 'volume',
        data: createSeries(moment(new Date()).subtract(28, 'days'), 29, 2000, 300)
    }]
};
	
var Dashboard = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
    getInitialState: function() {
        return {
        	filter: null,
            population: [{ value: 'All', label: 'All', type: 1 }],
            ranges: {
				'Today': [moment(), moment()],
				'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
				'Last 7 Days': [moment().subtract(6, 'days'), moment()],
				'Last 30 Days': [moment().subtract(29, 'days'), moment()],
				'This Month': [moment().startOf('month'), moment().endOf('month')],
				'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
			},
			interval:[moment(new Date(2016, 1, 1)), moment(new Date(2016, 1, 29))],
			points: []
        };
    },
	
    setFilter: function(value) {
    	this.setState({ filter : value});
    },
    
    componentWillMount : function() {
		this.setState({points : createPoints()});
	},

  	render: function() { 		
        var chartOptions = {
            tooltip: {
                show: true
            }
        };
        
		var mapOptions = {
			center:	[38.35, -0.48], 
			zoom: 13
		};
        
		var chartTitle;
		if(this.state.filter) {
			chartTitle = (
				<span>
					<i className='fa fa-bar-chart fa-fw'></i>
					<span style={{ paddingLeft: 4 }}>Daily Consumption</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
						<Bootstrap.Button bsStyle='default' className='btn-circle'>
							<i className='fa fa-expand fa-fw'></i>
						</Bootstrap.Button>
					</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
						<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.setFilter.bind(this, null)}>
							<i className='fa fa-bar-chart fa-fw'></i>
						</Bootstrap.Button>
					</span>
				</span>
			);
		} else {		
			chartTitle = (
				<span>
					<i className='fa fa-bar-chart fa-fw'></i>
					<span style={{ paddingLeft: 4 }}>Daily Consumption</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
						<Bootstrap.Button bsStyle='default' className='btn-circle'>
							<i className='fa fa-expand fa-fw'></i>
						</Bootstrap.Button>
					</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
						<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.setFilter.bind(this, 'source')}>
							<i className='fa fa-database fa-fw'></i>
						</Bootstrap.Button>
					</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
						<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.setFilter.bind(this, 'spatial')}>
							<i className='fa fa-map fa-fw'></i>
						</Bootstrap.Button>
					</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
						<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.setFilter.bind(this, 'population')}>
							<i className='fa fa-group fa-fw'></i>
						</Bootstrap.Button>
					</span>
					<span style={{float: 'right',  marginTop: -3 }}>
						<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.setFilter.bind(this, 'time')}>
							<i className='fa fa-calendar fa-fw'></i>
						</Bootstrap.Button>
					</span>
				</span>
			);
		}
		
		var mapTitle = (
			<span>
				<i className='fa fa-map fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Daily Consumption Heatmap</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
					<Bootstrap.Button bsStyle='default' className='btn-circle'>
						<i className='fa fa-expand fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' >
						<i className='fa fa-database fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' >
						<i className='fa fa-bar-chart fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' >
						<i className='fa fa-group fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' >
						<i className='fa fa-calendar fa-fw'></i>
					</Bootstrap.Button>
				</span>
			</span>
		);
		
    	var onChangeInterval = function (event, picker) {
    		this.setState({
    			interval: [picker.startDate, picker.endDate]
    		});
    	};

        var intervalLabel ='';
        if(this.state.interval) {
        	var start = this.state.interval[0].format('DD/MM/YYYY');
        	var end = this.state.interval[1].format('DD/MM/YYYY');
        	intervalLabel = start + ' - ' + end;
        	if (start === end) {
        		intervalLabel = start;
        	}
        }    	
		var intervalConfig = (
				<div className='col-md-6'>
					<DateRangePicker	startDate={this.state.interval ? this.state.interval[0] : moment() } 
										endDate={this.state.interval ? this.state.interval[1] : moment()} 
										ranges={this.state.ranges} 
										onEvent={onChangeInterval.bind(this)}>
						<div className='clearfix Select-control' style={{ cursor: 'pointer', padding: '5px 10px', width: '100%'}}>
							<span>{intervalLabel}</span>
						</div>
	    			</DateRangePicker>
	    			<span className='help-block'>Select data time interval</span>
				</div>
			);

  		var onChangePopulation = function(val) {
  			this.setState({
  				population: (val ? val.map( (item) => { return item.value; } ) : [])
  			});
        };
        var renderOption = function(option) {
        	switch(option.type){
        		case 1:
            		return <span><i className='fa fa-group fa-fw'></i>{option.label}</span>;
        		case 2:
        			return <span><i className='fa fa-user fa-fw'></i>{option.label}</span>;
        	}
    	};
		var populationConfig = (
			<div className='col-md-6'>
				<Select name='population'
					multi={true}
					value={this.state.population}
	            	options={[
        	          	  { value: 'All', label: 'All', type: 1 },
            	          { value: 'Alicante', label: 'Alicante', type: 1 },
            	          { value: 'St. Albans', label: 'St. Albans', type: 1 },
            	          { value: 'User 1', label: 'User 1', type: 2 },
                    ]}
					optionRenderer={renderOption.bind(this)}
	            	onChange={onChangePopulation.bind(this)}
					clearable={true} 
				/>
				<span className='help-block'>Select users or groups</span>  
			</div>
		);

		var chart, chartFilterTags = [], map, mapFilterTags = [];

		switch(this.state.filter) {
			case 'time':
				chart = (
					<Bootstrap.ListGroupItem>
						<TimeDimensionSelect />
					</Bootstrap.ListGroupItem>
				);
				break;
			default:        
	        	chartFilterTags.push( 
        			<FilterTag key='time' text={intervalLabel} icon='calendar' onClick={this.setFilter.bind(this, 'time')} />
	        	);
	        	chartFilterTags.push( 
        			<FilterTag key='population' text='All' icon='group' onClick={this.setFilter.bind(this, 'population')} />
	        	);
	        	chartFilterTags.push( 
        			<FilterTag key='spatial' text='Alicante' icon='map' onClick={this.setFilter.bind(this, 'spatial')} />
	        	);
	        	chartFilterTags.push( 
        			<FilterTag key='source' text='Meter, Amphiro' icon='database' onClick={this.setFilter.bind(this, 'source')} />
	        	);
				chart = (
					<Bootstrap.ListGroupItem>
						<Chart 	style={{ width: '100%', height: 400 }} 
								elementClassName='mixin'
								prefix='chart'
								options={chartOptions}
								data={chartData}/>
					</Bootstrap.ListGroupItem>
				);
				break;
		}

		var onChangeTimeline = function(value) {
			console.log(value);
			this.setState({points: createPoints()});
		};
		
		switch(this.state.filter) {
			default:	        
				mapFilterTags.push( 
	    			<FilterTag key='time' text={intervalLabel} icon='calendar' onClick={this.setFilter.bind(this, 'time')} />
	        	);
				mapFilterTags.push( 
	    			<FilterTag key='population' text='Alicante' icon='group' onClick={this.setFilter.bind(this, 'population')} />
	        	);
				mapFilterTags.push( 
	    			<FilterTag key='spatial' text='Alicante' icon='map' onClick={this.setFilter.bind(this, 'spatial')} />
	        	);
				mapFilterTags.push( 
	    			<FilterTag key='source' text='Meter' icon='database' onClick={this.setFilter.bind(this, 'source')} />
	        	);
				map = (
					<Bootstrap.ListGroupItem>
						<LeafletMap style={{ width: '100%', height: 400}} 
									elementClassName='mixin'
									prefix='map'
									options={mapOptions}
									points={this.state.points} />
						<Timeline 	onChange={onChangeTimeline.bind(this)} 
									style={{paddingTop: 10}}
									min={1}
			            			max={29}
									type='date'
								    data={createDateLabels(moment(new Date()).subtract(28, 'days'), 29)}>
						</Timeline>
					</Bootstrap.ListGroupItem>
				);
				break;
		}
		
		var alerts = (
			<Bootstrap.Panel header={ (<span><i className='fa fa-bell fa-fw'></i><span style={{ paddingLeft: 4 }}>Alerts / Announcements</span></span>) }>
				<Bootstrap.ListGroup fill>
					<Bootstrap.ListGroupItem>
						<i className='fa fa-volume-up fa-fw'></i>
						<span style={{ paddingLeft : 7}}>New water tariff policy</span>
						<span className='pull-right text-muted small'><em>4 minutes ago</em></span>
					</Bootstrap.ListGroupItem>
					<Bootstrap.ListGroupItem>
						<i className='fa fa-cogs fa-fw'></i>
						<span style={{ paddingLeft : 7}}>Job 'Daily pre aggregation MR job' has started</span>
						<span className='pull-right text-muted small'><em>12 minutes ago</em></span>
					</Bootstrap.ListGroupItem>
					<Bootstrap.ListGroupItem>
						<i className='fa fa-warning fa-fw' style={{ color: '#f39c12'}}></i>
						<span style={{ paddingLeft : 7}}>Excessive water consumption detected</span>
                        <span className='pull-right text-muted small'><em>27 minutes ago</em></span>
					</Bootstrap.ListGroupItem>
					<Bootstrap.ListGroupItem>
						<i className='fa fa-exclamation fa-fw' style={{ color: '#c0392b'}}></i>
						<span style={{ paddingLeft : 7}}>Server master-c1-n01 has gone offline</span>
						<span className='pull-right text-muted small'><em>1 hour ago</em></span>
					</Bootstrap.ListGroupItem>
					<Bootstrap.ListGroupItem>
						<span style={{ paddingLeft : 7}}> </span>
						<Link to='/alerts' style={{ paddingLeft : 7, float: 'right'}}>View all alerts</Link>
					</Bootstrap.ListGroupItem>
				</Bootstrap.ListGroup>
			</Bootstrap.Panel>
		);

		var counters = (
			<div className='row'>
				<div className='col-md-2'>
					<div style={{ marginBottom: 20 }}>
						<Counter text={'Counter.Users'} value={198} variance={-2} link='/analytics' />
					</div>
				</div>
				<div className='col-md-2'>
					<div style={{ marginBottom: 20 }}>
						<Counter text={'Counter.Meters'} value={75} variance={5} color='#1abc9c' link='/analytics'/>
					</div>
				</div>
				<div className='col-md-2'>
					<div style={{ marginBottom: 20 }}>
						<Counter text={'Counter.Devices'} value={230} variance={10} color='#27ae60' link='/analytics' />
					</div>
				</div>
				<div className='col-md-2'>
					<div style={{ marginBottom: 20 }}>
						<Counter text='Alerts' value={4} color='#c0392b' />
					</div>
				</div>
				<div className='col-md-2'>
					<div style={{ marginBottom: 20 }}>
						<Counter text='Warning' value={1} color='#f39c12' />
					</div>
				</div>
				<div className='col-md-2'>
					<div style={{ marginBottom: 20 }}>
						<Counter text='Information' value={2} color='#2e8ece' />
					</div>
				</div>
			</div>
		);
		
		var chartPanel = (
			<Bootstrap.Panel header={chartTitle}>
				<Bootstrap.ListGroup fill>
					{chart}	
					<Bootstrap.ListGroupItem className='clearfix'>				
						<div className='pull-left'>
							{chartFilterTags}
						</div>
						<span style={{ paddingLeft : 7}}> </span>
						<Link className='pull-right' to='/analytics' style={{ paddingLeft : 7, paddingTop: 12 }}>View analytics</Link>
					</Bootstrap.ListGroupItem>
				</Bootstrap.ListGroup>
			</Bootstrap.Panel>
		);

		var mapPanel = (
			<Bootstrap.Panel header={mapTitle}>
				<Bootstrap.ListGroup fill>
					{map}
					<Bootstrap.ListGroupItem className='clearfix'>
						<div className='pull-left'>
							{mapFilterTags}
						</div>
						<span style={{ paddingLeft : 7}}> </span>
						<Link className='pull-right' to='/analytics' style={{ paddingLeft : 7, paddingTop: 12 }}>View analytics</Link>
					</Bootstrap.ListGroupItem>
				</Bootstrap.ListGroup>
			</Bootstrap.Panel>
		);

		var layouts = {
			lg : [
			      { i: '0', x: 0, y: 0, w: 6, h: 14},
			      { i: '1', x: 6, y: 0, w: 6, h: 15},
			      { i: '2', x: 0, y: 16, w: 6, h: 7}
	      	]
		};

		var onLayoutChange = function(e) {

		};
		
		var onBreakpointChange = function(e) {

		};
		
		var onResizeStop = function(e) {

		};
		
  		return (
			<div className='container-fluid' style={{ paddingTop: 10 }}>
				<div className='row'>
					<div className='col-md-12'>
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				{counters}
				<div className='row' style={{ overflow : 'hidden' }}>
						<ResponsiveReactGridLayout	className='clearfix' 
													layouts={layouts}
													rowHeight={30}
													onLayoutChange={onLayoutChange.bind(this)}
													onBreakpointChange={onBreakpointChange.bind(this)}
													onResizeStop={onResizeStop.bind(this)}
													breakpoints={{lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0}}
													cols={{lg: 12, md: 10, sm: 6, xs: 4, xxs: 2}}
													autoSize={true}
													draggableHandle='.panel-heading'>
							<div key='0' className='draggable'>
								{chartPanel}
					        </div>
					        <div key='1' className='draggable'>
								{mapPanel}
							</div>
							<div key='2' className='draggable'>
								{alerts}
							</div>
				        </ResponsiveReactGridLayout>
				</div>
            </div>
 		);
  	}
});

Dashboard.icon = 'dashboard';
Dashboard.title = 'Section.Dashboard';

module.exports = Dashboard;
