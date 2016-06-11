
var moment = require('moment');

var React = require('react');
var Bootstrap = require('react-bootstrap');
var ReactRedux = require('react-redux');
var Select = require('react-controls/select-dropdown');
var DatetimeInput = require('react-datetime');

var {Panel, PanelGroup, ListGroup, ListGroupItem, Button, Collapse} = Bootstrap;
var PropTypes = React.PropTypes;

var population = require('../../model/population');
var {seriesPropType, populationPropType, reportPropType, configPropType} = require('../../prop-types');
var {ReportByDay, ReportByWeek, ReportByMonth, ReportByYear} = require('./common-reports');

var commonPropTypes = {
  now: PropTypes.number.isRequired,
  source: PropTypes.string.isRequired,
  field: PropTypes.string.isRequired,
  uom: PropTypes.string.isRequired,
};

var OverviewPanel = React.createClass({
 
  statics: {
    itemSpecs: {
      day: {
        title: 'Last Day',
        Report: ReportByDay,
      },
      week: {
        title: 'Last Week',
        Report: ReportByWeek,
      },
      month: {
        title: 'Last Month',
        Report: ReportByMonth,
      },
      year: {
        title: 'Last Year',
        Report: ReportByYear,
      },
    }
  },

  propTypes: {
    ...commonPropTypes,
    reports: PropTypes.shape({
      day: reportPropType,
      week: reportPropType,
      month: reportPropType,
      year: reportPropType,
    }),
    reportKey: PropTypes.string,
    target: populationPropType,
    title: PropTypes.string.isRequired,
  },
  
  getDefaultProps: function () {
    return {};  
  },

  render: function () {
    var {itemSpecs} = this.constructor;
    var {now, field, source, uom, reports, reportKey, target} = this.props;
    
    var reportProps = {
      now, 
      source, 
      field, 
      uom, 
      reportKey: !reportKey? 
        (target? target.toString().toLowerCase() : 'utility') : reportKey,
      target,
    };
      
    var items = _.values(
      _.mapValues(itemSpecs, (y, k) => (
        <ListGroupItem key={k}>
          <h4>{y.title}</h4>
          <y.Report {...reportProps} report={reports[k]} />
        </ListGroupItem>
      ))
    );
    
    return (
      <Panel header={this.props.title}>
        <ListGroup fill>
          {items} 
        </ListGroup>
      </Panel>
    );
  }
});

var OverviewPanelGroup = React.createClass({
  
  getInitialState: function () {
    return {
      activeKey: 'utility',
    };
  },
  
  shouldComponentUpdate: function (nextProps, nextState) {
    return (
      (nextProps.now != this.props.now) ||
      (nextProps.field != this.props.field) ||
      (nextProps.source != this.props.source) ||
      (nextState.activeKey != this.state.activeKey)
    );
  },

  render: function () {
    var {now, field, source, uom, reports} = this.props;
    var visible = (k) => (this.state.activeKey == k);
    
    var commonProps = {source, field, uom, now};

    var reportProps = {
      utility: {
        ...commonProps,
        reportKey: 'utility',
        target: null,
        visible: visible('utility')
      },
      perEfficiency: {
        ...commonProps,
        reportKey: 'per-efficiency',
        target: null, // Fixme population.Cluster
        visible: visible('per-efficiency')
      },
    };
    
    return (
      <PanelGroup accordion onSelect={this._selectPanel} activeKey={this.state.activeKey}>
        
        <Panel id="overview-utility" header="Water Consumption - Utility" eventKey="utility">
          <ListGroup fill>
            <ListGroupItem>
              <h4>Last Day</h4>
              <ReportByDay {...reportProps.utility} report={reports.day} />
            </ListGroupItem>
            <ListGroupItem>
              <h4>Last Week</h4>
              <ReportByWeek {...reportProps.utility} report={reports.week} />
            </ListGroupItem>
            <ListGroupItem>
              <h4>Last Month</h4>
              <ReportByMonth {...reportProps.utility} report={reports.month} />
            </ListGroupItem>
            <ListGroupItem>
              <h4>Last Year</h4>
              <ReportByYear {...reportProps.utility} report={reports.year} />
            </ListGroupItem>
          </ListGroup>
        </Panel>
        
        <Panel id="overview-per-efficiency" header="Water Consumption - Customer Efficiency" eventKey="per-efficiency">
          <div>Todo</div> 
        </Panel>
        
      </PanelGroup>    
    );
  },

  _selectPanel: function (key) {
    if (this.state.activeKey != key)
      this.setState({activeKey: key});
    return true;
  },

});

var Form = React.createClass({
  
  statics: {
    
    defaults: {
      datetimeProps: {
        dateFormat: 'D MMM[,] YYYY', 
        timeFormat: null,
        inputProps: {size: 10},
      },
    },

    _propsToState: function ({now, field, source}) {
      // Reset state according to newly received props
      return {now, field, source, submitted: false};
    },
  },
  
  contextTypes: {
    config: configPropType,
  },

  propTypes: {
    ...commonPropTypes,
    generated: PropTypes.number,
  },

  getInitialState: function () {
    return this.constructor._propsToState(this.props);
  },

  componentWillReceiveProps: function (nextProps) {
    this.setState(this.constructor._propsToState(nextProps));
  },

  render: function () {
    var {defaults} = this.constructor;
    var {config} = this.context;
    var {source, field, now, submitted} = this.state;

    var {fields, sources} = config.reports.byType.measurements;
    
    var sourceOptions = new Map(
      _.values(
        _.mapValues(sources, (s, k) => ([k, s.name])))
    );

    var fieldOptions = new Map(
      _.values(
        _.mapValues(
          fields, (y, k) => ((y.sources.indexOf(source) < 0)? null : [k, y.name])
        ))
      .filter(y => y)
    );

    return(
      <form className="form-inline report-form">
        
        <div className="form-group" title="Select source">
          <Select className="select-source"
            value={source}  
            onChange={(val) => (this.setState({source: val}), false)}
            options={sourceOptions}
           />
        </div>
        
        <div className="form-group">
          <Select className="select-field"
            value={field}  
            onChange={(val) => (this.setState({field: val}), false)}
            options={fieldOptions}
           />      
        </div>  
        
        <div className="form-group">
          <label>Use reference time:</label>
          <DatetimeInput {...defaults.datetimeProps} 
            value={now} 
            onChange={(m) => (this.setState({now: m.valueOf()}), false)}
           />
        </div>

        <div className="form-group submit-buttons">
          <Button className="submit-btn" bsStyle="default" title="Export to PDF" 
            onClick={this._exportToPdf} disabled={true}
           >
            <i className="fa fa-send-o"></i>&nbsp; Export
          </Button>
          <Button className="submit-btn" bsStyle="primary" title="Re-generate reports" 
            onClick={this._submit} disabled={submitted}
           >
            <i className={"fa fa-refresh" + (submitted? ' fa-spin': '')}></i>&nbsp; Refresh
          </Button>
        </div>
      </form>
    );
  },
 
  // Event handlers

  _exportToPdf: function () {
    console.info('Todo: Exporting to PDF...');
    return false;
  },

  _submit: function () {
    if (!this.state.now) {
      console.warn('No reference time was given! Skipping refresh');
      return false;
    }
    
    // Get the timestamp of the chosen YYYY-MM-DD date at GMT (UTC offset 0)
    var t = moment(this.state.now);
    t = moment(t.format('YYYY-MM-DD') + 'T00:00:00Z');
    if (!t.isValid()) {
      console.warn('Failed to convert to a valid moment! Skipping refresh'); 
      return false;
    }
    
    this.props.submit(this.state.source, this.state.field, t.valueOf());
    this.setState({submitted: true});
    
    return false;
  }, 

});

//
// Container components
//

var actions = require('../../actions/overview.js');
var {connect} = ReactRedux;

var mapStateToProps = function (state, ownProps) {
  var {field, referenceTime, source, requested} = state.overview;
  return {
    now: referenceTime,
    field,
    source,
    uom: state.config.reports.byType.measurements.fields[field].unit,
    generated: requested, // well, roughly
  };
};

OverviewPanel = connect(mapStateToProps, null)(OverviewPanel);

OverviewPanelGroup = connect(mapStateToProps, null)(OverviewPanelGroup);

Form = connect(
  mapStateToProps,
  (dispatch, ownProps) => {
    return {
      submit: (source, field, now) => (dispatch(actions.setup(source, field, now))),
    };
  }
)(Form);

// Export

module.exports = {
  OverviewPanelGroup,
  OverviewPanel,
  Form,
};
