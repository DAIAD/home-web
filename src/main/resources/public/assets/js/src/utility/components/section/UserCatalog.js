var $ = require('jquery');

var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { Link } = require('react-router');
var Select = require('react-select');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var LeafletMap = require('../LeafletMap');
var Chart = require('../Chart');
var theme = require('../chart/themes/shine');

var { getAccounts, changeIndex, filterText, filterSerial, clearFilter, getMeter, clearChart,
      setSearchModeText, setSearchModeMap, setGeometry,
      removeFavorite, addFavorite} = require('../../actions/UserCatalogActions');
  
var _handleKeyPress = function(e) {
  if (e.key === 'Enter') {
    this.refresh();
  }
};

var _setSearchMode = function(e) {
  if(this.props.userCatalog.search === 'map') {
    this.props.actions.setSearchModeText();
  } else {
    this.props.actions.setSearchModeMap();
  }
};

var _featureRenderer = function(feature) {
  var container = $('<div style="margin: 10px" />');
  
  var self = this;
  
  container.on('click', '.add-meter-chart', function(e) {
    e.stopPropagation();
    e.preventDefault();

    self.props.actions.getMeter(feature.properties.userKey, feature.properties.deviceKey, feature.properties.name);
  });
  
  var content = [];
  content.push('<span style="font-weight: bold;">Customer</span>');
  content.push('<br/>');
  content.push('<span style="font-size: 14px;">');
  content.push(feature.properties.name);
  content.push('</span>');
  content.push('<br/>');
  content.push('<br/>');
  content.push('<span style="font-weight: bold;">Address</span>');
  content.push('<br/>');
  content.push('<span style="font-size: 14px;">');
  content.push(feature.properties.address);
  content.push('</span>');
  content.push('<br/>');
  content.push('<br/>');
  content.push('<span style="font-weight: bold;">Meter Id</span>');
  content.push('<br/>');
  content.push('<span class="add-meter-chart" style="cursor: pointer; text-decoration: underline; font-size: 14px;">');
  content.push(feature.properties.meter.serial);
  content.push('</span>');
  
  container.html(content.join(''));

  return container[0];
};

var _onFeatureChange = function(features) {
  if((!features) || (features.length===0)){
    this.props.actions.setGeometry(null);
  } else {
    this.props.actions.setGeometry(features[0].geometry);
  }
  
};

var _clearChart = function(e) {
  this.props.actions.clearChart();
};

var UserCatalog = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },
  
  componentWillMount : function() {
    if(this.props.userCatalog.accounts == null) {
      this.props.actions.getAccounts();
    }
  },

  onPageIndexChange: function(index) {
    this.props.actions.changeIndex(index);
  },

  filterText: function(e) {
    this.props.actions.filterText(this.refs.accountFilter.getValue());
  },
  
  filterSerial: function(e) {
    this.props.actions.filterSerial(this.refs.serialFilter.getValue());
  },
  
  clearFilter: function(e) {
    this.props.actions.clearFilter();
  },
  
  refresh: function(e) {
    this.props.actions.getAccounts();
  },
  
  render: function() {
    var _t = this.context.intl.formatMessage;
      
    var tableConfiguration = {
      fields: [{
        name: 'id',
        title: 'Id',
        hidden: true
      }, {
        name: 'email',
        title: 'User',
        link: function(row) {
          if(row.id) {
            return '/user/{id}/';
          }
          return null;
        }
      }, {
        name: 'fullname',
        title: 'Name'
      }, {
        name: 'serial',
        title: 'SWM'
      }, {
        name: 'registrationDateMils',
        title: 'Registered On',
        type: 'datetime'
      }, {
        name : 'favorite',
        type : 'action',
        icon : function(field, row) {
          return (row.favorite ? 'star' : 'star-o');
        },
        handler : (function(field, row) {
          if(row.favorite) {
            this.props.actions.removeFavorite(row.id);
          } else {
            this.props.actions.addFavorite(row.id);
          }
        }).bind(this),
        visible : (function(field, row) {
          return (row.meter !== null);
        }).bind(this)
      }, {
        name : 'chart',
        type : 'action',
        icon : 'bar-chart-o',
        handler : (function(field, row) {
          if(row.serial) {
            this.props.actions.getMeter(row.id, row.meter.key, row.fullname);
          }
        }).bind(this),
        visible : (function(field, row) {
          return (row.meter !== null);
        }).bind(this)
      }],
      rows: this.props.userCatalog.data.accounts || [],
      pager: {
        index: this.props.userCatalog.data.index || 0,
        size: this.props.userCatalog.data.size || 10,
        count:this.props.userCatalog.data.total || 0,
        mode: Table.PAGING_SERVER_SIDE
      }    
    };
    
    var tableStyle = {
      row : {
        rowHeight: 50
      }
    }; 
    
    var resetButton = ( <div />);

    if((this.props.userCatalog.query.text) ||
       (this.props.userCatalog.query.serial)) {
      resetButton = (
        <div style={{float: 'right', marginLeft: 20}}>
          <Bootstrap.Button bsStyle='default' onClick={this.clearFilter}>Reset</Bootstrap.Button>
        </div>
      );
    }

    const filterOptions = (
      <Bootstrap.ListGroupItem>
        <div className="row">
          <div className="col-md-4">
            <Bootstrap.Input 
              type='text' 
               id='accountFilter' name='accountFilter' ref='accountFilter'
               placeholder='Account or Name  ...' 
               onChange={this.filterText}
               onKeyPress={_handleKeyPress.bind(this)} 
               value={this.props.userCatalog.query.text || ''} />
              <span className='help-block'>Filter by name or account</span>
          </div>
          <div className="col-md-4">
            <Bootstrap.Input 
              type='text' 
               id='serialFilter' name='serialFilter' ref='serialFilter'
               placeholder='SWM Serial Number  ...' 
               onChange={this.filterSerial}
               onKeyPress={_handleKeyPress.bind(this)} 
               value={this.props.userCatalog.query.serial || ''} />
              <span className='help-block'>Filter meter serial number</span>
          </div>
          <div className="col-md-4" style={{float: 'right'}}>
            {resetButton}
            <div style={{float: 'right'}}>
              <Bootstrap.Button bsStyle='primary' onClick={this.refresh}>Refresh</Bootstrap.Button>
            </div>
          </div>
        </div>
      </Bootstrap.ListGroupItem>
    );

    const dataNotFound = (
        <span>{ this.props.userCatalog.isLoading ? 'Loading data ...' : 'No data found.' }</span>
    );
    
    const filterTitle = (
      <span>
        <i className='fa fa-search fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Search</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
      </span>
    );
    
    const mapTitle = (
      <span>
        <span>
          <i className="fa fa-map fa-fw"></i>
          <span style={{ paddingLeft: 4 }}>Map</span>
        </span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
          <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={_setSearchMode.bind(this)} >
            <i className={ this.props.userCatalog.search === 'map' ? 'fa fa-undo fa-fw' : 'fa fa-pencil fa-fw' }></i>
          </Bootstrap.Button>
        </span>
      </span>
    );

    const mapOptions = {
        center: [51.75692, -0.32678], 
        zoom: 13,
        draw: true
      };

    var map = null;

    switch(this.props.userCatalog.search) {
      case 'map':
        map = (
          <LeafletMap style={{ width: '100%', height: 600}} 
                      elementClassName='mixin'
                      prefix='map'
                      center={[38.35, -0.48]} 
                      zoom={13}
                      mode={[LeafletMap.MODE_DRAW, LeafletMap.MODE_VECTOR]}
                      draw={{
                        onFeatureChange: _onFeatureChange.bind(this)
                      }}
                      vector={{
                        data : this.props.userCatalog.data.features,
                        renderer : _featureRenderer.bind(this)
                      }}
          />
        );
        break;
        
      default:
        map = (
          <LeafletMap style={{ width: '100%', height: 600}} 
                      elementClassName='mixin'
                      prefix='map'
                      center={[38.35, -0.48]} 
                      zoom={13}
                      mode={LeafletMap.MODE_VECTOR}
                      vector={{
                        data : this.props.userCatalog.data.features,
                        renderer : _featureRenderer.bind(this)
                      }}
          />
        );
        break;
    }

    var v, chartTitleText, chartConfig = null, chart = (<span>Select a meter ...</span>), data = [];

    if(Object.keys(this.props.userCatalog.charts).length) {
      chartTitleText = (
        <span>
          <span>
            <i className='fa fa-bar-chart fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>Consumption - Last 30 days</span>
          </span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
            <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={_clearChart.bind(this)} >
              <i className='fa fa-remove fa-fw' ></i>
            </Bootstrap.Button>
          </span>
        </span>
      );
        
      chartConfig = {
        options: {
          tooltip: {
            show: true
          },
          dataZoom: {
            show: true,
            format: 'day'
          }
        },
        data: {
          series: []
        },
        type: 'line'
      };
        
      for(var key in this.props.userCatalog.charts) {
        var c = this.props.userCatalog.charts[key];
        if((c.values) && (c.values.length > 0)) {
          data = [];
  
          for(v=0; v < c.values.length; v++) {
            data.push({
              volume: c.values[v].difference,
              date: new Date(c.values[v].timestamp)
            });
          }
  
          chartConfig.data.series.push({
            legend: c.label,
            xAxis: 'date',
            yAxis: 'volume',
            data: data,
            yAxisName: 'Volume (lt)'
          });
        }
      }
    
      chart = (
        <Chart  style={{ width: '100%', height: 400 }}
                elementClassName='mixin'
                prefix='chart'
                type={chartConfig.type}
                options={chartConfig.options}
                data={chartConfig.data}
                theme={theme}/>
      );
    }

    return (
      <div className="container-fluid" style={{ paddingTop: 10 }}>
        <div className="row">
          <div className="col-md-12">
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className="row">
          <div className="col-md-7">
            <Bootstrap.Panel header={filterTitle}>
              <Bootstrap.ListGroup fill>
                {filterOptions}
                <Bootstrap.ListGroupItem> 
                  <Table  data={tableConfiguration} 
                          onPageIndexChange={this.onPageIndexChange}
                          template={{empty : dataNotFound}}
                          style={tableStyle}
                  ></Table>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
          <div className="col-md-5">
            <Bootstrap.Panel header={mapTitle}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  {map}
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>
        <div className="row">
          <div className="col-md-12">
            <Bootstrap.Panel header={chartTitleText}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  {chart}
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>
      </div>
    );
  }
});

UserCatalog.icon = 'user';
UserCatalog.title = 'Section.Users';

function mapStateToProps(state) {
  return {
      userCatalog: state.userCatalog,
      profile: state.session.profile,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(
      Object.assign({}, {getAccounts, changeIndex, filterSerial, filterText, clearFilter, getMeter, clearChart,
                         setSearchModeText, setSearchModeMap, setGeometry,
                         removeFavorite, addFavorite}) , dispatch
  )};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(UserCatalog);
