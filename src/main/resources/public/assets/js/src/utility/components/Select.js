var React = require('react');
var ReactDOM = require('react-dom');

var $ = require('jquery');
var BootStrap = require('react-bootstrap');

var Select = require('bootstrap-select');

var ReactBootstrapSelect = React.createClass({
	displayName: 'ReactBootstrapSelect',
	
	getInitialState: function () {
		return {
			open: false
		};
	},

	componentDidUpdate: function () {
		$(ReactDOM.findDOMNode(this)).find('select').selectpicker('refresh');
		var select = $(ReactDOM.findDOMNode(this)).find('div.bootstrap-select');
		select.toggleClass('open', this.state.open);
	},
	
	componentWillUnmount: function () {
		var select = $(ReactDOM.findDOMNode(this)).find('select');
		$(select).selectpicker('destroy');
	},
	
	componentDidMount: function () {
		var select = $(ReactDOM.findDOMNode(this)).find('select');
		$(select).selectpicker();
	},
  
	render: function () {
		return (
			<BootStrap.Input {...this.props} type='select' />
		);
	}
});

module.exports = ReactBootstrapSelect;