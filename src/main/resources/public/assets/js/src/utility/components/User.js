var React = require('react');
var Bootstrap = require('react-bootstrap');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');
var Table = require('./Table');
var Chart = require('./Chart');

var UpsertFavouriteForm = require('./section/demographics/UpsertFavouriteForm');

var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var UserActions = require('../actions/UserActions');
var UserTablesSchema = require('../constants/UserTablesSchema');



var User = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	componentWillMount : function() {
    this.props.showUser(this.props.params.id);
  },
  
  getSimplifiedGroupListInfo : function (groupListInfo){
    
    var simplifiedGroupListInfo = [];
    groupListInfo.forEach(function(group){
      simplifiedGroupListInfo.push({
        id : group.id,
        name : group.name,
        size : group.numberOfMembers,
        createdOn : new Date (group.creationDateMils)
      });
    });
    
    return simplifiedGroupListInfo;
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
	  console.log('--p1--');
	  var currentUserGroups = {
	      fields : UserTablesSchema.Groups.fields,
	      rows : UserTablesSchema.Groups.rows,
	      pager : UserTablesSchema.Groups.pager
	  };
	  
	  
	  if (this.props.groupListInfo) {
	    currentUserGroups.rows = this.getSimplifiedGroupListInfo(this.membersObjectToArray(Object.assign({}, this.props.groupListInfo))).sort(this.compareGroups);
	    currentUserGroups.pager = {
        index : UserTablesSchema.Groups.pager.index,
        size : UserTablesSchema.Groups.pager.size,
        count : Math.ceil(this.props.groupListInfo.length / UserTablesSchema.Groups.pager.size)
      };
	    
	  }
	  console.log('--p2--');
		var chartData = {
			    series: [{
			        legend: 'Amphiro Shower #1',
			        xAxis: 'date',
			        yAxis: 'volume',
			        data: [{
			            id: 1,
			            name: 'Sales',
			            volume: 25,
			            date: new Date(2016, 1, 1),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 70,
			            date: new Date(2016, 1, 2),
			            geometry: { } 
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 75,
			            date: new Date(2016, 1, 3),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 62,
			            date: new Date(2016, 1, 4),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 53,
			            date: new Date(2016, 1, 5),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 27,
			            date: new Date(2016, 1, 6),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 41,
			            date: new Date(2016, 1, 7),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 45,
			            date: new Date(2016, 1, 8),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 13,
			            date: new Date(2016, 1, 9),
			            geometry: { }
			        }]
			    }, {
			        legend: 'Amphiro Shower #2',
			        xAxis: 'date',
			        yAxis: 'volume',
			        data: [{
			            id: 1,
			            name: 'Sales',
			            volume: 46,
			            date: new Date(2016, 1, 1),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 30,
			            date: new Date(2016, 1, 2),
			            geometry: { } 
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 97,
			            date: new Date(2016, 1, 3),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 32,
			            date: new Date(2016, 1, 4),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 23,
			            date: new Date(2016, 1, 5),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 11,
			            date: new Date(2016, 1, 6),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 18,
			            date: new Date(2016, 1, 7),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 11,
			            date: new Date(2016, 1, 8),
			            geometry: { }
			        }, {
			            id: 1,
			            name: 'Sales',
			            volume: 5,
			            date: new Date(2016, 1, 9),
			            geometry: { }
			        }]
			    }]
			};
	  		
	        var chartOptions = {
	            tooltip: {
	                show: true
	            }
	        };

	  		var devices = {
	  				fields: [{
	  					name: 'deviceId',
	  					title: 'Id',
	  					hidden: true
	  				}, {
	  					name: 'type',
	  					title: 'Type'
	  				}, {
	  					name: 'name',
	  					title: 'Description',
	  					link: '/device/{deviceId}'
	  				}, {
	  					name: 'registeredOn',
	  					title: 'Registered On',
	  					type: 'datetime'
	  				}, {
	  					name: 'lastUpdatedOn',
	  					title: 'Last Update On',
	  					type: 'datetime'
	  				}],
	  				rows: [{
	  					deviceId: 1,
	  					userId: 1,
	  					type: 'Amphiro',
	  					name: 'Amphiro #2',
	  					registeredOn: new Date((new Date()).getTime() + Math.random() * 3600000),
	  					lastUpdatedOn: new Date((new Date()).getTime() + Math.random() * 3600000)
	  				}, {
	  					deviceId: 2,
	  					userId: 1,
	  					type: 'Meter',
	  					name: 'I94854C384',
	  					registeredOn: new Date((new Date()).getTime() + Math.random() * 3600000),
	  					lastUpdatedOn: new Date((new Date()).getTime() + Math.random() * 3600000)
	  				}],
	  				pager: {
	  					index: 0,
	  					size: 1,
	  					count:2
	  				}
	  			};
	  		
	  		console.log('--p3--');
	  		
			const profileTitle = (
					<span>
						<i className='fa fa-user fa-fw'></i>
						<span style={{ paddingLeft: 4 }}>Profile</span>
						<span style={{float: 'right',  marginTop: -5 }}>
							<Bootstrap.SplitButton title='Actions' id='profile-actions'>
								<Bootstrap.MenuItem eventKey='1'>
									<i className='fa fa-envelope-o fa-fw'></i>
								<span style={{ paddingLeft: 4 }}>Send Message</span>
							</Bootstrap.MenuItem>
							<Bootstrap.MenuItem eventKey='1'>
								<i className='fa fa-user-plus fa-fw'></i>
								<span style={{ paddingLeft: 4 }}>Add to group</span>
							</Bootstrap.MenuItem>
							<Bootstrap.MenuItem eventKey='1' onSelect={this.props.showFavouriteAccountForm}>
								<i className='fa fa-bookmark-o fa-fw'></i>
								<span style={{ paddingLeft: 4 }}>Add to favourites</span>
							</Bootstrap.MenuItem>
								<Bootstrap.MenuItem divider />
								<Bootstrap.MenuItem eventKey='2'>
									<i className='fa fa-cloud-download fa-fw'></i>
									<span style={{ paddingLeft: 4 }}>Export data</span>
							</Bootstrap.MenuItem>
							</Bootstrap.SplitButton>
					    </span>
					</span>
				);
			
			const consumptionTitle = (
					<span>
						<i className='fa fa-bar-chart fa-fw'></i>
						<span style={{ paddingLeft: 4 }}>Consumption</span>
					</span>
				);

			const groupTitle = (
					<span>
						<i className='fa fa-group fa-fw'></i>
						<span style={{ paddingLeft: 4 }}>Groups</span>
					</span>
				);

			const deviceTitle = (
					<span>
						<i className='fa fa-bluetooth fa-fw'></i>
						<span style={{ paddingLeft: 4 }}>Devices</span>
					</span>
				);
			console.log('--p4--');
			console.log(this.props.isLoading);
			console.log(this.props.application);
			console.log(this.props.userInfo);
		if (!this.props.isLoading && this.props.userInfo){
		  if (this.props.application === 'favouriteAccountForm'){
		    console.log('--p5--');
		    return (
            <UpsertFavouriteForm
              type = 'ACCOUNT'
              itemId = {this.props.params.id}
              actions = {{
                cancelAction : this.props.hideFavouriteAccountForm,
                refreshParentForm : function (){}
              }}
            />
        );
		  } else if(this.props.application === 'default'){
		    console.log('--p6--');
    		return (
      		<div className='container-fluid' style={{ paddingTop: 10 }}>
      			<div className='row'>
      				<div className='col-md-5'>
      					<div className='row'>
      						<div className='col-md-12'>
      							<Bootstrap.Panel header={profileTitle}>
      								<Bootstrap.ListGroup fill>
      									<Bootstrap.ListGroupItem>
      										<div className='row'>
      											<div className='col-md-4'>
      												<div style={{width: '100px', height: '100px',  border: '#3498db solid 3px', borderRadius: '50%', padding: 3 }}>
      													<img src='/assets/images/utility/profile.png' style={{borderRadius: '50%', width: '100%', height: '100%'}} />
      												</div>
      											</div>
      											<div className='col-md-8'>
      												<table className='table table-profile'>
      													<tbody>
      														<tr>
      															<td>First name</td>
      															<td>{this.props.userInfo.firstName}</td>
      														</tr>
      														<tr>
      															<td>Last name</td>
      															<td>{this.props.userInfo.lastName}</td>
      														</tr>
      														<tr>
      															<td>Email</td>
      															<td>{this.props.userInfo.email}</td>
      														</tr>
      														<tr>
                                    <td>Gender</td>
                                    <td>{this.props.userInfo.gender}</td>
                                  </tr>
      														<tr>
      															<td>Registered on</td>
      															<td><FormattedDate value={this.props.userInfo.registeredOn} day='numeric' month='long' year='numeric' /></td>
      														</tr>
        														<tr>
                                    <td>Addrerss</td>
                                    <td>{this.props.userInfo.address}</td>
                                  </tr>
                                    <tr>
                                    <td>City</td>
                                    <td>{this.props.userInfo.city}</td>
                                  </tr>
      														<tr>
      															<td>Country</td>
      															<td>{this.props.userInfo.country}</td>
      														</tr>
      														<tr>
      															<td>Postal code</td>
      															<td>{this.props.userInfo.postalCode}</td>
      														</tr>	
      													</tbody>
      												</table>
      											</div>
      										</div>
      									</Bootstrap.ListGroupItem>
      								</Bootstrap.ListGroup>
      							</Bootstrap.Panel>
      						</div>
      					</div>
      					<div className="row">
      						<div className='col-md-12'>
      							<Bootstrap.Panel header={groupTitle}>
      								<Bootstrap.ListGroup fill>
      									<Bootstrap.ListGroupItem>
      										<Table data={currentUserGroups}></Table>
      									</Bootstrap.ListGroupItem>
      								</Bootstrap.ListGroup>
      							</Bootstrap.Panel>
      						</div>
      					</div>
      					<div className="row">
      						<div className='col-md-12'>
      							<Bootstrap.Panel header={deviceTitle}>
      								<Bootstrap.ListGroup fill>
      									<Bootstrap.ListGroupItem>
      										<Table data={devices}></Table>
      									</Bootstrap.ListGroupItem>
      								</Bootstrap.ListGroup>
      							</Bootstrap.Panel>
      						</div>
      					</div>
      				</div>
      				<div className='col-md-7'>
      					<div className='row'>
      						<div className='col-md-12'>
      							<Bootstrap.Panel header={consumptionTitle}>
      								<Bootstrap.ListGroup fill>
      									<Bootstrap.ListGroupItem>
      										<Chart 	style={{ width: '100%', height: 400 }} 
      												elementClassName='mixin'
      												prefix='chart'
      												options={chartOptions}
      												data={chartData}/>
      									</Bootstrap.ListGroupItem>
      								</Bootstrap.ListGroup>
      							</Bootstrap.Panel>
      						</div>
      					</div>
      				</div>
      			</div>
      		</div>
    		);
		  }
		} else {
		  return (
          <div>
            <img className='preloader' src='/assets/images/utility/preloader-counterclock.png' />
            <img className='preloader-inner' src='/assets/images/utility/preloader-clockwise.png' />
          </div>
        );
		}
	}
});

function mapStateToProps(state) {
  return {
    isLoading : state.user.isLoading,
    userInfo : state.user.userInfo,
    groupListInfo : state.user.groupListInfo,
    application : state.user.application,
    accountId : state.user.accountId
  };
}

function mapDispatchToProps(dispatch) {
  return {
    showUser: bindActionCreators(UserActions.showUser, dispatch),
    showFavouriteAccountForm : bindActionCreators(UserActions.showFavouriteAccountForm, dispatch),
    hideFavouriteAccountForm : bindActionCreators(UserActions.hideFavouriteAccountForm, dispatch),
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(User);