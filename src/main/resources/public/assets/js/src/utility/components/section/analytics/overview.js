var _ = require('lodash');
var moment = require('moment');

var React = require('react');
var ReactDOM = require('react-dom');
var Redux = require('react-redux');
var Bootstrap = require('react-bootstrap');
var Breadcrumb = require('../../Breadcrumb');

var reports = require('../../reports');

var PropTypes = React.PropTypes;
var _configPropType = PropTypes.shape({
  utility: PropTypes.object,
  reports: PropTypes.object,
  overview: PropTypes.object,
});

var Report = React.createClass({
  displayName: 'Analytics.Overview',

  propTypes: {
    routes: PropTypes.array, // supplied by react-router
    config: _configPropType,
  },

  contextTypes: {
    intl: React.PropTypes.object
  },

  render: function() {  

    // Fixme should be an input
    // Get the timestamp of a wall-clock at UTC with a specific date
    var now = moment('2016-03-05T00:00:00Z').valueOf(); 
    //var now = moment(); now = now.add(now.utcOffset(), 'minute').valueOf(); 

    return (
      <div className="container-fluid">
        <div className="row">
          <div className="col-md-12">
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className="row">
          <div className="col-md-12">
            <reports.Overview config={this.props.config} grouping={null} now={now} />
          </div>
        </div>
      </div>
    );
  },

});

Report.icon = 'bullseye';
Report.title = 'Section.Analytics.Overview';

function mapStateToProps(state, ownProps) {
  return {
    config: state.config,
  };
}

function mapDispatchToProps(dispatch, ownProps) {
  return {};
}

module.exports = Redux.connect(mapStateToProps, mapDispatchToProps)(Report);
