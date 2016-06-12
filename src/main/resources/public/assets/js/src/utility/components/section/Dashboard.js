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

var { getTimeline, getFeatures, getCounters, getChart } = require('../../actions/DashboardActions');

ResponsiveReactGridLayout = WidthProvider(ResponsiveReactGridLayout);

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
  this.props.actions.getFeatures(index, value);
};

var Dashboard = React.createClass({
  _disabledActionHandler: function(e) {
    e.stopPropagation();
    e.preventDefault();
  },
  
	contextTypes: {
	    intl: React.PropTypes.object
	},

	componentDidMount : function() {
	  var utility = this.props.profile.utility;
  
	  if(!this.props.map.timeline) {
	    this.props.actions.getTimeline(utility.key, utility.name, utility.timezone);
	  }
	  if(!this.props.chart.series) {
	    this.props.actions.getChart(utility.key, utility.name, utility.timezone);
	  }
	  this.props.actions.getCounters();
  },

  render: function() { 		
    var chartData = {
      series: []
    };

    if(this.props.chart.series) {
      if(this.props.chart.series.meters) {
        chartData.series.push({
          legend: 'Meter',
          xAxis: 'date',
          yAxis: 'volume',
          data: this.props.chart.series.meters.data
        });
      }
  
      if(this.props.chart.series.devices) {
        chartData.series.push({
          legend: 'Amphiro B1',
          xAxis: 'date',
          yAxis: 'volume',
          data: this.props.chart.series.devices.data
        });
      }
    }

    var chartOptions = {
      tooltip: {
        show: true
      },
      dataZoom : {
        format: 'day'
      }
    };
        
		var chartTitle = (
			<span>
				<i className='fa fa-bar-chart fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Last 2 Week Consumption</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' disabled >
						<i className='fa fa-database fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' disabled >
						<i className='fa fa-map fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' disabled >
						<i className='fa fa-group fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' disabled >
						<i className='fa fa-calendar fa-fw'></i>
					</Bootstrap.Button>
				</span>
			</span>
		);		

		var mapTitle = (
			<span>
				<i className='fa fa-map fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Last 2 Week Consumption</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' disabled >
						<i className='fa fa-database fa-fw'></i>
					</Bootstrap.Button>
				</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
          <Bootstrap.Button bsStyle='default' className='btn-circle' disabled >
            <i className='fa fa-map fa-fw'></i>
          </Bootstrap.Button>
        </span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' disabled >
						<i className='fa fa-group fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' disabled >
						<i className='fa fa-calendar fa-fw'></i>
					</Bootstrap.Button>
				</span>
			</span>
		);
		
    var intervalLabel ='';
    if(this.props.interval) {
    	var start = this.props.interval[0].format('DD/MM/YYYY');
    	var end = this.props.interval[1].format('DD/MM/YYYY');
    	intervalLabel = start + ' - ' + end;
    	if (start === end) {
    		intervalLabel = start;
    	}
    }    	

		var chart = null, chartFilterTags = [], map, mapFilterTags = [];

  	chartFilterTags.push( 
			<FilterTag key='time' text={intervalLabel} icon='calendar' />
  	);
  	chartFilterTags.push( 
			<FilterTag key='source' text='Meter, Amphiro B1' icon='database' />
  	);

  	if(chartData.series.length > 0) {
  		chart = (
  			<Bootstrap.ListGroupItem>
  				<Chart 	style={{ width: '100%', height: 400 }} 
  						elementClassName='mixin'
  						prefix='chart'
  						options={chartOptions}
  						data={chartData}/>
  			</Bootstrap.ListGroupItem>
  		);
  	}

		mapFilterTags.push( 
			<FilterTag key='time' text={intervalLabel} icon='calendar' />
  	);
		mapFilterTags.push( 
			<FilterTag key='spatial' text='Alicante' icon='map' />
  	);
		mapFilterTags.push( 
			<FilterTag key='source' text='Meter' icon='database' />
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
                      colors={['#2166ac', '#67a9cf', '#d1e5f0', '#fddbc7', '#ef8a62', '#b2182b']}
                      overlays={[
                        { url : '/assets/data/meters.geojson',
                          popupContent : 'serial'
                        }
                      ]}
	                    min={this.props.map.timeline ? this.props.map.timeline.min : 0 }
	                    max={this.props.map.timeline ? this.props.map.timeline.max : 0 }
	        
	        />
				</Bootstrap.ListGroupItem>
	      <Bootstrap.ListGroupItem>
	        <Timeline   onChange={_onChangeTimeline.bind(this)} 
	                    labels={ _getTimelineLabels(this.props.map.timeline) }
	                    values={ _getTimelineValues(this.props.map.timeline) }
	                    defaultIndex={this.props.map.index}
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
		
    var counters = this.props.counters;
    
		var counterComponents = (
			<div className='row'>
				<div className='col-md-4'>
					<div style={{ marginBottom: 20 }}>
						<Counter text={'Counter.Users'} 
						         value={counters ? counters.user.value : null} 
						         variance={counters ? counters.user.difference : null} link='/users' />
					</div>
				</div>
				<div className='col-md-4'>
					<div style={{ marginBottom: 20 }}>
						<Counter text={'Counter.Meters'}
						         value={counters ? counters.meter.value : null}
						         variance={counters ? counters.meter.difference : null} color='#1abc9c' link='/users'/>
					</div>
				</div>
				<div className='col-md-4'>
					<div style={{ marginBottom: 20 }}>
						<Counter text={'Counter.Devices'}
						         value={counters ? counters.amphiro.value : null}
						         variance={counters ? counters.amphiro.difference : null} color='#27ae60' link='/users' />
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
				{counterComponents}
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
      interval: state.dashboard.interval,
      map: state.dashboard.map,
      chart: state.dashboard.chart,
      counters: state.dashboard.statistics.counters,
      profile: state.session.profile,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { getTimeline, getFeatures, getCounters, getChart }) , dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Dashboard);
