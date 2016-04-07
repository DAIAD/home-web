var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var Chart = require('../Chart');

var { getActivity, setFilter, getSessions, resetSessions } = require('../../actions/AdminActions');

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
  
  resetSessions: function(e) {
    this.props.actions.resetSessions();
  },
  
  onPageIndexChange: function(index) {
    
  },
  
  render: function() {
    var self = this;
    
    const groupTitle = (
      <span>
        <i className='fa fa-group fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Users</span>
      </span>
    );
    
    var closeSessionChartButton = null;
    if(this.props.admin.user.name) {
      closeSessionChartButton = (
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
          <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={this.resetSessions}>
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
    var total = 0, registered = 0, login = 0, paired = 0, uploaded = 0;

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
        if(records[i].leastDeviceRegistration) {
          paired++;
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
            numberOfDevices: records[i].numberOfDevices,
            leastDeviceRegistration: (records[i].leastDeviceRegistration ? new Date(records[i].leastDeviceRegistration) : null),
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
    
    if(this.props.admin.user.name) {
      model.chart.type = 'line';
      model.chart.options = {
          tooltip: {
              show: true
          },
          dataZoom: {
            show: true
          }
      };
      
      var series = [], devices = this.props.admin.user.devices;
        
      for(var d=0; d<devices.length; d++) {
        var device = devices[d], data = [];
        
        for(var s=0; s < device.sessions.length; s++) {
          data.push({
            volume: device.sessions[s].volume,
            date: new Date(device.sessions[s].timestamp)
          });
        }
        
        series.push({
          legend: device.deviceKey,
          xAxis: 'date',
          yAxis: 'volume',
          data: data
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
        value: uploaded,
        label: 'Uploaded'
      });
      
      model.chart.data = {
          series: [{
              legend: 'Trial Activity',
              xAxis: 'label',
              yAxis: 'value',
              data: dataPoints
          }]
      };     
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
          name: 'numberOfDevices',
          title: '# of Amphiro'
        }, {
          name: 'leastDeviceRegistration',
          title: 'Amphiro registered on',
          type: 'datetime'
        }, {
          name: 'lastLoginSuccess',
          title: 'Last login on',
          type: 'datetime'
        }, {
          name: 'view',
          type:'action',
          icon: 'search',
          handler: function(e) {
            if((this.props.row.key) && (this.props.row.numberOfDevices > 0)) {
              self.props.actions.getSessions(this.props.row.key, this.props.row.username);
            }
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
                             placeholder='Search users ...' 
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
    actions : bindActionCreators(Object.assign({}, { getActivity, setFilter, getSessions, resetSessions }) , dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Reporting);
