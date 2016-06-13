var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../Breadcrumb');
var Chart = require('../Chart');
var moment = require('moment');
var Select = require('react-select');
var UserSearchTextBox = require('../UserSearchTextBox');
var { getUtilityData, getUtilityForecast, getUserData, getUserForecast, setUser } = require('../../actions/ForecastingActions');
var theme = require('../chart/themes/forecast');

var _onUserSelect= function(e) {
  var profile = this.props.profile;
  
  if(e) {
    if(e.value) {
      this.props.actions.getUserData(e.value, e.label, profile.timezone);
      this.props.actions.getUserForecast(e.value, e.label, profile.timezone);
    }
  }
  this.props.actions.setUser(e);
};

var _onChangeChartType = function(e) {
  this.setState({
    chart: {
      type: e.value
    }
  });
};

var Forecasting = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getInitialState() {
		return {
		  chart: {
		    type: 'line'
		  }
		};
	},

	toggleView(view) {
		this.setState({map : !this.state.map});
	},
  	
  componentWillMount : function() {
    var profile = this.props.profile;
    
    
    this.props.actions.setUser(null);

		this.props.actions.getUtilityData(profile.utility.key, profile.utility.name, profile.timezone);
		this.props.actions.getUtilityForecast(profile.utility.key, profile.utility.name, profile.timezone);
	},

	render: function() { 	  	
		var utilityChart = {
	    series: []
		};
    var userChart = {
      series: []
    };

		if(this.props.forecasting.data.utility) {
		  utilityChart.series.push({
		    legend: 'Actual',
		    xAxis: 'date',
		    yAxis: 'volume',
		    data: this.props.forecasting.data.utility.data
		  });
		}
    if(this.props.forecasting.forecast.utility) {
      utilityChart.series.push({
        legend: 'Forecast',
        xAxis: 'date',
        yAxis: 'volume',
        data: this.props.forecasting.forecast.utility.data
      });
    }

    if(this.props.forecasting.user) {
      if(this.props.forecasting.data.user) {
        userChart.series.push({
          legend: this.props.forecasting.data.user.label + ' - Actual',
          xAxis: 'date',
          yAxis: 'volume',
          data: this.props.forecasting.data.user.data
        });
      }
      if(this.props.forecasting.forecast.user) {
        userChart.series.push({
          legend: this.props.forecasting.forecast.user.label + ' - Forecast',
          xAxis: 'date',
          yAxis: 'volume',
          data: this.props.forecasting.forecast.user.data
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
        
		const title = (
			<span>
				<i className={'fa fa-bar-chart fa-fw'}></i>
				<span style={{ paddingLeft: 4 }}>Utility and User Water Consumption Forecasting</span>
			</span>
		);

		var chart1 = (<span>Loading ... </span>), chart2 = null;
		
		if(utilityChart.series.length > 0) {
		  chart1 = (
	      <Chart style={{ width: '100%', height: 450 }} 
               elementClassName='mixin'
               type={this.state.chart.type}
               prefix='chart'
               options={chartOptions}
               data={utilityChart}
	             theme={theme}
	      />
		  );
		}
		
		if(userChart.series.length > 0) {
		  chart2 = (
	      <Bootstrap.ListGroupItem>
          <Chart  style={{ width: '100%', height: 450 }} 
                  elementClassName='mixin'
                  type={this.state.chart.type}
                  prefix='chart'
                  options={chartOptions}
                  data={userChart}
                  theme={theme}
          />
	      </Bootstrap.ListGroupItem>
		  );
		}
		
		var content = (
			<div className='row'>
				<div className='col-lg-12'>
					<Bootstrap.Panel header={title}>
			      <Bootstrap.ListGroup fill>
			        <Bootstrap.ListGroupItem>
			          <div className='row'>
			            <div className='col-md-3'>
			              <UserSearchTextBox name='username' onChange={_onUserSelect.bind(this)}/>
			              <span className='help-block'>Select a single user</span>
		              </div>
		              <div className='col-md-2'>
  		              <Select name='chart-type'
                      value={ 'line' }
                      options={[
                          { value: 'bar', label: 'Bar' },
                          { value: 'line', label: 'Line' },
                          { value: 'area', label: 'Area' }
                      ]}
                      onChange={_onChangeChartType.bind(this)}
  		                clearable={false} 
  		              />
                    <span className='help-block'>Select chart type</span>
                  </div>
	              </div>
			        </Bootstrap.ListGroupItem>
  		        <Bootstrap.ListGroupItem>
  		          {chart1}
  		        </Bootstrap.ListGroupItem>
  		        {chart2}
  		        <Bootstrap.ListGroupItem className='clearfix'>        
  		          <Link className='pull-right' to='/scheduler' style={{ paddingLeft : 7, paddingTop: 12 }}>Job Scheduler</Link>
  		        </Bootstrap.ListGroupItem>
  		      </Bootstrap.ListGroup>
					</Bootstrap.Panel>
				</div>
			</div>
		);

		return (
			<div className='container-fluid' style={{ paddingTop: 10 }}>
				<div className='row'>
					<div className='col-md-12'>
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				{content}
      </div>
 		);
	}
});

Forecasting.icon = 'line-chart';
Forecasting.title = 'Section.Forecasting';

function mapStateToProps(state) {
  return {
      forecasting: state.forecasting,
      profile: state.session.profile,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { getUtilityData, getUtilityForecast,
                                                     getUserData, getUserForecast,
                                                     setUser }) , dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Forecasting);
