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

    // Fixme
    var now = moment('2016-03-09').valueOf(); 

    return (
      <div className="container-fluid">
        <div className="row">
          <div className="col-md-12">
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className="row">
          <div className="col-md-12">
            <reports.Overview config={this.props.config} grouping="utility" now={now} />
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
