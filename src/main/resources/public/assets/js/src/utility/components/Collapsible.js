var React = require('react');
var ReactDOM = require('react-dom');
var Panel = require('react-bootstrap').Panel;

var Collapsible = React.createClass({
	getDefaultProps: function() {
		return {
			open: true
	    };
	},


  	render: function() {
  		return (
			<Panel bsClass='collapsible' collapsible expanded={this.props.open}>
				{this.props.children}
			</Panel>
 		);
  	}
});

module.exports = Collapsible;
