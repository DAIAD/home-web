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

var { fetchMessages, changeIndex, showReceivers, goBack, setEditor, setEditorValue, setTimezone, setSelectedMessage } 
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
  
  onPageIndexChange: function(index) {
    this.props.actions.changeIndex(index);
  },
 
  clickedShowReceivers : function(row) {
    this.props.actions.showReceivers(row);
  },
  
  render: function() {
    var self = this;
    if(this.props.showReceivers && !this.props.isLoading){
      
      var receiversFields = {
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
          name: 'acknowledgedOn',
          title: 'Acknowledged On',
          type: 'datetime'
        }],
        rows: this.props.receivers,
        pager: {
          index: 0,
          size: 10,
          count:this.props.receivers ? this.props.receivers.length : 0
        }
      };
      
      var receiversTitle = (
        <span>
          <i className='fa fa-calendar fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>Users that received this Message</span>
            <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
        </span>
      ); 
     
      var messageInfoTitle = (
        <span>
          <i className='fa fa-calendar fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>Message Info</span>
            <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
        </span>
      );          
      var receiversTable = (
        <div>
          <Table data={receiversFields}></Table>
        </div>
      );

    var messageInfo = (
      <div>
        <Bootstrap.Row>
          <Bootstrap.Col xs={6}>
            <label>Title:</label>  
          </Bootstrap.Col>
          <Bootstrap.Col xs={6}>
            <div style={{fontSize:16}}>            
              <label>{this.props.selectedMessage.title}</label>   
            </div>
          </Bootstrap.Col>
        </Bootstrap.Row>        
         <Bootstrap.Row>
          <Bootstrap.Col xs={6}>
            <label>Description:</label>
          </Bootstrap.Col>
          <Bootstrap.Col xs={6}>
            <div style={{fontSize:16}}>
              <label>{this.props.selectedMessage.description}</label>
            </div> 
          </Bootstrap.Col>         
        </Bootstrap.Row>  
         <Bootstrap.Row>
          <Bootstrap.Col xs={6}>
            <label>Total receivers:</label>
          </Bootstrap.Col>
          <Bootstrap.Col xs={6}>
            <div style={{fontSize:16}}>
              <label>{this.props.selectedMessage.receiversCount}</label>
            </div> 
          </Bootstrap.Col>         
        </Bootstrap.Row>                
      </div>  
      );
     
      return (     
        < div className = "container-fluid" style = {{ paddingTop: 10 }} >  
          <div className="row">
              <Bootstrap.Panel header={messageInfoTitle}>
                <Bootstrap.ListGroup fill>
                  <Bootstrap.ListGroupItem>	       
                    {messageInfo}  
                  </Bootstrap.ListGroupItem>
                </Bootstrap.ListGroup>
              </Bootstrap.Panel> 
          </div>        
          <div className="row">
              <Bootstrap.Panel header={receiversTitle}>
                <Bootstrap.ListGroup fill>
                  <Bootstrap.ListGroupItem>	       
                    {receiversTable}  
                  </Bootstrap.ListGroupItem>
                </Bootstrap.ListGroup>
              </Bootstrap.Panel> 
          </div>   
            <div className="row">
              <Bootstrap.Button 
                onClick = {this.props.actions.goBack}>
                {'Back'}
              </Bootstrap.Button>
            </div>
        </div>  

      );       
    }
  
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

    var statistics, filterTags = [];

    filterTags.push( 
      <FilterTag key='time' text={intervalLabel} icon='calendar' />
    );
    filterTags.push( 
      <FilterTag key='population' text={ this.props.population ? this.props.population.label : this.props.profile.utility.name } icon='group' />
    );

    var messageFields = {
      fields: [{
        name: 'id',
        title: 'ID'
      }, {
          name: 'title',
          title: 'Message'
      }, {
        name: 'type',
        title: 'Type'
      }, {
        name: 'receiversCount',
        title: 'Total Receivers'
      }, {
          name: 'details',
          type:'action',
          icon: 'group',
          handler: function() {           
            self.props.actions.setSelectedMessage(this.props.row);
            self.clickedShowReceivers(this.props.row);
          }
        }],
      rows: this.props.messages ? this.props.messages : [],
      pager: {
        index: 0,
        size: 10,
        count: this.props.messages ? this.props.messages.length : 0,
        mode: Table.PAGING_CLIENT_SIDE        
      }
  	 };    
       
    statistics = (
      <Bootstrap.ListGroup fill>
        {filter}
        <Bootstrap.ListGroupItem>
          <Table 
            data = {messageFields} 
            onPageIndexChange={this.onPageIndexChange}> 
          </Table>
        </Bootstrap.ListGroupItem>
        <Bootstrap.ListGroupItem className='clearfix'>
          <div className='pull-left'>
            {filterTags}
          </div>       
          <span style={{ paddingLeft : 7}}> </span>
          <Link className='pull-right' to='/' style={{ paddingLeft : 7, paddingTop: 12 }}>View dashboard</Link>
        </Bootstrap.ListGroupItem>     
      </Bootstrap.ListGroup>
    );

    var statisticsPanel = (
      <Bootstrap.Panel header={mapTitle}>
        {statistics}
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
            {statisticsPanel}
          </div>
        </div>
      </div>
    );
    }
});

function mapStateToProps(state) {
  console.log(state);
  return {
      source: state.map.source,
      population: state.map.population,
      interval: state.map.interval,
      editor: state.map.editor,
      ranges: state.map.ranges,
      map: state.map.map,
      profile: state.session.profile,
      routing: state.routing,
      messages: state.messages.messages,
      showReceivers: state.messages.showReceivers,
      receivers: state.messages.receivers,
      selectedMessage: state.messages.selectedMessage
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { fetchMessages, changeIndex, 
                                                     setEditor, setEditorValue,
                                                     setTimezone, showReceivers, 
                                                     setSelectedMessage, goBack}) , dispatch)                                                     
  };
}

AnalyticsMap.icon = 'commenting';
AnalyticsMap.title = 'Section.ManageAlerts.Messages';

module.exports = connect(mapStateToProps, mapDispatchToProps)(AnalyticsMap);
