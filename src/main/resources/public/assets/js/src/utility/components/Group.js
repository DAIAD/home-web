var React = require('react');
var Bootstrap = require('react-bootstrap');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');
var Table = require('./Table');
var Chart = require('./Chart');

var UpsertFavouriteForm = require('./section/demographics/UpsertFavouriteForm');


var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var GroupActions = require('../actions/GroupActions');
var GroupTablesSchema = require('../constants/GroupTablesSchema');


var Group = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	componentWillMount : function() {
    this.props.showGroup(this.props.params.id);
	},
	
	componentWillUnmount : function() {
    this.props.resetGroup();
  },
	
	compareGroupMembers : function (a, b){
    return a.user.localeCompare(b.user);
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
  

	render: function() {
	  
	  var self = this;
	  var _t = this.context.intl.formatMessage;
	  
	  var currentMembersFields = GroupTablesSchema.Members.fields;
	  
	  var currentMembers = null;
	  if (this.props.currentMembers) {
  	  currentMembers = {
          fields : currentMembersFields,
          rows : this.membersObjectToArray(Object.assign({}, this.props.currentMembers)).sort(this.compareGroupMembers),
          pager : {
            index : GroupTablesSchema.Members.pager.index,
            size : GroupTablesSchema.Members.pager.size,
            count : Math.ceil(this.membersObjectToArray(this.props.currentMembers).length / GroupTablesSchema.Members.pager.size)
          }
      };
  	  
  	  currentMembers.fields.forEach(function(field){
        if(field.hasOwnProperty('name') && field.name === 'bookmark'){
          field.handler = function (){
            self.props.showFavouriteAccountForm(this.props.row.id);
          };
        }
      });
	  }
	  
	  
	  
		var chartData = {
			    series: [{
			        legend: 'Average',
			        xAxis: 'date',
			        yAxis: 'volume',
			        data: [{
			            id: 1,
			            volume: 25,
			            date: new Date(2016, 1, 1)
			        }, {
			            id: 1,
			            volume: 70,
			            date: new Date(2016, 1, 2)
			        }, {
			            id: 1,
			            volume: 75,
			            date: new Date(2016, 1, 3)
			        }, {
			            id: 1,
			            volume: 62,
			            date: new Date(2016, 1, 4)
			        }, {
			            id: 1,
			            volume: 53,
			            date: new Date(2016, 1, 5)
			        }, {
			            id: 1,
			            volume: 82,
			            date: new Date(2016, 1, 6)
			        }, {
			            id: 1,
			            volume: 41,
			            date: new Date(2016, 1, 7)
			        }, {
			            id: 1,
			            volume: 45,
			            date: new Date(2016, 1, 8)
			        }, {
			            id: 1,
			            volume: 52,
			            date: new Date(2016, 1, 9)
			        }]
			    }, {
			        legend: 'User 1',
			        xAxis: 'date',
			        yAxis: 'volume',
			        data: [{
			            id: 1,
			            volume: 2,
			            date: new Date(2016, 1, 1)
			        }, {
			            id: 1,
			            volume: 4,
			            date: new Date(2016, 1, 2) 
			        }, {
			            id: 1,
			            volume: 7,
			            date: new Date(2016, 1, 3)
			        }, {
			            id: 1,
			            volume: 8,
			            date: new Date(2016, 1, 4)
			        }, {
			            id: 1,
			            volume: 0,
			            date: new Date(2016, 1, 5)
			        }, {
			            id: 1,
			            volume: 0,
			            date: new Date(2016, 1, 6)
			        }, {
			            id: 1,
			            volume: 1,
			            date: new Date(2016, 1, 7)
			        }, {
			            id: 1,
			            volume: 2,
			            date: new Date(2016, 1, 8)
			        }, {
			            id: 1,
			            volume: 4,
			            date: new Date(2016, 1, 9)
			        }]
			    }]
			};
	  		
	        var chartOptions = {
	            tooltip: {
	                show: true
	            }
	        };
	    
			var groupTitle = null;
			if (this.props.groupInfo) {
  			groupTitle = (
  					<span>
  						<i className='fa fa-group fa-fw'></i>
  						<span style={{ paddingLeft: 4 }}>{this.props.groupInfo.name ? this.props.groupInfo.name : ''}</span>
  						<span style={{float: 'right',  marginTop: -5 }}>
  							<Bootstrap.SplitButton title={_t({id : 'Buttons.Actions'})} id='profile-actions'>
  								<Bootstrap.MenuItem eventKey='1'>
  									<i className='fa fa-envelope-o fa-fw'></i>
  								<span style={{ paddingLeft: 4 }}>Send Message</span>
  							</Bootstrap.MenuItem>
  							<Bootstrap.MenuItem eventKey='1' onSelect={this.props.showFavouriteGroupForm}>
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
			}
			const consumptionTitle = (
					<span>
						<i className='fa fa-bar-chart fa-fw'></i>
						<span style={{ paddingLeft: 4 }}>Consumption</span>
					</span>
				);

			const memberTitle = (
					<span>
						<i className='fa fa-user fa-fw'></i>
						<span style={{ paddingLeft: 4 }}>{_t({id : 'Group.Members'})}</span>
					</span>
				);
			
			if (this.props.application === 'favouriteGroupForm'){
			  return (
			      <UpsertFavouriteForm
			      type = 'GROUP'
            itemId = {this.props.params.id}
            cancelAction = {this.props.hideFavouriteGroupForm}
          />
			  );
			} else if (this.props.application === 'favouriteAccountForm'){
        return (
            <UpsertFavouriteForm
            type = 'ACCOUNT'
            itemId = {this.props.accountId}
            cancelAction = {this.props.hideFavouriteAccountForm}
          />
        );
      } else  if (this.props.groupInfo && this.props.currentMembers){
			  
    		return (
    		  
    		  <div className='container-fluid' style={{ paddingTop: 10 }}>
            <div className='row'>
              <div className='col-md-4'>
                <Bootstrap.Panel header={groupTitle}>
                  <Bootstrap.ListGroup fill>
                    <Bootstrap.ListGroupItem>
                      <div className='row'>
                        <table className='table table-profile'>
                          <tbody>
                            <tr>
                              <td>Name</td>
                              <td>{this.props.groupInfo.name ? this.props.groupInfo.name : ''}</td>
                            </tr>
                            <tr>
                              <td>Description</td>
                              <td>{this.props.groupInfo.name ? this.props.groupInfo.name : ''}</td>
                            </tr>
                            <tr>
                              <td>Created on</td>
                              <td><FormattedDate value={this.props.groupInfo.createdOn ? this.props.groupInfo.createdOn : new Date()} day='numeric' month='long' year='numeric' /></td>
                            </tr>
                            <tr>
                              <td>Country</td>
                              <td>{this.props.groupInfo.country ? this.props.groupInfo.country : ''}</td>
                            </tr>
                            <tr>
                              <td>Size</td>
                              <td>{this.props.groupInfo.size ? this.props.groupInfo.size : 0}</td>
                            </tr> 
                          </tbody>
                        </table>
                      </div>
                    </Bootstrap.ListGroupItem>
                  </Bootstrap.ListGroup>
                </Bootstrap.Panel>
              </div>
              <div className='col-md-8'>
                <Bootstrap.Panel header={memberTitle}>
                  <Bootstrap.ListGroup fill>
                    <Bootstrap.ListGroupItem>
                        <Bootstrap.Input  type="text" 
                                  placeholder='Search for user ...' 
                                  buttonAfter={<Bootstrap.Button><i className='fa fa-plus fa-fw'></i></Bootstrap.Button>} 
                        />
                      <Table data={currentMembers}></Table>
                    </Bootstrap.ListGroupItem>
                  </Bootstrap.ListGroup>
                </Bootstrap.Panel>
              </div>
            </div>
            <div className='row'>
              <div className='col-md-12'>
                <Bootstrap.Panel header={consumptionTitle}>
                  <Bootstrap.ListGroup fill>
                    <Bootstrap.ListGroupItem>
                      <Chart  style={{ width: '100%', height: 400 }} 
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
      	);
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
    isLoading : state.group.isLoading,
    groupInfo : state.group.groupInfo,
    currentMembers : state.group.currentMembers,
    application : state.group.application,
    accountId : state.group.accountId
  };
}

function mapDispatchToProps(dispatch) {
  return {
    showGroup: bindActionCreators(GroupActions.showGroup, dispatch),
    
    showFavouriteGroupForm : bindActionCreators(GroupActions.showFavouriteGroupForm, dispatch),
    hideFavouriteGroupForm : bindActionCreators(GroupActions.hideFavouriteGroupForm, dispatch),
    resetGroup : bindActionCreators(GroupActions.resetDemograhpics, dispatch),
    
    showFavouriteAccountForm : bindActionCreators(GroupActions.showFavouriteAccountForm, dispatch),
    hideFavouriteAccountForm : bindActionCreators(GroupActions.hideFavouriteAccountForm, dispatch)
    
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Group);


