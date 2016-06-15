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
	  var rows = this.membersObjectToArray(Object.assign({}, this.props.currentMembers)).sort(this.compareGroupMembers);
	  if (this.props.currentMembers) {
  	  currentMembers = {
          fields : currentMembersFields,
          rows : rows,
          pager: {
            index: 0,
            size: 10,
            count: rows.length || 0,
            mode: Table.PAGING_CLIENT_SIDE
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
	  
	  
	  
	    
			var groupTitle = null;
			if (this.props.groupInfo) {
  			groupTitle = (
					<span>
						<i className='fa fa-group fa-fw'></i>
						<span style={{ paddingLeft: 4 }}>{this.props.groupInfo.name ? this.props.groupInfo.name : ''}</span>
					</span>
				);
			}

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
			      actions = {{
              cancelAction : this.props.hideFavouriteGroupForm,
              refreshParentForm : function (){}
            }}
          />
			  );
			} else if (this.props.application === 'favouriteAccountForm'){
        return (
            <UpsertFavouriteForm
            type = 'ACCOUNT'
            itemId = {this.props.accountId}
            actions = {{
              cancelAction : this.props.hideFavouriteAccountForm,
              refreshParentForm : function (){}
            }}
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
                    <Bootstrap.ListGroupItem className='clearfix'>
                      <Link className='pull-right' to='/groups' style={{ paddingLeft : 7, paddingTop: 12 }}>Browse all groups</Link>
                    </Bootstrap.ListGroupItem>
                  </Bootstrap.ListGroup>
                </Bootstrap.Panel>
              </div>
              <div className='col-md-8'>
                <Bootstrap.Panel header={memberTitle}>
                  <Bootstrap.ListGroup fill>
                    <Bootstrap.ListGroupItem>
                      <Table data={currentMembers}></Table>
                    </Bootstrap.ListGroupItem>
                    <Bootstrap.ListGroupItem className='clearfix'>
                      <Link className='pull-right' to='/users' style={{ paddingLeft : 7, paddingTop: 12 }}>Browse all users</Link>
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
    hideFavouriteAccountForm : bindActionCreators(GroupActions.hideFavouriteAccountForm, dispatch),    
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Group);


