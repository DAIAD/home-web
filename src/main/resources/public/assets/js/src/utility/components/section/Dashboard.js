var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../Breadcrumb');
var Counter = require('../Counter');
var Message = require('../Message');
var Chart = require('../Chart');
var LeafletMap = require('../LeafletMap');
var Select = require('react-select');
var DateRangePicker = require('react-bootstrap-daterangepicker');
var FilterTag = require('../chart/dimension/FilterTag');
var Timeline = require('../Timeline');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');

var WidthProvider = require('react-grid-layout').WidthProvider;
var ResponsiveReactGridLayout = require('react-grid-layout').Responsive;

var { getTimeline, getFeatures } = require('../../actions/DashboardActions');

ResponsiveReactGridLayout = WidthProvider(ResponsiveReactGridLayout);

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

var _getTimelineValues = function(timeline) {
  if(timeline) {
    return timeline.getTimestamps();
  } 
  return [];
};

var _getTimelineLabels = function(timeline) {
  if(timeline) {
    return timeline.getTimestamps().map(function(timestamp) {
      return (
        <FormattedTime  value={new Date(timestamp)} 
                        day='numeric' 
                        month='numeric' 
                        year='numeric'/>
      );      
    });
  } 
  return [];
};

var _onChangeTimeline = function(value, label, index) {
  this.props.actions.getFeatures(value);
};

var Dashboard = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

	componentDidMount : function() {
	  var utility = this.props.profile.utility;
  
	  this.props.actions.getTimeline(utility.key, utility.name, utility.timezone);
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
    
  	render: function() { 		
        var chartOptions = {
            tooltip: {
                show: true
            }
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
					<span style={{ paddingLeft: 4 }}>Last Week Daily Consumption</span>
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
				<span style={{ paddingLeft: 4 }}>Last Week Daily Consumption Choropleth</span>
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
			    <Bootstrap.ListGroup fill>
  					<Bootstrap.ListGroupItem>
    	        <LeafletMap style={{ width: '100%', height: 600}} 
                          elementClassName='mixin'
                          prefix='map'
                          center={[38.36, -0.479]} 
                          zoom={13}
                          mode={LeafletMap.MODE_CHOROPLETH}
                          data={ this.props.map.features }
                          colors={['#2166ac', '#67a9cf', '#d1e5f0', '#f7f7f7', '#fddbc7', '#ef8a62', '#b2182b']}
                          urls={['/assets/data/meters.geojson']} 
    	        />
  					</Bootstrap.ListGroupItem>
  		      <Bootstrap.ListGroupItem>
    	        <Timeline   onChange={_onChangeTimeline.bind(this)} 
    	                    labels={ _getTimelineLabels(this.props.map.timeline) }
    	                    values={ _getTimelineValues(this.props.map.timeline) }
    	                    defaultIndex={0}
    	                    speed={1000}
    	                    animate={false}>
    	        </Timeline>
    	      </Bootstrap.ListGroupItem>
            <Bootstrap.ListGroupItem className='clearfix'>
              <div className='pull-left'>
                {mapFilterTags}
              </div>
              <span style={{ paddingLeft : 7}}> </span>
              <Link className='pull-right' to='/analytics' style={{ paddingLeft : 7, paddingTop: 12 }}>View analytics</Link>
            </Bootstrap.ListGroupItem>
  	      </Bootstrap.ListGroup>
				);
				break;
		}
		
        
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
				{map}
			</Bootstrap.Panel>
		);

		var layouts = {
			lg : [
			      { i: '0', x: 0, y: 0, w: 12, h: 14, minH: 14, maxH: 14},
			      { i: '1', x: 0, y: 12, w: 12, h: 20, minH: 20, maxH: 20}
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
					                verticalCompact={true}
					                isResizable={false}
													draggableHandle='.panel-heading'>
						<div key='0' className='draggable'>
							{chartPanel}
		        </div>
		        <div key='1' className='draggable'>
							{mapPanel}
						</div>
	        </ResponsiveReactGridLayout>
				</div>
            </div>
 		);
  	}
});

Dashboard.icon = 'dashboard';
Dashboard.title = 'Section.Dashboard';

function mapStateToProps(state) {
  return {
      map: state.dashboard.map,
      profile: state.session.profile,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { getTimeline, getFeatures }) , dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Dashboard);
