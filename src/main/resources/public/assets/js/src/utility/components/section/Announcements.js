var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var DropDown = require('../DropDown');
var Select = require('react-controls/select-dropdown');
var Redux = require('react-redux');


var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');
var AnnouncementsActions = require('../../actions/AnnouncementsActions');

var populatedUsers = [];
var addedRows = [];

var Announcements = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },
  
  componentWillMount : function() {
      this.props.getCurrentUtilityUsers();  
  },
   
  handleCurrentMembersCheckboxChange: function (rowId, propertyName){
    console.log('current change ' + rowId);
    this.props.setUserSelected(this.props.accounts, rowId);
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
         
    var currentUsersFields1 = {
        fields: [{
          name: 'accountId',
          title: 'id',
          hidden: true
        }, {
          name: 'lastName',
          title: 'Last Name'
        }, {
          name: 'username',
          title: 'Username'
        }, {
          name: 'selected',
          type:'alterable-boolean',
          handler: null       
        }],
        rows: pluckAccounts(this.props.accounts),
        pager: {
          index: 0,
          size: 10,
          count:this.props.accounts ? this.props.accounts.length : 0
        }
    }; 
    
//    var currentUsersFields = JSON.parse(JSON.stringify(currentUsersFields1));
//    
//    currentUsersFields.forEach(function (field){
//      if (field.hasOwnProperty('accountId') && field.name === 'selected'){
//        field.handler = self.handleCurrentMembersCheckboxChange;
//      }
//    });

    var userAdded = {id:32, username:'user2@hotmail.com', accountRegisteredOn:new Date((new Date()).getTime() + (2+Math.random()) * 3600000)};
    addedRows.push(userAdded);
    
    var addedUsersFields = {
      fields: [{
        name: 'accountId',
        title: 'id',
        hidden: true
      }, {
          name: 'lastName',
          title: 'Last Name'
      }, {
        name: 'username',
        title: 'Username'
      }, {
        name: 'addedSelected',
        type:'alterable-boolean',
        handler: function(field, row) {
          console.log(this);
          //self.handleCurrentMembersCheckboxChange(field, row);
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

    if(this.props.accounts){
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
                  <Bootstrap.Button >
                    {'>>>'}
                  </Bootstrap.Button>
                </div>
                <br></br>
                <div>
                  <div>
                  <Bootstrap.Button  >
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
    else{
      return (
        <div>
          <img className='preloader' src='/assets/images/utility/preloader-counterclock.png' />
          <img className='preloader-inner' src='/assets/images/utility/preloader-clockwise.png' />
        </div>
      );      
    }

  }
});

function pluckAccounts(accounts){
  for(var obj in accounts){
    var currentId, currentUsername, currentLastName, element, selected;
      
    for(var prop in accounts[obj]){
      if(prop == "id"){
        currentId = accounts[obj][prop];
      } 
      else if(prop == "lastName"){
        currentLastName = accounts[obj][prop];
      }
      else if(prop == "username"){
        currentUsername = accounts[obj][prop];
      } 
      else if(prop == "selected"){
        selected = accounts[obj][prop];
      }
    }

    element = {id: currentId, lastName: currentLastName, username : currentUsername, selected: selected};
    populatedUsers.push(element);
  } 
  return populatedUsers;
}

function mapStateToProps(state) {
  return {    
      accounts: state.announcements.accounts,
      initialUsers: state.announcements.initialUsers,
      addedUsers: state.announcements.addedUsers,
      rowIdToggled: state.announcements.rowIdToggled
  };
}

function mapDispatchToProps(dispatch) {
  return {
    getCurrentUtilityUsers: bindActionCreators(AnnouncementsActions.getCurrentUtilityUsers, dispatch),
    //setUserSelected: bindActionCreators(AnnouncementsActions.setSelectedUser, dispatch),
    setUserSelected: function (accounts, accountId){
      console.log('map id ' + accountId);
      dispatch(AnnouncementsActions.setSelectedUser(accounts, accountId));
    },
    
    setInitialUsers: function (initialUsers){
      dispatch(AnnouncementsActions.setInitialUsers(initialUsers));
    }
  };
}

Announcements.icon = 'wechat';
Announcements.title = 'Section.ManageAlerts.Announcements';
module.exports = connect(mapStateToProps, mapDispatchToProps)(Announcements);
