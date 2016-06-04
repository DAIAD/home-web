
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

var MeasurementReport = React.createClass({
  
  propTypes: {
    routes: PropTypes.array, // supplied by react-router
    config: _configPropType,
  },

  contextTypes: {
    intl: React.PropTypes.object
  },

  render: function() {  
    
    var commonReportProps = {
      config: this.props.config,
      field: 'volume',
    };

    return (
      <div className="container-fluid reports reports-measurements" style={{paddingTop: 10}}>
        <div className="row">
          <div className="col-md-12">
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className="row">
          <div className="col-md-12">
            <reports.MeasurementReport {...commonReportProps} level="week" reportName="avg-daily-avg" />
          </div>
        </div>
      </div>
    );
  },

});

MeasurementReport.icon = 'pie-chart';
MeasurementReport.title = 'Section.Reports.Measurements';

function mapStateToProps(state, ownProps) {
  return {
    config: state.config,
  };
}

function mapDispatchToProps(dispatch, ownProps) {
  return {};
}

module.exports = Redux.connect(mapStateToProps, mapDispatchToProps)(MeasurementReport);
