var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var Select = require('react-select');
var Breadcrumb = require('../../Breadcrumb');
var Table = require('../../Table');

var { getEvents, changeIndex, filterAccount, filterLevel, clearFilter } = require('../../../actions/LoggingActions');
  
var handleKeyPress = function(e) {
  if (e.key === 'Enter') {
    this.refresh();
  }
};

var Logging = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },
  
  componentWillMount : function() {
    if(this.props.logging.events == null) {
      this.props.actions.getEvents();
    }
  },

  onPageIndexChange: function(index) {
    this.props.actions.changeIndex(index);
  },

  filterAccount: function(e) {
    this.props.actions.filterAccount(this.refs.accountFilter.getValue());
  },
  
  filterLevel: function(e) {
    this.props.actions.filterLevel(e.value === 'UNDEFINED' ? null : e.value);
  },
  
  clearFilter: function(e) {
    this.props.actions.clearFilter();
  },
  
  refresh: function(e) {
    this.props.actions.getEvents();
  },
  
  render: function() {
    var _t = this.context.intl.formatMessage;
      
    var data = {
      fields: [{
        name: 'id',
        title: 'Id',
        hidden: true
      }, {
        name: 'level',
        title: 'Level',
        align: 'center',
        className: function(value) {
          switch(value) {
            case "FATAL": case "ERROR":
              return 'log_error';
            case "WARN":
              return 'log_warn';
            case "INFO":
              return 'log_info';
            case "DEBUG": case "TRACE":
              return 'log_debug';
          }
            return '';
        }
      }, {
        name: 'category',
        title: 'Category'
      }, {
        name: 'code',
        title: 'Code'
      }, {
        name: 'timestamp',
        title: 'Created On',
        type: 'datetime'
      }, {
        name: 'message',
        title: 'Message'
      }, {
        name: 'remoteAddress',
        title: 'Source'
      }, {
        name: 'account',
        title: 'Account'
      }],
      rows: this.props.logging.data.events || [],
      pager: {
        index: this.props.logging.data.index || 0,
        size: this.props.logging.data.size || 10,
        count:this.props.logging.data.total || 0,
        mode: Table.PAGING_SERVER_SIDE
      }
    };

    const dataNotFound = (
        <span>No events found.</span>
    );
    
    const searchHeader = (
      <span>
        <i className='fa fa-search fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Search</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
      </span>
    );

    const eventHeader = (
      <span>
        <i className='fa fa-table fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Events</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
      </span>
    );

    return (
      <div className="container-fluid" style={{ paddingTop: 10 }}>
        <div className="row">
          <div className="col-md-12">
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className="row">
          <div className="col-md-12">
            <Bootstrap.Panel header={searchHeader}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <div className="row">
                    <div className="col-md-2">
                      <Bootstrap.Input 
                        type='text' 
                         id='accountFilter' name='accountFilter' ref='accountFilter'
                         placeholder='Search by account ...' 
                         onChange={this.filterAccount}
                         onKeyPress={handleKeyPress.bind(this)} 
                         value={this.props.logging.query.account || ''} />
                        <span className='help-block'>Filter by account</span>
                    </div>
                    <div className="col-md-2">
                      <Select name='level'
                              value={this.props.logging.query.level || 'UNDEFINED'}
                              options={[
                                { value: 'UNDEFINED', label: '-' },
                                { value: 'DEBUG', label: 'DEBUG' },
                                { value: 'INFO', label: 'INFO' },
                                { value: 'WARN', label: 'WARN' },
                                { value: 'ERROR', label: 'ERROR' }
                              ]}
                              onChange={this.filterLevel}
                              clearable={false} 
                              searchable={false} className="form-group"/>
                      <span className='help-block'>Filter by level</span>  
                    </div>
                    <div className="col-md-4" style={{float: 'right'}}>
                      <div style={{float: 'right'}}>
                        <Bootstrap.Button bsStyle='default' onClick={this.clearFilter}>Reset</Bootstrap.Button>
                      </div>
                      <div style={{float: 'right', marginRight: 20}}>
                        <Bootstrap.Button bsStyle='primary' onClick={this.refresh}>Refresh</Bootstrap.Button>
                      </div>
                    </div>
                  </div>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>
        <div className="row">
          <div className="col-md-12">
            <Bootstrap.Panel header={eventHeader}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem> 
                  <Table  data={data} 
                          onPageIndexChange={this.onPageIndexChange}
                          template={{empty : dataNotFound}}
                  ></Table>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>
      </div>
    );
  }
});

Logging.icon = 'history';
Logging.title = 'Section.Support.Logging';

function mapStateToProps(state) {
  return {
      logging: state.logging,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(
      Object.assign({}, {getEvents, changeIndex, filterLevel, filterAccount, clearFilter}) , dispatch
  )};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Logging);
