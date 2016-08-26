var React = require('react');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../../Breadcrumb');
var Message = require('../../Message');
var Select = require('react-select');
var DateRangePicker = require('react-bootstrap-daterangepicker');
var FilterTag = require('../../chart/dimension/FilterTag');
var GroupSearchTextBox = require('../../GroupSearchTextBox');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var DateRangePicker = require('react-bootstrap-daterangepicker');

var Table = require('../../UserTable');

var { fetchMessages, getTimeline, setEditor, setEditorValue, setTimezone } 
 = require('../../../actions/MessageAnalyticsActions');


var _onIntervalEditorChange = function (event, picker) {
  this.props.actions.setEditorValue('interval', [picker.startDate, picker.endDate]);
};

var onPopulationEditorChange = function(e) {
  if(!e) {
    var utility = this.props.profile.utility;

    e = {
      key: utility.key,
      name: utility.name,
      type: 'UTILITY'
    };
  }
  this.props.actions.setEditorValue('population', e);
};


var _setEditor = function(key) {
  this.props.actions.setEditor(key);
};

var AnalyticsMap = React.createClass({

  contextTypes: {
      intl: React.PropTypes.object
  },
  componentWillMount : function() {
    this.props.actions.fetchMessages();   
  },
  componentDidMount : function() {
    var utility = this.props.profile.utility;
   
    this.props.actions.setTimezone(utility.timezone);
    if(!this.props.map.timeline) {
      var population = {
          utility: utility.key,
          label: utility.name,
          type: 'UTILITY'
      };
    }
  },

  render: function() {
    // Filter configuration
    var intervalLabel ='';
    if(this.props.interval) {
      var start = this.props.interval[0].format('DD/MM/YYYY');
      var end = this.props.interval[1].format('DD/MM/YYYY');
      intervalLabel = start + ' - ' + end;
      if (start === end) {
        intervalLabel = start;
      }
    }     

    var intervalEditor = (
      <div className='col-md-3'>
        <DateRangePicker  startDate={this.props.interval[0]} 
                  endDate={this.props.interval[1]} 
                  ranges={this.props.ranges} 
                  onEvent={_onIntervalEditorChange.bind(this)}>
          <div className='clearfix Select-control' style={{ cursor: 'pointer', padding: '5px 10px', width: '100%'}}>
            <span>{intervalLabel}</span>
          </div>
          </DateRangePicker>
          <span className='help-block'>Select time interval</span>
      </div>
    );
    
    var populationEditor = (
      <div className='col-md-3'>
        <GroupSearchTextBox name='groupname' onChange={onPopulationEditorChange.bind(this)}/>
        <span className='help-block'>Select a consumer group</span>
      </div>
    );
   
    var filter = null;

    switch(this.props.editor) {
      case 'interval':
        filter = (
          <Bootstrap.ListGroupItem>
            <div className="row">
              {intervalEditor}
            </div>
          </Bootstrap.ListGroupItem>
        );
        break;

      case 'population':
        filter = (
          <Bootstrap.ListGroupItem>
            <div className="row">
              {populationEditor}
            </div>
          </Bootstrap.ListGroupItem>
        );
        break;
    }

    var mapTitle = (
      <span>
        <i className='fa fa-bar-chart fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Statistics</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5, display : (this.props.editor ? 'block' : 'none' ) }}>
          <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={_setEditor.bind(this, null)}>
            <i className='fa fa-rotate-left fa-fw'></i>
          </Bootstrap.Button>
        </span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5}}>
        <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={_setEditor.bind(this, 'population')}>
            <i className='fa fa-group fa-fw'></i>
          </Bootstrap.Button>
        </span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5}}>
        <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={_setEditor.bind(this, 'interval')}>
            <i className='fa fa-calendar fa-fw'></i>
          </Bootstrap.Button>
        </span>
      </span>
    );

    var map, mapFilterTags = [];

    mapFilterTags.push( 
      <FilterTag key='time' text={intervalLabel} icon='calendar' />
    );
    mapFilterTags.push( 
      <FilterTag key='population' text={ this.props.population ? this.props.population.label : 'All' } icon='group' />
    );

    var messageFields = {
      fields: [{
        name: 'id',
        title: 'ID'
      }, {
          name: 'message',
          title: 'Message'
      }, {
        name: 'type',
        title: 'Type'
      }, {
        name: 'receivers',
        title: 'Total Receivers'
      }],
      rows: this.props.messages ? populateMessageStatistics(this.props.messages) : [],
      pager: {
        index: 0,
        size: 10,
        count:0
      }
    };
    
    map = (
      <Bootstrap.ListGroup fill>
        {filter}
        <Bootstrap.ListGroupItem>
          <Table data = {messageFields} > </Table>
        </Bootstrap.ListGroupItem>
        <Bootstrap.ListGroupItem className='clearfix'>
          <div className='pull-left'>
            {mapFilterTags}
          </div>       
          <span style={{ paddingLeft : 7}}> </span>
          <Link className='pull-right' to='/' style={{ paddingLeft : 7, paddingTop: 12 }}>View dashboard</Link>
        </Bootstrap.ListGroupItem>     
      </Bootstrap.ListGroup>
    );

    var mapPanel = (
      <Bootstrap.Panel header={mapTitle}>
        {map}
      </Bootstrap.Panel>
    );
   
    return (
      <div className='container-fluid' style={{ paddingTop: 10 }}>
        <div className='row'>
          <div className='col-md-12'>
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className='row'>
          <div className='col-md-12'>
            {mapPanel}
          </div>
        </div>
      </div>
    );
    }
});

function populateMessageStatistics(messageMap){ 
  var populated = [];
  var element = {};
  for (var prop in messageMap){
    var currentId, currentIndex, currentTitle, currentDescription;
    console.log(prop);
  }
  return populated;
}

function mapStateToProps(state) {
  console.log('map success?? ->>');
  console.log(state.messages.messages);
  return {
      source: state.map.source,
      population: state.map.population,
      interval: state.map.interval,
      editor: state.map.editor,
      ranges: state.map.ranges,
      map: state.map.map,
      profile: state.session.profile,
      routing: state.routing,
      messages: state.messages.messages
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { fetchMessages,
                                                     setEditor, setEditorValue,
                                                     setTimezone}) , dispatch)
  };
}

AnalyticsMap.icon = 'commenting';
AnalyticsMap.title = 'Section.ManageAlerts.Messages';

module.exports = connect(mapStateToProps, mapDispatchToProps)(AnalyticsMap);
