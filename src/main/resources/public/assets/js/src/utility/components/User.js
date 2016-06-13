var React = require('react');
var Bootstrap = require('react-bootstrap');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');
var Table = require('./Table');
var Chart = require('./Chart');
var theme = require('./chart/themes/shine');

var UpsertFavouriteForm = require('./section/demographics/UpsertFavouriteForm');

var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var UserActions = require('../actions/UserActions');
var UserTablesSchema = require('../constants/UserTablesSchema');
 
var _showChart = function(type, key, e) {
  switch(type) {
    case 'METER':
      this.props.getMeters(this.props.user.id);
      break;
    case 'AMPHIRO':
      this.props.getSessions(this.props.user.id, key);
      break;
  }
};

var _clearGroupSeries = function() {
  this.props.clearGroupSeries();
};

var _onAction = function(event, eventKey) {
  console.log(eventKey);
  switch(eventKey) {
    case '2':
      if(this.props.favorite) {
        this.props.removeFavorite(this.props.user.id);
      } else {
        this.props.addFavorite(this.props.user.id);
      }
      break;
    case '3':
      this.props.exportData(this.props.user.id, this.props.user.email);
      break;
  }
};

var User = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	componentWillMount : function() {
    this.props.showUser(this.props.params.id);
  },
  
  getSimplifiedGroups : function (groups){
    
    var simplifiedGroups = [];
    groups.forEach(function(group){
      simplifiedGroups.push({
        id : group.id,
        name : group.name,
        size : group.numberOfMembers,
        createdOn : new Date (group.creationDateMils)
      });
    });
    
    return simplifiedGroups;
  },
  
  membersObjectToArray: function(membersObject){
    var membersArray = [];
    
    for (var id in membersObject) {
      if (membersObject.hasOwnProperty(id)) {
        membersArray.push(membersObject[id]);
      }
    }
    
    return membersArray;
  },
  
  compareGroups : function (a, b){
    return a.name.localeCompare(b.name);
  },

	render: function() {
	  var groupTableConfig = {
      fields : [{
        name : 'id',
        title : 'Table.Group.id',
        hidden : true
      }, {
        name : 'name',
        title : 'Table.Group.name',
        link : '/group/{id}'
      }, {
        name : 'size',
        title : 'Table.Group.size'
      }, {
        name : 'createdOn',
        title : 'Table.Group.createdOn'
      }, {
        name : 'chart',
        type : 'action',
        icon : 'bar-chart-o',
        handler : (function(field, row) {
          var utility = this.props.profile.utility;
          
          this.props.getGroupSeries(row.id, row.name, utility.timezone);
        }).bind(this),
        visible : (function(field, row) {
          return (this.props.data.meters !== null);
        }).bind(this)
      }],
      rows : []
    };
	  
	  if (this.props.groups) {
	    groupTableConfig.rows = this.getSimplifiedGroups(this.membersObjectToArray(Object.assign({}, this.props.groups))).sort(this.compareGroups);
	  }

		const profileTitle = (
			<span>
				<i className='fa fa-user fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Profile</span>
				<span style={{float: 'right',  marginTop: -5 }}>
					<Bootstrap.SplitButton title='Actions' id='profile-actions' onSelect={_onAction.bind(this)}>
						<Bootstrap.MenuItem eventKey='1' disabled>
							<i className='fa fa-envelope-o fa-fw'></i>
						<span style={{ paddingLeft: 4 }}>Send Message</span>
					</Bootstrap.MenuItem>
					<Bootstrap.MenuItem eventKey='2'>
						<i className={ this.props.favorite ? 'fa fa-bookmark-o fa-fw' : 'fa fa-bookmark fa-fw'}></i>
						<span style={{ paddingLeft: 4 }}>{ this.props.favorite ? 'Remove from favourites' : 'Add to favourites' }</span>
					</Bootstrap.MenuItem>
						<Bootstrap.MenuItem divider />
						<Bootstrap.MenuItem eventKey='3'>
							<i className='fa fa-cloud-download fa-fw'></i>
							<span style={{ paddingLeft: 4 }}>Export data</span>
					</Bootstrap.MenuItem>
					</Bootstrap.SplitButton>
			    </span>
			</span>
		);
		
    const deviceTitle = (
      <span>
        <i className='fa fa-database fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Data Sources</span>
      </span>
    );

		const groupTitle = (
			<span>
				<i className='fa fa-group fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Groups</span>
			</span>
		);

		var deviceElements = [];

		if(this.props.meters) {
		  this.props.meters.forEach( m => {
		    deviceElements.push(
	          <Bootstrap.ListGroupItem key={deviceElements.length + 1} className='clearfix'>
	            <div style={{ width: 24, float : 'left', textAlign : 'center', marginLeft: -5 }}>
	              <img src='/assets/images/utility/meter.svg' />
              </div>
	            <div style={{ paddingTop: 2, float : 'left' }}>{m.serial}</div>
	            <div style={{ width: 24, float : 'right' }}>
	              <i  className={'fa fa-bar-chart-o fa-fw table-action'} onClick={_showChart.bind(this, 'METER', m.deviceKey)}>
	              </i>
              </div>
	          </Bootstrap.ListGroupItem>
        );
		    deviceElements.push(
	        <Bootstrap.ListGroupItem key={deviceElements.length + 1}>
	          <table>
	            <tbody>
  	            <tr>
  	              <td style={{ paddingRight: 5 }}><b>Current value</b></td>
  	              <td>{m.volume} lt</td>
                </tr>
                <tr>
                  <td style={{ paddingRight: 5 }}><b>Last update</b></td>
                  <td>
                    <FormattedTime  value={ new Date(m.timestamp) } 
                                    day='numeric' 
                                    month='numeric' 
                                    year='numeric'
                                    hour='numeric' 
                                    minute='numeric' />
                  </td>
                </tr>
              </tbody>
            </table>
	        </Bootstrap.ListGroupItem>
	     );   
		  });
		} 

    if(this.props.devices) {
      this.props.devices.forEach( d => {
        deviceElements.push(
          <Bootstrap.ListGroupItem key={deviceElements.length + 1} className='clearfix'>
            <div style={{ width: 24, float : 'left', textAlign : 'center', marginLeft: -5 }}>
              <img src='/assets/images/utility/amphiro.svg' />
            </div>
            <div style={{ paddingTop: 2, float : 'left' }}>{d.name}</div>
            <div style={{ width: 24, float : 'right' }}>
              <i  className={'fa fa-bar-chart-o fa-fw table-action'} onClick={_showChart.bind(this, 'AMPHIRO', d.deviceKey)}>
              </i>
            </div>
          </Bootstrap.ListGroupItem>
        );
        if((d.sessions) && (d.sessions.length > 0)) {
          var s = d.sessions[0];
          deviceElements.push(
            <Bootstrap.ListGroupItem key={deviceElements.length + 1}>
              <table>
                <tbody>
                  <tr>
                    <td style={{ paddingRight: 5 }}><b>Consumption</b></td>
                    <td>{s.volume} lt</td>
                  </tr>
                  <tr>
                    <td style={{ paddingRight: 5 }}><b>Last update</b></td>
                    <td>
                      <FormattedTime  value={ new Date(s.timestamp) } 
                                      day='numeric' 
                                      month='numeric' 
                                      year='numeric'
                                      hour='numeric' 
                                      minute='numeric' />
                    </td>
                  </tr>
                </tbody>
              </table>
            </Bootstrap.ListGroupItem>
          );   
        }
      });
    }

		var chartTitleText = (
	    <span>
        <span>
          <i className='fa fa-bar-chart fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>Consumption - Last 30 days</span>
        </span>
      </span>
    );
		
		var chartConfig = null, chart = (<span>No meter data found.</span>), data = [];

    if(this.props.data.meters) {     
      chartTitleText = (
        <span>
          <span>
            <i className='fa fa-bar-chart fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>Consumption - Last 30 days</span>
          </span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
            <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={_clearGroupSeries.bind(this)} >
              <i className='fa fa-remove fa-fw' ></i>
            </Bootstrap.Button>
          </span>
        </span>
      );
      
      chartConfig = {
        options: {
          tooltip: {
            show: true
          },
          dataZoom: {
            show: true,
            format: 'day'
          }
        },
        data: {
          series: []
        },
        type: 'line'
      };
      
      var v, meters = this.props.data.meters;

      for(var m=0; m<meters.length; m++) {
        var meter = meters[m];
        data = [];

        for(v=0; v < meter.values.length; v++) {
          data.push({
            volume: meter.values[v].difference,
            date: new Date(meter.values[v].timestamp)
          });
        }

        chartConfig.data.series.push({
          legend: meter.serial || meter.deviceKey,
          xAxis: 'date',
          yAxis: 'volume',
          data: data,
          yAxisName: 'Volume (lt)'
        });
      }
      
      for(var key in this.props.data.groups) {
        var group = this.props.data.groups[key];
        if((group.points) && (group.points.length > 0)) {
          data = [];

          for(v=0; v < group.points.length; v++) {
            data.push({
              volume: group.points[v].volume.AVERAGE,
              date: new Date(group.points[v].timestamp)
            });
          }

          chartConfig.data.series.push({
            legend: group.label + ' (Average)',
            xAxis: 'date',
            yAxis: 'volume',
            data: data,
            yAxisName: 'Volume (lt)'
          });
        }
      }
      
      chart = (
        <Chart  style={{ width: '100%', height: 400 }}
                elementClassName='mixin'
                prefix='chart'
                type={chartConfig.type}
                options={chartConfig.options}
                data={chartConfig.data}
                theme={theme}/>
  		);
    } else if(this.props.data.devices) {
      chartTitleText = (
        <span>
          <span>
            <i className='fa fa-bar-chart fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>Consumption - Last 30 days</span>
          </span>
        </span>
      );
      
      chartConfig = {
        options: {
          tooltip: {
            show: true
          },
          dataZoom: {
            show: true
          }
        },
        data: {
          series: []
        },
        type: 'bar'
      };
      
      var devices = this.props.data.devices, deviceIndex = 0, d;

      for(d = 0; d< devices.length; d++) {
        if(devices[d].deviceKey === this.props.data.deviceKey) {
          deviceIndex = d;
          break;
        }
      }

      var size = devices[deviceIndex].sessions.length, device = devices[deviceIndex], index = 1;
      data = [];

      for(var s=0; s < size; s++) {
        data.push({
          volume: device.sessions[s].volume,
          id:  index
        });
        index++;
      }

      chartConfig.data.series.push({
        legend: device.name || device.deviceKey,
        xAxis: 'id',
        yAxis: 'volume',
        data: data,
        yAxisName: 'Volume (lt)'
      });
      
      chart = (
        <Chart  style={{ width: '100%', height: 400 }}
                elementClassName='mixin'
                prefix='chart'
                type={chartConfig.type}
                options={chartConfig.options}
                data={chartConfig.data}/>
      );
    }

    const chartTitle = (
      <span>
        <i className='fa fa-bar-chart fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>{ chartTitleText }</span>
      </span>
    );

		var deviceListGroup = (
	    <Bootstrap.ListGroup fill>
        {deviceElements.length === 0 ? null : deviceElements}
      </Bootstrap.ListGroup>
    );
		
		if (this.props.user) {
  		return (
    		<div className='container-fluid' style={{ paddingTop: 10 }}>
    		  <div className='row'>
    		    <div className='col-md-7'>
      		    <Bootstrap.Panel header={profileTitle}>
                <Bootstrap.ListGroup fill>
                  <Bootstrap.ListGroupItem>
                    <div className='row'>
                      <div className='col-md-3'>
                        <div style={{width: '100px', height: '100px',  border: '#3498db solid 3px', borderRadius: '50%', padding: 3 }}>
                          <img src='/assets/images/utility/profile.png' style={{borderRadius: '50%', width: '100%', height: '100%'}} />
                        </div>
                      </div>
                      <div className='col-md-9'>
                        <table className='table table-profile'>
                          <tbody>
                            <tr>
                              <td>First name</td>
                              <td>{this.props.user.firstName}</td>
                            </tr>
                            <tr>
                              <td>Last name</td>
                              <td>{this.props.user.lastName}</td>
                            </tr>
                            <tr>
                              <td>Email</td>
                              <td>{this.props.user.email}</td>
                            </tr>
                            <tr>
                              <td>Gender</td>
                              <td><FormattedMessage id={'Gender.' + this.props.user.gender} /></td>
                            </tr>
                            <tr>
                              <td>Registered on</td>
                              <td><FormattedDate value={this.props.user.registeredOn} day='numeric' month='long' year='numeric' /></td>
                            </tr>
                              <tr>
                              <td>Addrerss</td>
                              <td>{this.props.user.address}</td>
                            </tr>
                              <tr>
                              <td>City</td>
                              <td>{this.props.user.city}</td>
                            </tr>
                            <tr>
                              <td>Country</td>
                              <td>{this.props.user.country}</td>
                            </tr>
                            <tr>
                              <td>Postal code</td>
                              <td>{this.props.user.postalCode}</td>
                            </tr> 
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </Bootstrap.ListGroupItem>
                </Bootstrap.ListGroup>
              </Bootstrap.Panel>
    		    </div>
    		    <div className='col-md-5'>
              <Bootstrap.Panel header={groupTitle}>
                <Bootstrap.ListGroup fill>
                  <Bootstrap.ListGroupItem>
                    <Table data={groupTableConfig}></Table>
                  </Bootstrap.ListGroupItem>
                </Bootstrap.ListGroup>
              </Bootstrap.Panel>
    		    </div>
    		  </div>
    		  <div className='row'>
    		    <div className='col-md-3'>
              <Bootstrap.Panel header={deviceTitle}>
                {deviceListGroup}
              </Bootstrap.Panel>
    		    </div>
            <div className='col-md-9'>
              <Bootstrap.Panel header={chartTitleText}>
                <Bootstrap.ListGroup fill>
                  <Bootstrap.ListGroupItem>
                    {chart}
                  </Bootstrap.ListGroupItem>
                </Bootstrap.ListGroup>
              </Bootstrap.Panel>
            </div>
          </div>
    		</div>
  		);
		} else {
		  return null;
		}
	}
});

function mapStateToProps(state) {
  return {
    isLoading : state.user.isLoading,
    favorite : state.user.favorite,
    user : state.user.user,
    meters : state.user.meters,
    devices : state.user.devices,
    data : state.user.data,
    groups : state.user.groups,
    application : state.user.application,
    accountId : state.user.accountId,
    profile: state.session.profile
  };
}

function mapDispatchToProps(dispatch) {
  return {
    showUser: bindActionCreators(UserActions.showUser, dispatch),
    showFavouriteAccountForm : bindActionCreators(UserActions.showFavouriteAccountForm, dispatch),
    hideFavouriteAccountForm : bindActionCreators(UserActions.hideFavouriteAccountForm, dispatch),
    getSessions : bindActionCreators(UserActions.getSessions, dispatch),
    getMeters : bindActionCreators(UserActions.getMeters, dispatch),
    clearGroupSeries : bindActionCreators(UserActions.clearGroupSeries, dispatch),
    getGroupSeries : bindActionCreators(UserActions.getGroupSeries, dispatch),
    exportData : bindActionCreators(UserActions.exportData, dispatch),
    addFavorite : bindActionCreators(UserActions.addFavorite, dispatch),
    removeFavorite : bindActionCreators(UserActions.removeFavorite, dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(User);