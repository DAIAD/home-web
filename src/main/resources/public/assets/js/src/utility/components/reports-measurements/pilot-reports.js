
var _ = require('lodash');
var moment = require('moment');
var sprintf = require('sprintf');

var React = require('react');
var Bootstrap = require('react-bootstrap');
var {connect} = require('react-redux');
var Select = require('react-controls/select-dropdown');

var {Panel, PanelGroup, ListGroup, ListGroupItem, Button, Collapse} = Bootstrap;
var PropTypes = React.PropTypes;

var {generateTimestamps} = require('../../helpers/timestamps');
var {product} = require('../../helpers/array-funcs');
var population = require('../../model/population');
var {seriesPropType, populationPropType, reportPropType, configPropType} = require('../../prop-types');
var Report = require('./sliding-report');

var Form = React.createClass({
  propTypes: {
    defaultNow: PropTypes.number,
    setReferenceTime: PropTypes.func,
  },

  statics: {
    _computeNextState: function (props, {config}) {
      var {period} = config.trials;
      var start = moment(period.start).utc();
      var end = moment(start).add(period.duration, 'month');
      return {start, end, now: props.defaultNow || start};
    },
  },

  contextTypes: {config: configPropType},
  
  getInitialState: function () {
    var computedState = this.constructor._computeNextState(this.props, this.context);
    return computedState;
  },

  componentWillReceiveProps: function (nextProps, nextContext) {
    var computedState = this.constructor._computeNextState(nextProps, nextContext);
    this.setState(computedState);
  },

  componentDidMount: function () {
    if (this.props.defaultNow == null) {
      // This means our reference time was never initialized, so initialize
      // it with the value we just read from configuration
      this.props.setReferenceTime(this.state.now.valueOf());
    }
  },

  render: function () {
    var {now, start, end} = this.state;
    
    var monthOptions = new Map(
      Array.from(generateTimestamps(start, end, 'month'))
        .map((t, i) => (
          [t.toString(), sprintf('M%d : %s', i + 1, moment(t).utc().format('MMMM YYYY'))]
        ))
    );
   
    return (
      <form className="form-inline report-form">
        <div className="form-group">
          <label>Select month:</label>&nbsp;
          <Select className="select-month"
            value={now.valueOf().toString()} options={monthOptions}
            onChange={(t) => (this.setState({now: moment(Number(t))}))}
          />
        </div>
        <div className="form-group submit-buttons">
          <Button bsStyle="primary" 
            onClick={() => (this.props.setReferenceTime(now.valueOf()))}
           >
            <i className="fa fa-fw fa-refresh"></i>&nbsp;Refresh
          </Button>
        </div>
      </form>
    );
  },
});

Form.displayName = 'PilotReports.Form';

var ReportsPanel = React.createClass({
  
  propTypes: {
    now: PropTypes.number,
  },
  
  contextTypes: {config: configPropType},
  
  render: function () {
    var {config} = this.context;
    var {now} = this.props;
    
    if (now == null)
      return (<div>No reference time yet...</div>);

    var {sources} = config.reports.byType.measurements;
    var {reports, population: targetNames} = config.trials;
    var {clusters} = config.utility;

    var targets = targetNames.map(name => {
      if (name.startsWith('CLUSTER:')) {
        name = name.substring('CLUSTER:'.length);
        return new population.Cluster(
          clusters.find(c => c.name == name).key, name
        );
      } else {
        return new population.Utility(config.utility.key, config.utility.name);
      }
    });
    
    var nameTarget = (target) => {
      if (target instanceof population.Cluster) {
        return 'Cluster: ' + target.name;
      } else if (target == null || target instanceof population.Utility) {
        return 'Utility';
      }
    };

    var reportItems = product(reports, _.keys(sources), targets)
      .map(([report, source, target], i) => (
        <ListGroupItem key={i}>
          <h4>{report.title} - {sources[source].name} - {nameTarget(target)}</h4>
          <Report 
            field='volume'
            source={source}
            now={now}
            report={report}
            target={target}
            reportKey={sprintf('pilot-%d-%s', i + 1, target.key)}
          />
        </ListGroupItem>    
      ));
    
    var header = (
      <h3>
        Pilot Reports
        <small>
          {sprintf('The report was generated for %s', moment(now).utc().format('MMMM YYYY'))}
        </small>
      </h3>
    );

    return (
      <Panel header={header}> 
        <ListGroup fill>
          {reportItems}
        </ListGroup>
      </Panel>
    );
  },

  shouldComponentUpdate: function (nextProps) {
    return (
      nextProps.now != this.props.now
    ); 
  },
});

ReportsPanel.displayName = 'PilotReports.ReportsPanel';

//
// Containers
//

var actions = require('../../actions/trials');

Form = connect(
  (state, ownProps) => ({
    defaultNow: state.trials.referenceTime,
  }),
  (dispatch, ownProps) => ({
    setReferenceTime: (t) => (dispatch(actions.setReferenceTime(t))),
  })
)(Form);

ReportsPanel = connect(
  (state, ownProps) => ({
    now: state.trials.referenceTime,
  }),
  null
)(ReportsPanel);

//
// Export
//

module.exports = {ReportsPanel, Form};
