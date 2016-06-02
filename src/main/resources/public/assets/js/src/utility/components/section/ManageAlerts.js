var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var Checkbox = require('../Checkbox');
var UtilityDropDown = require('../UtilityDropDown');
var ManageAlertsActions = require('../../actions/ManageAlertsActions');
var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');
var TipsEditTable = require('../TipsEditTable');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var Schema = require('../../constants/ManageAlertsTableSchema');
var { getUtilities, addTip, cancelAddTip, showAddTipForm, beganEditingTip } = require('../../actions/ManageAlertsActions');

var Helpers = require('../../helpers/helpers');

var self;
var saveDisabled = true;
var rowsOriginal = [];
var tempRows = [];
var savedTips = [];

var ManageAlerts = React.createClass({
  changedModes: [],
  contextTypes: {
    intl: React.PropTypes.object
  },
  getDefaultProps: function() {
    return {  
        defaultDropDownTitle: 'Select Utility', 
        rowsChanged : [],
        saveButtonDisabled : true
    };
  },
  getInitialState: function(){   
    return {value:"initial state", saveButtonDisabled : false, rowsChanged : []};//TODO, change this to true and resolve state
  },
  componentWillMount : function() {
    this.props.fetchUtilities();   
  },

  editClickedTip : function(tip) {
    this.props.editTip(tip);
  },

  validateNewTipForm: function(title, description, image){
    var errors = [];   
//    if (this.props.admin.addUser.selectedUtility === null){
//      errors.push({code: errorsCodes['ValidationError.Alerts.NO_UTILITY']});
//    }    
//    if (!title){
//      errors.push({code: errorsCodes['ValidationError.Alerts.NO_TITLE']});
//    }
//    if (!description){
//      errors.push({code: errorsCodes['ValidationError.Alerts.NO_DESCRIPTION']});
//    }   
    return errors;
  },  
  
  processAddNewTipForm: function(){    

    var inputFieldsFormValues = {
        title : this.refs.title.getValue(),
        description : this.description.lastName.getValue(),
        image : this.refs.image.getValue()
    };
    
    var errors = this.validateNewTipForm(
          this.refs.title.getValue(),
          this.refs.description.getValue(),
          this.refs.image.getValue()
        );

    if (errors.length === 0){
      this.props.actions.addTipFillForm(inputFieldsFormValues);
      var userInfo = {
          title : this.refs.lastName.getValue(),
          description : this.refs.email.getValue(),
          image : this.refs.address.getValue() === '' ? null : this.refs.address.getValue(),
      };    
      this.props.actions.addTip(tipInfo);
    } else {
      this.props.actions.addTipValidationsErrorsOccurred(errors);
    }
  },  
  
  
  render: function() {
    self = this;
    var _t = this.context.intl.formatMessage;
    if (!this.props.isLoading && this.props.utilities){ 

//      for (let i=0; i < data.fields.length; i++){
//              if (this.props.tips[i].active === true){
//                      this.props.tips[i].handler = tipDeactivate;
//              }
//      }
     var fieldsData = [{
          name: 'index',
          title: 'ID'
        }, {
          name: 'title',
          title: 'Title'
        }, {
          name: 'description',
          title: 'Description'
        }, {
          name: 'createdOn',
          title: 'Created',
          type: 'datetime'
        }, {
            name: 'modifiedOn',
            title: 'Modified',
            type: 'datetime'
        },{
            name: 'active',
            title: 'Active',
            type: 'boolean',
            icon: 'check-square',
        }, {
          name: 'edit',
          type:'action',
          icon: 'pencil',
          handler: function() {           
            self.editClickedTip(this.props.row);
          }
        }, {
        name: 'cancel',
            type:'action',
            icon: 'remove',
            handler: function() {
              console.log(this);
            }
        }];
      
      var data = { 
        filters: Schema.filters,
        fields: fieldsData, 
        rows: populateTips(this),
        pager: {
          index: 0,
          size: 7,
          count:1
        }
      };

      var utilityOptions = populateUtilityOptions(this.props.utilities);

      var filterTitle = (
        < span >
          < i className = 'fa fa-filter fa-fw' > < /i>
          < span style = {{ paddingLeft: 4 }} > Filter < /span>
            < span style = {{float: 'left', marginTop: - 3, marginLeft: 5 }} >
          < /span>
        < /span>
      );
      var filter = (
            < div className = "row" >
              < Bootstrap.Panel header = {filterTitle} >       
                <UtilityDropDown  
                  title = {this.props.utility ? this.props.utility.label : this.props.defaultDropDownTitle}                                   
                  options={utilityOptions}
                  disabled={false}
                  onSelect={this.props.setUtility}  
                />
                <div className='pull-right' >
                  <button id='save'
                          type='submit'
                          className='btn btn-primary'
                                  style={{ height: 33 }}
                                  disabled={this.props.saveOff}
                                  onClick={this.props.saveActive}>
                          <FormattedMessage id='Table.Save' />
                  </button>
                </div>  
                < /Bootstrap.Panel>
            < /div>      
      );
    
      var staticTipsTitle = (
        < span >
          < i className = 'fa fa-list-ol fa-fw' > < /i>
          < span style = {{ paddingLeft: 4 }} > Static Tips < /span>
              < span style = {{float: 'right', marginTop: - 3, marginLeft: 5 }} >          
                < Bootstrap.Button  bsStyle = "default" className = "btn-circle" onClick={this.props.actions.showAddTipForm} >    
                    < Bootstrap.Glyphicon glyph = "plus" />
                < /Bootstrap.Button>              
             
          < /span>
        < /span>
      );

      var table = (
        < div className = "row" >
         < Bootstrap.Panel header = {staticTipsTitle} >
           < Bootstrap.ListGroup fill >
             < Bootstrap.ListGroupItem >
               < TipsEditTable //TODO this was "Table" and had only data={data} // togle with TipsEditTable
                 data = {data} 
                 //saveAction={this.showModalSaveChanges}
                 setActivePage={this.props.setActivePage} 
                 activePage={this.props.activePage}

                 modes={this.props.modes}
                 setModes={this.props.setModes}  
                 
                 saveAction={this.showModalSaveChanges}               
                   
                               > 
               < /TipsEditTable>
             < /Bootstrap.ListGroupItem>
           < /Bootstrap.ListGroup>
         < /Bootstrap.Panel>                 
       < /div>
      );

   
    
    
      var tipsBody = (
        < div className = "container-fluid" style = {{ paddingTop: 10 }} >   
            {filter}
            {table}
        < /div>
      );
      
      var tipForm = (
        <div>
          <Bootstrap.Row>
            <Bootstrap.Col xs={6}>
              <label>Title</label>
            
              <textarea name="Title" 
                rows="2" cols="120" 
                ref="editedTitle"
                defaultValue={this.props.currentTip ? this.props.currentTip.title : ""}
              />
            </Bootstrap.Col>
          </Bootstrap.Row> 
           <Bootstrap.Row>
            <Bootstrap.Col xs={6}>
              <label>Description</label>
              <textarea name="Description" 
                rows="8" cols="120" 
                defaultValue={this.props.currentTip ? this.props.currentTip.description : ""}
              />
            </Bootstrap.Col>
          </Bootstrap.Row>    
        
           <Bootstrap.Row>
            <Bootstrap.Col xs={6}>
                <div>
                  <button id='add'
                          label = 'Add'
                          type = 'submit'
                          className = 'btn btn-primary'
                                  style={{ height: 33}}
                                  onClick={this.props.saveCurrentTip}
                                  disabled={this.props.saveTipDisabled} >
                                   
                          <FormattedMessage id='Save Static Tip' />
                  </button>
                  <button id='cancel'
                          label = 'Cancel'
                          type = 'cancel'
                          className = 'btn btn-primary'
                                  style={{ height: 33, marginLeft : 10}}
                                  onClick={this.props.actions.cancelAddTip}>
                          <FormattedMessage id='Cancel' />
                  </button>  
                </div>
            </Bootstrap.Col>         
          </Bootstrap.Row>           
        </div>  
        );
      
      var addTipForm = (
        < div className = "container-fluid" style = {{ paddingTop: 10 }} >   
            {filter}
            {tipForm}
        < /div>        
      );       
      
      var visiblePart = this.props.show ? addTipForm : tipsBody; //lala

      return (
      <div className="container-fluid" style={{ paddingTop: 10 }}>
        <div className="row">
          <div className="col-md-12">
                  <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        {visiblePart}
      </div>);

    
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

function populateUtilityOptions(utilities){
  var utilityOptions = [];
  for(var obj in utilities){
    var currentName, currentId, currentKey;
    for(var prop in utilities[obj]){     
      if(prop == "name"){
          currentName = utilities[obj][prop];
      } 
      else if(prop == "id"){
          currentId = utilities[obj][prop];
      } 
      else if(prop == "key"){
          currentKey = utilities[obj][prop];
      }      
    }  
    var option = {label: currentName, value: currentId, key: currentKey};
    utilityOptions.push(option);
  } 
  return utilityOptions;
}

function populateTips(object){ 
  var element = {}, populatedTips = [];   
  if(object.props.tips == null){
    return []; //admin has not selected a utility, return empty tips
  }
  else{
    for(var obj in object.props.tips){
      var currentIndex, currentTitle, currentDescription, currentModifiedOn, currentCreatedOn, currentActive;

      for(var prop in object.props.tips[obj]){
        if(prop == "index"){
            currentIndex = object.props.tips[obj][prop];
        } 
        else if(prop == "title"){
            currentTitle = object.props.tips[obj][prop];
        }
        else if(prop == "description"){
            currentDescription = object.props.tips[obj][prop];
        }
        else if(prop == "createdOn"){
            currentCreatedOn = object.props.tips[obj][prop];
        }
        else if(prop == "modifiedOn"){
            currentModifiedOn = object.props.tips[obj][prop];
        }  
        else if(prop == "active"){
            currentActive = object.props.tips[obj][prop];                    
        }
      }
      rowsOriginal.push({index : currentIndex, active : currentActive});
      tempRows.push({index : currentIndex, active : currentActive});
      
      element = {index : currentIndex, title : currentTitle, description : currentDescription, 
        createdOn : currentCreatedOn, modifiedOn : currentModifiedOn, active : currentActive};
      populatedTips.push(element);
    } 
    
//    for (var i = 0; i < tempRows.length; i++) {
//      var row = tempRows[i];
//      console.log('index: ' + row.index + ', active:' + row.active);
//    }
    
    populatedTips.sort(sortBy('index', true));
    
    return populatedTips;
  }
}

var sortBy = function(field, reverse){
   var key = function (x) {return x[field];};

   return function (a,b) {
	  var keyA = key(a), keyB = key(b);
	  return ( (keyA < keyB) ? -1 : ((keyA > keyB) ? 1 : 0) ) * [-1,1][+!!reverse];                  
   };
};

  var computeModesState = function (data){
          var modesState = {};
          var propertyNames = Helpers.pluck(
                                          Helpers.pickQualiffiedOnEquality(data.fields, 'type', 'property'),
                                          'name'
                                  );
          var self = this;
          var rowIds = Helpers.pluck(data.rows, 'id');

          for (var i = 0, len = rowIds.length; i < len; i++){
                  var modeEntry = {};
                  modeEntry.active = data.rows[i].active;
                  modeEntry.modes = {};
                  for (var p = 0, len2 = propertyNames.length; p < len2; p++){
                          var mode = {
                                  value: data.rows[i][propertyNames[p]],
                                  draft: false
                          };
                          modeEntry.modes[propertyNames[p]] = mode;
                  }
                  modesState[rowIds[i]] = modeEntry;
          }
          return modesState;
  };

function mapStateToProps(state) {  
  return {
    utility: state.alerts.utility,
    tips: state.alerts.tips,
    utilities: state.alerts.utilities,
    isLoading: state.alerts.isLoading,
    rowsChanged: state.alerts.rowsChanged,
    activePage: state.alerts.activePage,
    show: state.alerts.show,
    modes: state.alerts.modes,
    currentTip: state.alerts.currentTip,
    saveOff: state.alerts.saveOff,
    saveTipDisabled: state.alerts.saveTipDisabled
  };
}

function mapDispatchToProps(dispatch) {  
  return {
    actions : bindActionCreators(Object.assign({}, {beganEditingTip, showAddTipForm, cancelAddTip}) , dispatch), 
    editTip : bindActionCreators(ManageAlertsActions.editTip, dispatch),
    setUtility: function (event, utility){
      dispatch(ManageAlertsActions.setUtility(event, utility));
      dispatch(ManageAlertsActions.getStaticTips(event, utility));
    },
    saveCurrentTip : function (){
      self.props.currentTip.title = self.refs.editedTitle.value;
      dispatch(ManageAlertsActions.addTip(event, self.props.currentTip));
    },
    fetchUtilities : bindActionCreators(ManageAlertsActions.fetchUtilities, dispatch),
    setActivePage: function(activePage){
      dispatch(ManageAlertsActions.setActivePage(activePage));
    }, 
    setModes: function (modes){
      dispatch(ManageAlertsActions.setModes(modes));
    },
    saveChanges: function(rowsChanged){
      //save changes
    },
    saveActive : function(event){

      if(_.isEqual(rowsOriginal, tempRows)){ 
        return;//nothing to save
      }
      var toBeSaved = tempRows.filter(function(obj) {
        return !rowsOriginal.some(function(obj2) {
            return obj.active == obj2.active;
        });   
      });
      console.log("number of rows to save: " + toBeSaved.length);
      var locale;
      if(self.props.utility.label == 'Alicante'){
        locale = "es";
      }
      else{
        locale = "en";
      }
      dispatch(ManageAlertsActions.saveActiveTips(event, toBeSaved, locale));
    }
  };
}

ManageAlerts.icon = 'server';
ManageAlerts.title = 'Section.ManageAlerts';
module.exports = connect(mapStateToProps, mapDispatchToProps)(ManageAlerts);
