// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');

// Components
var UtilityApp = require('./components/UtilityApp');

ReactDOM.render(
	<UtilityApp reload={properties.reload} locale={properties.locale} />,
	document.getElementById('app')
);
