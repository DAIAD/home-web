// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');


var HomeApp = require('./components/HomeApp');

ReactDOM.render(
	<HomeApp reload={properties.reload} locale={properties.locale} />, 
		document.getElementById('app'));

