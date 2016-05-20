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
var { getUtilities } = require('../../actions/ManageAlertsActions');
var Helpers = require('../../helpers/helpers');
var EditTable = require('../EditTable');
var TipsEditTable = require('../TipsEditTable');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var Schema = require('../../constants/ModeManagementTableSchema');

var self;
var saveDisabled = true;
var rowsOriginal = [];
var tempRows = [];
var savedTips = [];

var ManageAlerts = React.createClass({
  contextTypes: {
    intl: React.PropTypes.object
  },
  getDefaultProps: function() {
    console.log('getDefaultProps');
    return {  
        defaultDropDownTitle: 'Select Utility', 
        rowsChanged : [],
        saveButtonDisabled : true
    };
  },
  getInitialState: function(){
    console.log('initial state');    
    return {value:"initial state", saveButtonDisabled : false, rowsChanged : []};//TODO, change this to true and resolve state
  },
  componentWillMount : function() {
    this.props.fetchUtilities();    
  },
  render: function() {
    self = this;
    console.log('RENDERING MANAGE ALERTS');

    if (!this.props.isLoading && this.props.utilities){ 

      var tipDeactivate = function(){
              var title = _t({ id:'Modal.DeactivateUser.Title'});

              var body = _t({ id:'Modal.DeactivateUser.Body.Part1'}) +
                      this.props.row.name +
              _t({ id:'Modal.DeactivateUser.Body.Part2'}) +
              this.props.row.id +
              _t({ id:'Modal.DeactivateUser.Body.Part3'});

              var actions = [{
                      action: self.closeModal,
                      name: _t({id:'Buttons.Cancel'})
              },  {
                      action: self.decativateUser,
                      name: _t({id:'Buttons.Deactivate'}),
                      style: 'danger'
              }
      ];
              self.openModal(title, body, actions);
              self.props.markUserForDeactivation(this.props.row);	

      };

//      for (let i=0; i < data.fields.length; i++){
//              if (this.props.tips[i].active === true){
//                      this.props.tips[i].handler = tipDeactivate;
//              }
//      }
                
                
      var _t = this.context.intl.formatMessage;            
      var data = { 
        filters: Schema.filters,
        fields: [{
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
            onClick: function(){
              console.log(this);
              console.log(this.props.row.index);
              //console.log('deactivated id 1 ' + self.props.tips[1].active);             
              console.log('this.props.row' +this.props.row);
              self.props.checkBoxClicked(this.props.row, self.props.tips);              
            },
            handler: function() {
              console.log(this);
              console.log(this.props.row.index);
              //console.log('deactivated id 1 ' + self.props.tips[1].active);             
              console.log('this.props.row' +this.props.row);
              self.props.checkBoxClicked(this.props.row, self.props.tips);
            }
        }, {
          name: 'edit',
          type:'action',
          icon: 'pencil',
          handler: function() {
            console.log(this);
          }
        }, {
        name: 'cancel',
            type:'action',
            icon: 'remove',
            handler: function() {
              console.log(this);
            }
        }], 
        rows: populateTips(this),
        pager: {
          index: 0,
          size: 60,
          count:1
        }
      };

      var utilityOptions = populateUtilityOptions(this.props.utilities);

      const staticTipsTitle = (
        < span >
          < i className = 'fa fa-list-ol fa-fw' > < /i>
          < span style = {{ paddingLeft: 4 }} > Static Tips < /span>
              < span style = {{float: 'right', marginTop: - 3, marginLeft: 5 }} >
                < Bootstrap.Button	bsStyle = "default" className = "btn-circle" >
                    < Bootstrap.Glyphicon glyph = "plus" />
                < /Bootstrap.Button>
          < /span>
        < /span>
      );

      const filter = (
        < span >
          < i className = 'fa fa-filter fa-fw' > < /i>
          < span style = {{ paddingLeft: 4 }} > Filter < /span>
            < span style = {{float: 'left', marginTop: - 3, marginLeft: 5 }} >
          < /span>
        < /span>
      );
      return (

        < div className = "container-fluid" style = {{ paddingTop: 10 }} >       
          < div className = "row" >
            < div className = "row" >
                < Breadcrumb routes = {this.props.routes} />
            < /div>
          < /div>
          < div className = "row" >
            < div className = "row" >
              < Bootstrap.Panel header = {filter} >       
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
                                  disabled={this.state.saveButtonDisabled}
                                  onClick={this.props.saveActive}>
                          <FormattedMessage id='Table.Save' />
                  </button>
                </div>  
                < /Bootstrap.Panel>
            < /div>
            < div className = "row" >
              < Bootstrap.Panel header = {staticTipsTitle} >
                < Bootstrap.ListGroup fill >
                  < Bootstrap.ListGroupItem >
                    < TipsEditTable //TODO this was "Table" and had only data={data} // togle with TipsEditTable
                      data = {data} 
                      //saveAction={this.showModalSaveChanges}
                      //setActivePage={this.props.setActivePage}           
                      saveAction={this.showModalSaveChanges}
                      activePage={this.props.activePage}
                      modes={this.props.modes}
                      setModes={this.props.setModes}                     
                      
                      
                                    > 
                    < /TipsEditTable>
                  < /Bootstrap.ListGroupItem>
                < /Bootstrap.ListGroup>
              < /Bootstrap.Panel>                 
            < /div>
          < /div>      
        < /div>    
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

function mapStateToProps(state) {  
  console.log('mapStateToProps, state.alerts.saveButtonDisabled ' + state.alerts.saveButtonDisabled);

  return {
    utility: state.alerts.utility,
    tips: state.alerts.tips,
    utilities: state.alerts.utilities,
    isLoading: state.alerts.isLoading,
    rowsChanged: state.alerts.rowsChanged,
    saveButtonDisabled : state.alerts.saveButtonDisabled,
    activePage: state.alerts.activePage
  };
}

function mapDispatchToProps(dispatch) {
  console.log('mapDispatchToProps ' );   
  return {
    setUtility: function (event, utility){
      dispatch(ManageAlertsActions.setUtility(event, utility));
      dispatch(ManageAlertsActions.getStaticTips(event, utility));
    },
    fetchUtilities : bindActionCreators(ManageAlertsActions.fetchUtilities, dispatch),
    saveChanges: function(rowsChanged){
      //save changes
    },
    setActivePage: function(activePage){
            dispatch(ManageAlertsActions.setActivePage(activePage));
    },
    checkBoxClicked: function(tip, tips){
      console.log('dispatching checkBoxClicked ' + tip.index);
      dispatch(ManageAlertsActions.checkBoxClicked(event, tip, tips));
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
