
var React = require('react');
var {connect} = require('react-redux');

var BaseReport = require('./overview-base');

class Report extends BaseReport {
  
  static get reportKey() {
    return 'overview-utility';
  }
}; 

Report.displayName = 'Overview.UtilityReport';

//
// Container component
//

var actions = require('../../actions/reports-measurements');

var mapStateToProps = Report.mapStateToProps.bind(Report);
var mapDispatchToProps = Report.mapDispatchToProps.bind(Report); 

module.exports = connect(mapStateToProps, mapDispatchToProps)(Report);
