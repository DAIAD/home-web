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
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');

var ManageAlerts = React.createClass({
  contextTypes: {
    intl: React.PropTypes.object
  },
  getDefaultProps: function() {
    console.log('getDefaultProps');
    return {  
        defaultDropDownTitle: 'Select Utility',        
    };
  },      
  getInitialState: function(){
    //console.log('initial state');    
    return {value:"initial state"};
  },
  componentWillMount : function() {
    console.log('fetching utilities');
    this.props.fetchUtilities();    
  },
  handleCheckboxChange: function (rowId, propertyName, currentValue){
    var truthTable = {
      'ACTIVE': true,
      'INACTIVE': false
    };

    var inverseTruthTable = {
      true: 'ACTIVE',
      false: 'INACTIVE'
    };

    var currentModesState = Object.assign({}, this.props.modes);

    if (currentModesState[rowId].modes[propertyName].value === 'NOT_APPLICABLE'){
      return;
    } else {
      currentModesState[rowId].modes[propertyName] = {
        value: inverseTruthTable[!truthTable[this.props.modes[rowId].modes[propertyName].value]],
        draft: !this.props.modes[rowId].modes[propertyName].draft								  
      };
    }

    this.props.setModes(currentModesState);
  },
  render: function() {

    if (!this.props.isLoading && this.props.utilities){ 

      var _t = this.context.intl.formatMessage;            
      var data = { 
        filters: [{
          id: 'utilityName',
          name: 'Utility',
          field: 'utilityName',
          icon: 'utility',
          type: 'text'
        }],
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
        }, {
            name: 'active',
            title: 'Active',
            type: 'boolean',
            handler: function() {
              console.log(this);

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
                                  onClick={this.saveModeChanges}>
                          <FormattedMessage id='Table.Save' />
                  </button>
                </div>  
                < /Bootstrap.Panel>
            < /div>
            < div className = "row" >
              < Bootstrap.Panel header = {staticTipsTitle} >
                < Bootstrap.ListGroup fill >
                  < Bootstrap.ListGroupItem >
                    < Table data = {data} > < /Table>
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
      element = {index : currentIndex, title : currentTitle, description : currentDescription, 
        createdOn : currentCreatedOn, modifiedOn : currentModifiedOn, active : currentActive};
      populatedTips.push(element);
    } 
    return populatedTips;
  }
}

function mapStateToProps(state) {  
  return {
    utility: state.alerts.utility,
    tips: state.alerts.tips,
    utilities: state.alerts.utilities,
    isLoading: state.alerts.isLoading

  };
}

function mapDispatchToProps(dispatch) {
  return {
    setUtility: function (event, utility){
      dispatch(ManageAlertsActions.setUtility(event, utility));
      dispatch(ManageAlertsActions.getStaticTips(event, utility));
    },
    fetchUtilities : bindActionCreators(ManageAlertsActions.fetchUtilities, dispatch)
  };
}

ManageAlerts.icon = 'server';
ManageAlerts.title = 'Section.ManageAlerts';
module.exports = connect(mapStateToProps, mapDispatchToProps)(ManageAlerts);
