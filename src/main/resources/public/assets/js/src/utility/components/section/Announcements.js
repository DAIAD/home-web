var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var DropDown = require('../DropDown');
var Select = require('react-controls/select-dropdown');
var Redux = require('react-redux');

var { getCurrentUtilityUsers } = require('../../actions/AnnouncementsActions');
//var AnnouncementsTableSchema = require('../../constants/AnnouncementsTableSchema');

var Announcements = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },
  
//  getDefaultProps: function() {
//    return {
//      currentMembers: null,
//      selectedMembers: null,
//      actions: {
//        addSelectedMembers: function(){},
//        removeSelectedMembers: function(){}       
//      } 
//    };
//  },
  
  componentWillMount : function() {
    
    console.log(this);
    //console.log('will mount ' + this.props.actions.getCurrentUtilityUsers);
    //if(this.props.currentUsers) {
      console.log('getting.. ');
      this.props.getCurrentUtilityUsers();   
    //}
  },
  
  handleCurrentMembersCheckboxChange: function (rowId, propertyName, currentValue){
    console.log('current change');
    //this.props.actions.toggleCandidateGroupMemberToRemove(rowId, currentValue);
  },
  
  handleAddedMembersCheckboxChange: function (rowId, propertyName, currentValue){
    console.log('added change');
    //this.props.actions.toggleCandidateGroupMemberToAdd(rowId, currentValue);
  },  
  
  render: function() {
    var self = this;
  		var _t = this.context.intl.formatMessage;
 
  		var historyTable = {
  				fields: [{
  					name: 'id',
  					title: 'Id',
  					hidden: true
  				}, {
  					name: 'text',
  					title: 'Message'
  				}, {
  					name: 'dispatchedOn',
  					title: 'Dispatched On',
  					type: 'datetime'
  				}, {
  					name: 'status',
  					title: 'Status',
  					className: function(value) {
  						return 'danger';
  					},
  					hidden: true
  				}],
  				rows: [{
  					id: 1,
  					text: 'Save 10% on your monthly bill by installing Amphiro B1',
  					dispatchedOn: new Date((new Date()).getTime() + (3+Math.random()) * 3600000),
  					status: 'Pending',
  					acknowledged: true
  				}, {
  					id: 2,
  					text: 'Welcome to DAIAD!',
  					dispatchedOn: new Date((new Date()).getTime() - (950) * 3600000),
  					status: 'Received',
  					acknowledged: false
  				}],
  				pager: {
  					index: 0,
  					size: 10,
  					count:20
  				}
  			};
        
    var currentRows = [];
    var user1 = {id:1, username:'user1@gmail.com', accountRegisteredOn:new Date((new Date()).getTime() + (2+Math.random()) * 3600000)};
    var user2 = {id:2, username:'user2@gmail.com', accountRegisteredOn:new Date((new Date()).getTime() + (2+Math.random()) * 3600000)};
    var user3 = {id:3, username:'user3@gmail.com', accountRegisteredOn:new Date((new Date()).getTime() + (2+Math.random()) * 3600000)};
    var user4 = {id:4, username:'user4@gmail.com', accountRegisteredOn:new Date((new Date()).getTime() + (2+Math.random()) * 3600000)};
    currentRows.push(user1);
    currentRows.push(user2);
    currentRows.push(user3);
    currentRows.push(user4);
//    var currentRows2 = this.props.actions.getCurrentUtilityUsers;
//    if(currentRows2){
//      console.log('got users?? ' + currentRows2.length);
//    }
    
    var addedRows = [];
    var userAdded = {id:32, username:'user2@hotmail.com', accountRegisteredOn:new Date((new Date()).getTime() + (2+Math.random()) * 3600000)};
    addedRows.push(userAdded);

    var currentUsersFields = {
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
          title: 'Username'
        }, {
          name: 'accountRegisteredOn',
          title: 'Registered On',
          type: 'datetime'
        }, {
          name: 'lastLoginSuccess',
          title: 'Last login on',
          type: 'datetime'
        }, {
          name: 'currentSelected',
          type:'alterable-boolean',
          handler: function(field, row) {
            console.log(this);
          }        
        }],
        rows: currentRows,
        pager: {
          index: 0,
          size: 10,
          count:currentRows.length
        }
    }; 
    
    var addedUsersFields = {
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
        title: 'Username'
      }, {
        name: 'addedSelected',
        type:'alterable-boolean',
        handler: function(field, row) {
          console.log(this);
        }        
      }],
      rows: addedRows,
      pager: {
        index: 0,
        size: 10,
        count:addedRows.length
      }
    };   
    
 
    const usersTitle = (
      <span>
        <i className='fa fa-calendar fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>Users</span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
      </span>
    );
   
    const selectedUsersTitle = (
      <span>
        <i className='fa fa-calendar fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>Selected Users</span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
      </span>
    );   

    const historyTitle = (
      <span>
       <i className='fa fa-calendar fa-fw'></i>
       <span style={{ paddingLeft: 4 }}>History</span>
       <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
      </span>
      </span>
     );

    var filter = (
      <div className='col-md-4'>
        <Bootstrap.Input type='text'
          id='filter' name='filter' ref='filter'
          placeholder='Search participants by email ...'
          //onChange={this.setFilter}
          //value={this.props.admin.filter}
        />
      </div>
    );  
   
//    var groupDropDown = (
//      <div className='col-md-4'>
//        <DropDown
//          title={'Select Group'}
//          //options={utilityOptions}
//          //onSelect={this.props.actions.addUserSelectUtility}
//          disabled={false}
//        />
//      </div>
//    );     
    var groupDropDown = (
      <Select className='select-cluster-group'
        value={''}
        //onChange={(val) => this._setPopulation(clusterKey, val)}
       >
        <optgroup label={ 'All groups'}>
          <option value="" key="">{'Everyone'}</option>
        </optgroup>
      </Select>
    );
    var usersTable = (
      <div>
        <Table data={currentUsersFields}></Table>
      </div>
    );  
   
    var selectedUsersTable = (
      <div>
        <Table data={addedUsersFields}></Table>
      </div>
    );

    return (
      <div className="container-fluid" style={{ paddingTop: 10 }}>
        <div className="row">
         <div className="col-md-12">
          <Breadcrumb routes={this.props.routes}/>
         </div>
        </div>
        <div className="row">
          <div className='col-md-5 equal-height-col'>
            <Bootstrap.Panel header={usersTitle}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>	
                  {groupDropDown}
                  {filter}    
                  {usersTable}    
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel> 
          </div>
         
          <div className='col-md-2 equal-height-col' >
            <div className='div-centered'  style={{marginTop : 120}}>
              <div>
                <Bootstrap.Button onClick={this.props.actions.addSelectedGroupMembers}>
                  {'>>>'}
                </Bootstrap.Button>
              </div>
              <br></br>
              <div>
                <div>
                <Bootstrap.Button onClick={this.props.actions.getCurrentUtilityUsers} >
                  {'<<<'}
                </Bootstrap.Button>
                </div>
              </div>
            </div>
          </div>         
         
          <div className='col-md-5 equal-height-col'>
            <Bootstrap.Panel header={selectedUsersTitle}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>	       
                  {selectedUsersTable}  
                  <div>
                    <Bootstrap.Button >
                      {'Broadcast Announcement'}
                    </Bootstrap.Button>
                  </div>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel> 
          </div>
        </div>
        <div>
        
        
          <div className="row">
            <div className="col-md-12">
              <Bootstrap.Panel header={historyTitle}>
               <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>	
                 <Table data={historyTable}></Table>
                </Bootstrap.ListGroupItem>
               </Bootstrap.ListGroup>
              </Bootstrap.Panel>
            </div> 
          </div> 
        </div>
     </div>
     );
  }
});

function mapStateToProps(state) {
  console.log('mapStateToProps currentUsers' + state.announcements.currentUsers);
  return {
      currentUsers: state.announcements.currentUsers,
      addedUsers: state.announcements.addedUsers
  };
}

function mapDispatchToProps(dispatch) {
  console.log('mapDispatchToProps');
  return {
    //actions : bindActionCreators(Object.assign({}, {getCurrentUtilityUsers}) , dispatch),
    //getCurrentUtilityUsers: bindActionCreators(AnnouncementsActions.getCurrentUtilityUsers, dispatch)
//    getCurrentUtilityUsers: function (){
//      dispatch(AnnouncementsActions.getCurrentUtilityUsers());
//    },
    getCurrentUtilityUsers : bindActionCreators(AnnouncementsActions.getCurrentUtilityUsers, dispatch)
  };
}


Announcements.icon = 'wechat';
Announcements.title = 'Section.ManageAlerts.Announcements';

module.exports = Announcements;
