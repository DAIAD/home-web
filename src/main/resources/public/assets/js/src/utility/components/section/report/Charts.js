var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../../Breadcrumb');

var errorsCodes = require('../../../constants/Errors');
var successCodes = require('../../../constants/Successes');

var Charts = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },
   
  render: function() {  
    return (
      <div className="container-fluid" style={{ paddingTop: 10 }}>
        <div className="row">
          <div className="col-md-12">
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
      </div>);
    }
});

Charts.icon = 'pie-chart';
Charts.title = 'Section.Reports.Charts';

function mapStateToProps(state) {
  return {
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return { };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Charts);
