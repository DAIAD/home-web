var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { Link } = require('react-router');
var Select = require('react-select');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var Chart = require('../Chart');
var theme = require('../chart/themes/shine');

var { getGroups, changeIndex, deleteGroup, 
      getChart, clearChart, setChartMetric,
      removeFavorite, addFavorite,
      filterByType, filterByName, clearFilter } = require('../../actions/GroupCatalogActions');
  
var _handleKeyPress = function(e) {
  if (e.key === 'Enter') {
    this.refresh();
  }
};

var _setChartMetric = function(e) {
  this.props.actions.setChartMetric(e.value);
};

var _clearChart = function(e) {
  this.props.actions.clearChart();
};

var _filterByType = function(e) {
  this.props.actions.filterByType(e.value === 'UNDEFINED' ? null : e.value);
};


var _filterByName = function(e) {
  this.props.actions.filterByName(this.refs.nameFilter.getValue());
};

var GroupCatalog  = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },
  
  componentWillMount : function() {
    if(this.props.groupCatalog.groups == null) {
      this.props.actions.getGroups();
    }
  },

  onPageIndexChange: function(index) {
    this.props.actions.changeIndex(index);
  },

  refresh: function(e) {
    this.props.actions.getGroups();
  },
  
  render: function() {
    var _t = this.context.intl.formatMessage;
      
    var tableConfiguration = {
      fields: [{
        name: 'id',
        title: 'Id',
        hidden: true
      }, {
        name: 'type',
        title: 'Type',
        width: 100
      }, {
        name: 'text',
        title: 'Name',
        link: function(row) {
          if(row.key) {
            return '/group/{key}/';
          }
          return null;
        }
      }, {
        name: 'size',
        title: '# of members'
      }, {
        name: 'createdOn',
        title: 'Updated On',
        type: 'datetime'
      }, {
        name : 'favorite',
        type : 'action',
        icon : function(field, row) {
          return (row.favorite ? 'star' : 'star-o');
        },
        handler : (function(field, row) {
          if(row.favorite) {
            this.props.actions.removeFavorite(row.key);
          } else {
            this.props.actions.addFavorite(row.key);
          }
        }).bind(this),
        visible : (function(field, row) {
          return (row.type == 'SET');
        }).bind(this)
      }, {
        name : 'chart',
        type : 'action',
        icon : 'bar-chart-o',
        handler : (function(field, row) {
          var utility = this.props.profile.utility;
          
          this.props.actions.getChart(row.key, row.text, utility.timezone);
        }).bind(this)
      }],
      rows: this.props.groupCatalog.data.filtered || [],
      pager: {
        index: 0,
        size: 10,
        count: this.props.groupCatalog.data.filtered.length || 0,
        mode: Table.PAGING_CLIENT_SIDE
      }    
    };
    
    var tableStyle = {
      row : {
        rowHeight: 50
      }
    }; 
    
    var resetButton = ( <div />);

    if((this.props.groupCatalog.query.text) ||
       (this.props.groupCatalog.query.serial)) {
      resetButton = (
        <div style={{float: 'right', marginLeft: 20}}>
          <Bootstrap.Button bsStyle='default' onClick={this.clearFilter}>Reset</Bootstrap.Button>
        </div>
      );
    }

    const filterOptions = (
      <Bootstrap.ListGroupItem>
        <div className="row">
          <div className='col-md-3'>
            <Select name='groupType'
                    value={this.props.groupCatalog.query.type || 'UNDEFINED'}
                    options={[
                      { value: 'UNDEFINED', label: '-' },
                      { value: 'SEGMENT', label: 'Segment' },
                      { value: 'SET', label: 'Set' }
                    ]}
                    onChange={_filterByType.bind(this)}
                    clearable={false} 
                    searchable={false} className='form-group'/>
            <span className='help-block'>Filter group type</span>  
          </div>
          <div className="col-md-3">
            <Bootstrap.Input 
              type='text' 
               id='nameFilter' name='nameFilter' ref='nameFilter'
               placeholder='Name ...' 
               onChange={_filterByName.bind(this)}
               onKeyPress={_handleKeyPress.bind(this)} 
               value={this.props.groupCatalog.query.name || ''} />
              <span className='help-block'>Filter by name</span>
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

    const chartViewOptions = (
      <Bootstrap.ListGroupItem>
        <div className="row">
          <div className='col-md-3'>
            <Select name='chartMetric'
                    value={this.props.groupCatalog.metric}
                    options={[
                      { value: 'SUM', label: 'Total' },
                      { value: 'AVERAGE', label: 'Average' }
                    ]}
                    onChange={_setChartMetric.bind(this)}
                    clearable={false} 
                    searchable={false} className='form-group'/>
            <span className='help-block'>Select value to display ....</span>  
          </div>
        </div>
      </Bootstrap.ListGroupItem>
    );
    
    const dataNotFound = (
        <span>{ this.props.groupCatalog.isLoading ? 'Loading data ...' : 'No data found.' }</span>
    );
    
    const filterTitle = (
      <span>
        <i className='fa fa-search fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Search</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
      </span>
    );
    
    var v, chartTitleText, chartConfig = null, chart = (<span>Select a group ...</span>), data = [];

    if(!Object.keys(this.props.groupCatalog.charts).length) {
      chartTitleText = (
        <span>
        <i className='fa fa-bar-chart fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Consumption - Last 30 days</span>
        </span>
      );
    } else {
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
        
      for(var key in this.props.groupCatalog.charts) {
        var c = this.props.groupCatalog.charts[key];

        if((c.points) && (c.points.length > 0)) {
          data = [];
  
          for(v=0; v < c.points.length; v++) {
            data.push({
              volume: ( this.props.groupCatalog.metric === 'SUM' ? c.points[v].sum : c.points[v].average),
              date: new Date(c.points[v].timestamp)
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
          <div className="col-md-12">
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
                <Bootstrap.ListGroupItem style={{background : '#f5f5f5'}}>
                  {chartTitleText}  
                </Bootstrap.ListGroupItem>
                <Bootstrap.ListGroupItem>
                  {chartViewOptions}
                </Bootstrap.ListGroupItem>
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

GroupCatalog.icon = 'group';
GroupCatalog.title = 'Section.Demographics';

function mapStateToProps(state) {
  return {
      groupCatalog: state.groupCatalog,
      profile: state.session.profile,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(
      Object.assign({}, {getGroups, changeIndex, deleteGroup, 
                         getChart, clearChart, setChartMetric,
                         removeFavorite, addFavorite,
                         filterByType, filterByName, clearFilter }) , dispatch
  )};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(GroupCatalog);
