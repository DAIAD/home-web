var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var {FormattedDate} = require('react-intl');

var MessageAlert = require('../../AlertDismissable');

var errorsCodes = require('../../../constants/Errors');
var successCodes = require('../../../constants/Successes');

var UpsertFavouriteFormActions = require('../../../actions/UpsertFavouriteFormActions');

var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var UpsertFavouriteForm = React.createClass({
  
  contextTypes: {
    intl: React.PropTypes.object
  },
  
  componentWillMount : function() {
    if (this.props.type === 'ACCOUNT'){
      this.props.fetchFavouriteAccountStatus(this.props.itemId);
    } else {
      this.props.fetchFavouriteGroupStatus(this.props.itemId);
    }
  },
  
  getDefaultProps: function() {
    return {
      type: null,
      itemId: null,
      actions : {
        cancelAction : function(){},
        refreshParentForm : function(){}
      }
      
     };
  },
  
  updateLabel : function(){
    this.props.setFavouriteLabel(this.refs.favouriteLabel.getValue());
  },
  
  validateUpsertFavouriteForm : function (){
    var errors = [];
        
    if(!this.props.favouriteInfo.label){
      errors.push({code: errorsCodes['ValidationError.Favourite.NO_LABEL']});
    }
    
    return errors;
  },
  
  
  processUpsertFavourite : function (){    
    var errors = this.validateUpsertFavouriteForm();
    
    if (errors.length === 0){
      this.props.upsertFavourite({
        key: this.props.favouriteInfo.itemId,
        label: this.props.favouriteInfo.label,
        type: this.props.type
      });
            
    } else {
      this.props.upsertFavouriteValidationErrorsOccurred(errors);
    }
  },
  

  
  render: function(){
    
    var self = this;
    var _t = this.context.intl.formatMessage;
    
    
    var itemInfoTitleText = this.props.type === 'GROUP' ? 
          _t({ id:'Demographics.NewFavourite.Group.FavouriteGroupInfo'}) : 
          _t({ id:'Demographics.NewFavourite.Account.FavouriteAccountInfo'});
          
    const itemInfoTitle = (
        <span>
          <i className='fa fa-info fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>{itemInfoTitleText}</span>  
        </span>
      );
    
    
    const favouriteLabel = (
        <span>
          <i className='fa fa-bookmark fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>{_t({ id:'Demographics.NewFavourite.Label'})}</span>  
        </span>
      );
    
    
    var createdOn = null;
    var included = null;
    var successCode = null;
    var buttons = null;
    var messageAlert = null;
    
    if (this.props.favouriteInfo){
      switch (this.props.type) {
      case 'ACCOUNT':
        if (this.props.favouriteInfo.accountCreatedOn){
          createdOn = (<FormattedDate value={this.props.favouriteInfo.accountCreatedOn} day='numeric' month='long' year='numeric' />);
        } else {
          createdOn = '-';
        }
        break;
      case 'GROUP':
        if (this.props.favouriteInfo.groupCreatedOn){
          createdOn = (<FormattedDate value={this.props.favouriteInfo.groupCreatedOn} day='numeric' month='long' year='numeric' />);
        } else {
          createdOn = '-';
        }
        break;
      }
      
      
      if (this.props.favouriteInfo.included === true){
        included = (<b>{_t({ id:'Demographics.NewFavourite.Status.Included'})}</b>);
      } else if (this.props.favouriteInfo.included === false) {
        included = (<b>{_t({ id:'Demographics.NewFavourite.Status.NotIncluded'})}</b>);
      } else {
        included = ('-');
      }
      
      if(this.props.favouriteInfo.justAdded){
        successCode = 'FavouriteSuccess.FAVOURITE_ADDED';
      } else {
        successCode = 'FavouriteSuccess.FAVOURITE_UPDATED';
      }
            
      const cancelButton = (
        <Bootstrap.Col xs={6}>
          <Bootstrap.Button onClick={
              function (){
                self.props.actions.cancelAction();
                self.props.actions.refreshParentForm();
             }}>
            {_t({ id:'Buttons.Cancel'})}
          </Bootstrap.Button>
        </Bootstrap.Col>
      );
      
      if (this.props.favouriteInfo.included === true){
        buttons = (
            <div style={{ float: 'left'}}>
              {cancelButton}
              <Bootstrap.Col xs={6}>
                  <Bootstrap.Button bsStyle='success' onClick={this.processUpsertFavourite}>
                    {_t({ id:'Buttons.UpdateFavourite'})}
                  </Bootstrap.Button>
              </Bootstrap.Col>
            </div>
           );
        
      } else if (this.props.favouriteInfo.included === false){
        buttons = (
            <div style={{ float: 'left'}}>
              {cancelButton}
              <Bootstrap.Col xs={6}>
                  <Bootstrap.Button bsStyle='success' onClick={this.processUpsertFavourite}>
                    {_t({ id:'Buttons.AddFavourite'})}
                  </Bootstrap.Button>
              </Bootstrap.Col>
            </div>
           );
      } else {
        buttons = (
            <div style={{ float: 'left'}}>
              {cancelButton}
            </div>
           );
      }
      
      messageAlert = (
        <div className='col-md-12'>
            <MessageAlert
              show={this.props.showMessageAlert}
              title={!this.props.success ? _t({id: 'Form.ErrorsDetected'}) : _t({id: 'Form.Success'})}
              i18nNamespace={this.props.success ? 'Success.' : 'Error.'}
              bsStyle={this.props.success ? 'success' : 'danger' }
              format='list'
              messages={!this.props.success ? this.props.errors : [{code: successCodes[successCode]}]}
              dismissFunc={this.props.hideMessageAlert}
            />
        </div>
      );
    }
    
    var accountInfoUI = null;
    var groupInfoUI = null;
    if (this.props.favouriteInfo){     
      
      accountInfoUI = (
          <Bootstrap.ListGroup fill>
            <Bootstrap.ListGroupItem>
              <div className='row'>
                <table className='table table-profile'>
                  <tbody>
                    <tr>
                      <td>{_t({ id:'Demographics.NewFavourite.Account.Name'})}</td>
                      <td>{this.props.favouriteInfo.accountName ? this.props.favouriteInfo.accountName : '-'}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Demographics.NewFavourite.Account.Email'})}</td>
                      <td>{this.props.favouriteInfo.accountEmail ? this.props.favouriteInfo.accountEmail : '-'}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Demographics.NewFavourite.Account.Gender.label'})}</td>
                      <td>{this.props.favouriteInfo.accountGender ? this.props.favouriteInfo.accountGender : '-'}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Demographics.NewFavourite.Account.CreatedOn'})}</td>
                      <td>{createdOn}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Demographics.NewFavourite.Account.City'})}</td>
                      <td>{this.props.favouriteInfo.accountCity ? this.props.favouriteInfo.accountCity : '-'}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Demographics.NewFavourite.Account.Country'})}</td>
                      <td>{this.props.favouriteInfo.accountCountry ? this.props.favouriteInfo.accountCountry : '-'}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Demographics.NewFavourite.Account.NumDevices'})}</td>
                      <td>{this.props.favouriteInfo.accountDevicesNum ? this.props.favouriteInfo.accountDevicesNum : 0}</td>
                    </tr> 
                    <tr>
                      <td>{_t({ id:'Demographics.NewFavourite.Status.Label'})}</td>
                      <td>{included}</td>
                    </tr> 
                  </tbody>
                </table>
              </div>
            </Bootstrap.ListGroupItem>
          </Bootstrap.ListGroup>
      );      
      
      groupInfoUI = (
          
          <Bootstrap.ListGroup fill>
            <Bootstrap.ListGroupItem>
              <div className='row'>
                <table className='table table-profile'>
                  <tbody>
                    <tr>
                      <td>{_t({ id:'Group.Name'})}</td>
                      <td>{this.props.favouriteInfo.groupName ? this.props.favouriteInfo.groupName : '-'}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Group.Description'})}</td>
                      <td>{this.props.favouriteInfo.groupName ? this.props.favouriteInfo.groupName : '-'}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Group.CreatedOn'})}</td>
                      <td>{createdOn}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Group.Country'})}</td>
                      <td>{this.props.favouriteInfo.groupCountry ? this.props.favouriteInfo.groupCountry : '-'}</td>
                    </tr>
                    <tr>
                      <td>{_t({ id:'Group.Size'})}</td>
                      <td>{this.props.favouriteInfo.groupSize ? this.props.favouriteInfo.groupSize : 0}</td>
                    </tr> 
                    <tr>
                      <td>{_t({ id:'Demographics.NewFavourite.Status.Label'})}</td>
                      <td>{included}</td>
                    </tr> 
                  </tbody>
                </table>
              </div>
            </Bootstrap.ListGroupItem>
          </Bootstrap.ListGroup>
      );
      
    }
    
    
    if (this.props.favouriteInfo){
      var infoUI;
      if (this.props.type === 'ACCOUNT'){
        infoUI = accountInfoUI;
      } else {
        infoUI = groupInfoUI;
      }
      return (
          <div className='container-fluid' style={{ paddingTop: 10 }}>
            <div className='row'>
              <div className='col-md-5'>
                <Bootstrap.Panel header={itemInfoTitle}>
                  {infoUI}
                </Bootstrap.Panel>
              </div>
              <div className='col-md-7'>
                <Bootstrap.Panel header={favouriteLabel}>
                  <Bootstrap.ListGroup fill>
                    <Bootstrap.ListGroupItem>
                      <div className='row'>
                        <div className='col-md-12'>
                          <Bootstrap.Input 
                            type='text' 
                            value={this.props.favouriteInfo.label}
                            label={_t({ id:'Demographics.NewFavourite.Label'})}
                            ref='favouriteLabel' 
                            placeholder={_t({ id:'Demographics.NewFavourite.LabelPlaceholder'})}
                            onChange={this.updateLabel}
                          />
                        </div>
                      </div>
                      <div className='row'>
                        {messageAlert}
                      </div>
                      <div className='row'>
                        {buttons}
                      </div>
                    </Bootstrap.ListGroupItem>
                  </Bootstrap.ListGroup>
                </Bootstrap.Panel>
              </div>
            </div>
          </div>
      );
    } else {
      return (
          <div className='container-fluid' style={{ paddingTop: 10 }}>
            <div className='row'>
              <div className='col-md-5'>
              </div>
            </div>
          </div>
       );
    }
  }
  
});
      
function mapStateToProps(state) {
  return {
    isLoading : state.upsertFavouriteForm.isLoading,
    success : state.upsertFavouriteForm.success,
    errors : state.upsertFavouriteForm.errors,
    favouriteInfo : state.upsertFavouriteForm.favouriteInfo,
    showMessageAlert : state.upsertFavouriteForm.showMessageAlert
  };
}

function mapDispatchToProps(dispatch) {
  return {
    fetchFavouriteAccountStatus: bindActionCreators(UpsertFavouriteFormActions.fetchFavouriteAccountStatus, dispatch),
    fetchFavouriteGroupStatus: bindActionCreators(UpsertFavouriteFormActions.fetchFavouriteGroupStatus, dispatch),
    setFavouriteLabel: bindActionCreators(UpsertFavouriteFormActions.setFavouriteLabel, dispatch),
    upsertFavourite: bindActionCreators(UpsertFavouriteFormActions.upsertFavourite, dispatch),
    upsertFavouriteValidationErrorsOccurred: bindActionCreators(UpsertFavouriteFormActions.upsertFavouriteValidationErrorsOccurred, dispatch),
    hideMessageAlert: bindActionCreators(UpsertFavouriteFormActions.hideMessageAlert, dispatch)
  };
}


module.exports = connect(mapStateToProps, mapDispatchToProps)(UpsertFavouriteForm);



