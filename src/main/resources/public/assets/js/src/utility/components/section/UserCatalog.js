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
var InputTextModal = require('../InputTextModal');

var { getAccounts, changeIndex, filterText, filterSerial, clearFilter, getMeter, clearChart,
      setSearchModeText, setSearchModeMap, setGeometry,
      removeFavorite, addFavorite,
      setSelectionMode, discardBagOfConsumers, toggleConsumer, saveBagOfConsumers } = require('../../actions/UserCatalogActions');
  
var _setSelectionMode = function(e) {
  this.props.actions.setSelectionMode(!this.props.userCatalog.selection.enabled);
};

var _show = function() {
  if(Object.keys(this.props.userCatalog.selection.selected).length > 0) {
    this.setState({ modal: true});
  }
};

var _hide = function() {
  this.setState({ modal: false});
};

var _setTitle =  function(key, text) {
  _hide.bind(this)();
  
  if((text) && (key==='save')) {
    _saveBagOfConsumers.bind(this)(text, Object.keys(this.props.userCatalog.selection.selected));
  }
};

var _saveBagOfConsumers = function(title, members) {
  this.props.actions.saveBagOfConsumers(title, members);
};

var _discardBagOfConsumers = function(e) {
  this.props.actions.discardBagOfConsumers();
};

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

  container.on('click', '.add-consumer-to-collection', function(e) {
    e.stopPropagation();
    e.preventDefault();

    self.props.actions.toggleConsumer(feature.properties.userKey);
  });

  var content = [];

  content.push('<span style="font-size: 14px; font-weight: bold;">');
  content.push(feature.properties.name + '&nbsp');
  if(this.props.userCatalog.selection.enabled) {
    content.push('<span class="add-consumer-to-collection" style="cursor: pointer; text-decoration: underline; font-size: 14px;">');
    content.push('<i class="fa fa-shopping-basket fa-fw">&nbsp</i>');
    content.push('</span>');  
  }
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
  
  getInitialState: function() {
    return {
      modal: false
    };
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
    
    if(this.props.userCatalog.selection.enabled) {
      tableConfiguration.fields.splice(1, 0, {
        name: 'selected',
        title: '',
        type: 'alterable-boolean',
        width: 30,
        handler: (function(id, name, value) {
          this.props.actions.toggleConsumer(id);
        }).bind(this)
      });
    }

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
    
    var filterTitle;
    if(this.props.userCatalog.selection.enabled) {
      filterTitle = (
        <span>
          <i className='fa fa-search fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>Search</span>
          <span style={{float: 'right',  marginTop: 2, marginLeft: 5 }}>
            {Object.keys(this.props.userCatalog.selection.selected).length + ' selected'}
          </span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
            <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={_discardBagOfConsumers.bind(this)} >
              <i className='fa fa-remove fa-lg' ></i>
            </Bootstrap.Button>
          </span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
            <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={_show.bind(this)} >
              <i className='fa fa-save fa-lg' ></i>
            </Bootstrap.Button>
          </span>
        </span>
      );
    } else {
      filterTitle = (
        <span>
          <i className='fa fa-search fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>Search</span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
            <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={_setSelectionMode.bind(this)} title='Create a new group' >
              <i className='fa fa-shopping-basket fa-lg' ></i>
            </Bootstrap.Button>
          </span>
        </span>
      );
    }

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
        <InputTextModal 
          onHide={_hide.bind(this)}
          title='Create Group'
          visible={this.state.modal}
          prompt='Title ...'
          help='Set title for the new group'
          actions={
            [{ style : 'default', key : 'save', text : 'Save' },
             { style : 'danger', key : 'cancel', text : 'Cancel' }]
          }
          handler={_setTitle.bind(this)}
        />
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
                         removeFavorite, addFavorite,
                         setSelectionMode, discardBagOfConsumers, toggleConsumer, saveBagOfConsumers }) , dispatch
  )};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(UserCatalog);
