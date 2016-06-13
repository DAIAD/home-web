var _ = require('lodash');
var React = require('react');
var {connect} = require('react-redux');
var Breadcrumb = require('../../Breadcrumb');

var PropTypes = React.PropTypes;
var {configPropType} = require('../../../prop-types');

var {PilotReports} = require('../../reports');

var Page = React.createClass({
  displayName: 'Trials.PilotReports',

  propTypes: {
    routes: PropTypes.array, // supplied by react-router
    config: configPropType,
  },

  contextTypes: {
    intl: React.PropTypes.object
  },

  render: function() {
    return (
      <div className="container-fluid">
        <div className="row">
          <div className="col-md-12">
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className="row">
          <div className="col-md-12">
            <PilotReports config={this.props.config} />
          </div>
        </div>
      </div>
    );
  },

});

Page.icon = 'pie-chart';
Page.title = 'Section.Trials.PilotReports';

function mapStateToProps(state, ownProps) {
  return {
    config: state.config,
  };
}

function mapDispatchToProps(dispatch, ownProps) {
  return {};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Page);
