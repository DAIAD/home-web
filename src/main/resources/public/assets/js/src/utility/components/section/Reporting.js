var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var Chart = require('../Chart');

var { getActivity, setFilter, getSessions, getMeters, resetUserData, exportUserData } = require('../../actions/AdminActions');

var Reporting = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
  componentWillMount : function() {
    if(this.props.admin.activity === null) {
      this.props.actions.getActivity();
    }
  },
  
  setFilter: function(e) {
    this.props.actions.setFilter(this.refs.filter.getValue());
  },
  
  clearFilter: function(e) {
    this.props.actions.setFilter('');
  },
  
  resetUserData: function(e) {
    this.props.actions.resetUserData();
  },
  
  refreshParticipants: function(e) {
    this.props.actions.getActivity();
  },
  
  onPageIndexChange: function(index) {
    
  },
  
  render: function() {
    var self = this;
    
    const groupTitle = (
      <span>
        <i className='fa fa-group fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Participants</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
          <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={this.refreshParticipants}>
            <i className='fa fa-refresh fa-fw'></i>
          </Bootstrap.Button>
        </span>
      </span>
    );
    
    var closeSessionChartButton = null;
    if(this.props.admin.user.name) {
      closeSessionChartButton = (
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
          <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={this.resetUserData}>
            <i className='fa fa-rotate-left fa-fw'></i>
          </Bootstrap.Button>
        </span>
      );
    }
    const chartTitle = (
      <span>
        <i className='fa fa-bar-chart fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>{ (this.props.admin.user.name || 'Activity') }</span>
        {closeSessionChartButton}
      </span>
    );
    
    var rows = [];
    var total = 0, registered = 0, login = 0, paired = 0, uploaded = 0, assigned = 0;

    if(this.props.admin.activity!==null) {
      var records = this.props.admin.activity;
      for(var i=0, count=records.length; i<count; i++) {
        total++;
        if(records[i].accountRegisteredOn){
          registered++;
        } 
        if(records[i].lastLoginSuccess){
          login++;
        }  
        if(records[i].leastAmphiroRegistration) {
          paired++;
        }
        if(records[i].leastMeterRegistration) {
          assigned++;
        }
        if(records[i].lastDataUploadSuccess) {
          uploaded++;
        }
        if((!this.props.admin.filter) || (records[i].username.indexOf(this.props.admin.filter) !== -1)) {
          rows.push({
            id: records[i].id,
            key: records[i].key,
            username: records[i].username || '',
            accountRegisteredOn: (records[i].accountRegisteredOn ? new Date(records[i].accountRegisteredOn) : null),
            numberOfAmphiroDevices: records[i].numberOfAmphiroDevices,
            numberOfMeters: records[i].numberOfMeters,
            leastAmphiroRegistration: (records[i].leastAmphiroRegistration ? new Date(records[i].leastAmphiroRegistration) : null),
            leastMeterRegistration: (records[i].leastMeterRegistration ? new Date(records[i].leastMeterRegistration) : null),
            lastLoginSuccess: (records[i].lastLoginSuccess ? new Date(records[i].lastLoginSuccess) : null)
          });
        }
      }
    }
    var model = {
        table: null,
        chart: {
          options: null,
          data: null,
          type: 'bar'
        }
    };
    
    var series = [], data = [];
    if(!this.props.admin.isLoading) {
      if(this.props.admin.user.devices) {
        model.chart.type = 'line';
        model.chart.options = {
          tooltip: {
              show: true
          },
          dataZoom: {
            show: true,
            format: 'day-hour'
          }
        };

        
        var devices = this.props.admin.user.devices;
          
        for(var d=0; d<devices.length; d++) {
          var device = devices[d];
          data = [];
          
          for(var s=0; s < device.sessions.length; s++) {
            data.push({
              volume: device.sessions[s].volume,
              date: new Date(device.sessions[s].timestamp)
            });
          }
          
          series.push({
            legend: device.name || device.deviceKey,
            xAxis: 'date',
            yAxis: 'volume',
            data: data,
            yAxisName: 'Volume (lt)'
          });
        }
        
        model.chart.data = {
          series: series
        };  
      } else if(this.props.admin.user.meters) {
        model.chart.type = 'bar';
        model.chart.options = {
            tooltip: {
                show: true
            },
            dataZoom: {
              show: true,
              format: 'day'
            }
        };
        
        var meters = this.props.admin.user.meters;
          
        for(var m=0; m<meters.length; m++) {
          var meter = meters[m];
          data = [];
          
          for(var v=0; v < meter.values.length; v++) {
            data.push({
              volume: meter.values[v].difference,
              date: new Date(meter.values[v].timestamp)
            });
          }
  
          series.push({
            legend: meter.serial || meter.deviceKey,
            xAxis: 'date',
            yAxis: 'volume',
            data: data,
            yAxisName: 'Volume (lt)'
          });
        }
        
        model.chart.data = {
          series: series
        }; 
        
      } else {
        model.chart.type = 'bar';
  
        var dataPoints = [];
        
        model.chart.options = {
          tooltip: {
              show: true
          },
          dataZoom: {
            show: false
          },
          itemStyle: {
            normal: {
              label : {
                show: true, 
                position: 'top',
                formatter: function (params) {
                  return params.value;
                },
                textStyle: {
                  color: '#565656',
                  fontWeight: 'bold'
                }
              }
            }
          }
        };
  
        dataPoints.push({
          value: total,
          label: 'Total Participants'
        });
        dataPoints.push({
          value: registered,
          label: 'Registered'
        });
        dataPoints.push({
          value: login,
          label: 'Login'
        });
        dataPoints.push({
          value: paired,
          label: 'Paired'
        });
        dataPoints.push({
          value: assigned,
          label: 'Meters'
        });
        dataPoints.push({
          value: uploaded,
          label: 'Uploaded'
        });
        
        model.chart.data = {
            series: [{
                legend: 'Trial Activity',
                xAxis: 'label',
                yAxis: 'value',
                data: dataPoints,
                yAxisName: 'Count'
            }]
        };     
      }
    }

    model.table = {
        fields: [{
          name: 'id',
          title: 'id',
          hidden: true
        }, {
          name: 'key',
          title: 'key',
          hidden: true
        }, {
          name: 'username',
          title: 'Username',
          link: function(row) {
            if(row.key) {
              return '/user/{key}';
            }
            return null;
          }
        }, {
          name: 'accountRegisteredOn',
          title: 'Registered On',
          type: 'datetime'
        }, {
          name: 'numberOfAmphiroDevices',
          title: '# of Amphiro'
        }, {
          name: 'leastAmphiroRegistration',
          title: 'Amphiro registered on',
          type: 'datetime'
        }, {
          name: 'lastLoginSuccess',
          title: 'Last login on',
          type: 'datetime'
        }, {
          name: 'meter',
          type:'action',
          image: '/assets/images/utility/meter.svg',
          handler: function(e) {
            if((this.props.row.key) && (this.props.row.numberOfMeters > 0)) {
              self.props.actions.getMeters(this.props.row.key, this.props.row.username);
            }
          },
          visible: function(row) { 
            return ((row.key) && (row.numberOfMeters > 0));
          }
        }, {
          name: 'session',
          type:'action',
          image: '/assets/images/utility/amphiro.svg',
          handler: function(e) {
            if((this.props.row.key) && (this.props.row.numberOfAmphiroDevices > 0)) {
              self.props.actions.getSessions(this.props.row.key, this.props.row.username);
            }
          },
          visible: function(row) { 
            return ((row.key) && (row.numberOfAmphiroDevices > 0));
          }
        }, {
          name: 'export',
          type:'action',
          icon: 'cloud-download fa-2x',
          color: '#2D3580',
          handler: function(e) {
            if((this.props.row.key) && (this.props.row.numberOfAmphiroDevices > 0)) {
              self.props.actions.exportUserData(this.props.row.key, this.props.row.username);
            }
          },
          visible: function(row) { 
            return ((row.key) && (row.numberOfAmphiroDevices > 0));
          }
        }],
        rows: rows,
        pager: {
          index: 0,
          size: 10,
          count:rows.length
        }
    };
    
    var table = null, filter = null, chart = null;
    if(this.props.data !== null) {
      filter = (
        <div className='row'>
          <div className='col-md-4'>
            <Bootstrap.Input type='text' 
                             id='filter' name='filter' ref='filter'
                             placeholder='Search participants by email ...' 
                             onChange={this.setFilter}
                             value={this.props.admin.filter}
                             buttonAfter={
                              <Bootstrap.Button onClick={this.clearFilter}><i className='fa fa-trash fa-fw'></i></Bootstrap.Button>
                            } 
            />
          </div>
        </div>);
          
      table = (
        <div className='row'>
          <div className="col-md-12">
            <Bootstrap.Panel header={groupTitle}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <Table data={model.table} onPageIndexChange={this.onPageIndexChange}></Table>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>);
      
      chart = (
        <div className='row'>
          <div className="col-md-12">
            <Bootstrap.Panel header={chartTitle}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <Chart  style={{ width: '100%', height: 400 }} 
                          elementClassName='mixin'
                          prefix='chart'
                          type={model.chart.type}
                          options={model.chart.options}
                          data={model.chart.data}/>
            		</Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>
      );
    }
    
    var _t = this.context.intl.formatMessage;
    
		return (
  		<div className="container-fluid" style={{ paddingTop: 10 }}>
  		  <div className="row">
  				<div className="col-md-12">
  					<Breadcrumb routes={this.props.routes}/>
  				</div>
  			</div>
  			{filter}
        {table}
        {chart}
  		</div>);
  	}
});

Reporting.icon = 'database';
Reporting.title = 'Section.Reporting';

function mapStateToProps(state) {
  return {
      admin: state.admin,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { getActivity, setFilter, getSessions, getMeters, resetUserData, exportUserData }) , dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Reporting);
