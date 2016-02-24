var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');

var Forecasting = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

  	render: function() {
  		var _t = this.context.intl.formatMessage;
 
  		return (
			<div className="container-fluid" style={{ paddingTop: 10 }}>
				<div className="row">
					<div className="col-md-12">
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
			</div>
 		);
  	}
});

Forecasting.icon = 'line-chart';
Forecasting.title = 'Section.Forecasting';

module.exports = Forecasting;
