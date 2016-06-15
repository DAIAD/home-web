var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var {FormattedDate} = require('react-intl');
var Table = require('../../Table');
var MessageAlert = require('../../AlertDismissable');

var errorsCodes = require('../../../constants/Errors');
var successCodes = require('../../../constants/Successes');

var Helpers = require('../../../helpers/array-funcs');
var DemographicsTablesSchema = require('../../../constants/DemographicsTablesSchema');



var CreateGroupForm = React.createClass({
  
  contextTypes: {
    intl: React.PropTypes.object
  },
  
  getDefaultProps: function() {
    return {
      newGroupName: null,
      currentMembers: null,
      possibleMembers: null,
      messageAlert: {
        show: false,
        success: null,
        errors: null,
        dismissFunc : function(){}
      },
      actions: {
        toggleCandidateGroupMemberToRemove: function(){},
        toggleCandidateGroupMemberToAdd: function(){},
        addSelectedGroupMembers: function(){},
        removeSelectedGroupMembers: function(){},
        hideNewGroupForm: function(){},
        getGroupsAndFavourites: function(){},
        setGroupName: function(){},
        createGroupSet: function(){},
        addGroupValidationErrorsOccurred: function(){} 
      }
     };
  },
  
  handleCurrentMembersCheckboxChange: function (rowId, propertyName, currentValue){
    this.props.actions.toggleCandidateGroupMemberToRemove(rowId, currentValue);
  },
  
  handlePossibleMembersCheckboxChange: function (rowId, propertyName, currentValue){
    this.props.actions.toggleCandidateGroupMemberToAdd(rowId, currentValue);
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
  
  compareGroupMembers : function (a, b){
    return a.name.localeCompare(b.name);
  },
  
  updateGroupName : function (){
    this.props.actions.setGroupName(this.refs.groupName.getValue());
  },
  
  validateAddNewGroupForm : function (groupName, groupMembersIds){
    var errors = [];
    
    if(!groupName){
      errors.push({code: errorsCodes['ValidationError.Group.NO_GROUP_NAME']});
    }
    
    if (groupMembersIds.length === 0){
      errors.push({code: errorsCodes['ValidationError.Group.NO_GROUP_MEMBERS']});
    }
    
    return errors;
  },
  
  processAddNewGroupForm : function (){
    var groupName = this.refs.groupName.getValue();
    var groupMembersIds = Helpers.pluck(this.membersObjectToArray(this.props.currentMembers),'id');
    
    var errors = this.validateAddNewGroupForm(groupName, groupMembersIds);
    
    if (errors.length === 0){
      this.props.actions.setGroupName(groupName);
      var groupInfo = {
          name : groupName,
          members : groupMembersIds
      };
      
      this.props.actions.createGroupSet(groupInfo);
      
    } else {
      this.props.actions.addGroupValidationErrorsOccurred(errors);
    }
  },
  
  render : function(){
    
    var self = this;
    var _t = this.context.intl.formatMessage;
    
    
    const newGroupInfoFormTitle = (
        <span>
          <i className='fa fa-group fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>{_t({ id:'Demographics.NewGroup.NewGroup'})}</span>
        </span>
      );
    
    var membersTitle = (
        <span className='clearfix'>
          <span>
            <i className='fa fa-user fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>{_t({ id:'Demographics.NewGroup.CurrentMembers'})}</span>
          </span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
            <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={this.props.actions.removeSelectedGroupMembers} >
              <i className='fa fa-arrow-right fa-fw'></i>
            </Bootstrap.Button>
          </span>
        </span>
      );
    
    var nonMembersTitle = (
        <span className='clearfix'>
          <span style={{ float: 'left', marginTop: -3, marginLeft: 5 }}>
            <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={this.props.actions.addSelectedGroupMembers} >
              <i className='fa fa-arrow-left fa-fw'></i>
            </Bootstrap.Button>
          </span>
          <span style={{float: 'right'}}>
            <i className='fa fa-user-plus fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>{_t({ id:'Demographics.NewGroup.PossibleMembers'})}</span>
          </span>
        </span>
      );
    
    var currentMembersFields = JSON.parse(JSON.stringify(DemographicsTablesSchema.GroupMembers.fields));
    var possibleMembersFields = JSON.parse(JSON.stringify(DemographicsTablesSchema.GroupMembers.fields));
    
    
    currentMembersFields.forEach(function (field){
      if (field.hasOwnProperty('name') && field.name === 'selected'){
        field.handler = self.handleCurrentMembersCheckboxChange;
      }
    });

    possibleMembersFields.forEach(function (field){
      if (field.hasOwnProperty('name') && field.name === 'selected'){
        field.handler = self.handlePossibleMembersCheckboxChange;
      }
    });

    var rows = this.membersObjectToArray(this.props.currentMembers).sort(this.compareGroupMembers);
    
    var currentMembers = {
        fields : currentMembersFields,
        rows : rows,
        pager : {
          index : DemographicsTablesSchema.GroupMembers.pager.index,
          size : DemographicsTablesSchema.GroupMembers.pager.size,
          count : rows.length//Math.ceil(this.props.currentMembers.length / DemographicsTablesSchema.GroupMembers.pager.size)
        }
    };
    rows = this.membersObjectToArray(this.props.possibleMembers).sort(this.compareGroupMembers);
    var possibleMembers = {
        fields : possibleMembersFields,
        rows : rows,
        pager : {
          index : DemographicsTablesSchema.GroupMembers.pager.index,
          size : DemographicsTablesSchema.GroupMembers.pager.size,
          count : rows.length //Math.ceil(this.props.possibleMembers.length / DemographicsTablesSchema.GroupMembers.pager.size)
        }
    };
    
    return (
      <div className='row'>
        <div className='col-md-12'>
          <Bootstrap.Panel header={newGroupInfoFormTitle}>
            <Bootstrap.ListGroup fill>
              <Bootstrap.ListGroupItem>
                <Bootstrap.Row>
                  <Bootstrap.Col xs={4}>
                      
                        <Bootstrap.Input 
                          type='text' 
                          value={this.props.newGroupName}
                          label={_t({ id:'Demographics.NewGroup.Name'}) + ' (*)'} 
                          ref='groupName' 
                          placeholder={_t({ id:'Demographics.NewGroup.NamePlaceholder'})}
                          onChange={this.updateGroupName}
                        />
                      
                   </Bootstrap.Col>
                 </Bootstrap.Row>
              </Bootstrap.ListGroupItem>
            </Bootstrap.ListGroup>
            <div className='row equal-height-row'>
              <div className='col-md-6 equal-height-col'>
                <Bootstrap.Panel header={membersTitle}>
                  <Bootstrap.ListGroup fill>
                    <Bootstrap.ListGroupItem> 
                      <Table data={currentMembers}></Table>
                    </Bootstrap.ListGroupItem>
                  </Bootstrap.ListGroup>
                </Bootstrap.Panel>
              </div>
              <div className='col-md-6 equal-height-col'>
                <Bootstrap.Panel header={nonMembersTitle}>
                  <Bootstrap.ListGroup fill>
                    <Bootstrap.ListGroupItem> 
                      <Table data={possibleMembers}></Table>
                    </Bootstrap.ListGroupItem>
                  </Bootstrap.ListGroup>
                </Bootstrap.Panel>
              </div>
            </div>
            
            <div className='row'>
              <div className='col-md-6'>
                  <MessageAlert
                    show={this.props.messageAlert.show}
                    title={!this.props.messageAlert.success ? _t({id: 'Form.ErrorsDetected'}) : _t({id: 'Form.Success'})}
                    i18nNamespace={this.props.messageAlert.success ? 'Success.' : 'Error.'}
                    bsStyle={this.props.messageAlert.success ? 'success' : 'danger' }
                    format='list'
                    messages={!this.props.messageAlert.success ? this.props.messageAlert.errors : [{code: successCodes['GroupSuccess.GROUP_CREATED']}]}
                    dismissFunc={this.props.messageAlert.dismissFunc}
                  />
              </div> 
            </div>
            <div className='row'>
              <div style={{ float: 'left'}}>
                <Bootstrap.Col xs={6}>
                  <Bootstrap.Button onClick={function(){
                      self.props.actions.hideNewGroupForm();
                      self.props.actions.getGroupsAndFavourites();
                      }
                    }>
                    {_t({ id:'Buttons.Cancel'})}
                  </Bootstrap.Button>
                </Bootstrap.Col>
                <Bootstrap.Col xs={6}>
                    <Bootstrap.Button bsStyle='success' onClick={this.processAddNewGroupForm}>
                    {_t({ id:'Buttons.CreateGroup'})}
                    </Bootstrap.Button>
                </Bootstrap.Col>
              </div>
            </div>
          </Bootstrap.Panel>
        </div>
      </div>
        
    );
  }
  
  
});

module.exports = CreateGroupForm;
