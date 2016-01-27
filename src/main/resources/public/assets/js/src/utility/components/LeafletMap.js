var React = require('react');
var ReactDOM = require('react-dom');
var PortalMixin = require('./PortalMixin');
var L = require('leaflet');

var LeafletMap = React.createClass({

	mixins: [PortalMixin],
	
	getDefaultProps: function() {
		return {
			center: [0 ,0],
			zoom: 13
	    };
	},

	render: function() {
		return (
			<div {...this.props} />
		);
	},
	
	componentDidMount: function() {
		L.Icon.Default.imagePath = '../assets/lib/leaflet/images/';
		
		this.map = L.map(this.getId()).setView(this.props.center, this.props.zoom);

		L.tileLayer(
				'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', 
			{
				attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
			}).addTo(this.map);
	},

	componentWillUnmount : function() {
		this.map.remove();
	},

});

module.exports = LeafletMap;
