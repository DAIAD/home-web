var React = require('react');
var ReactDOM = require('react-dom');
var PortalMixin = require('./PortalMixin');
var L = require('leaflet');
require('leaflet.heat');

var LeafletMap = React.createClass({

	mixins: [PortalMixin],
	
	getDefaultProps: function() {
		return {
			center: [0 ,0],
			zoom: 13
	    };
	},

	render: function() {
		var { prefix, options , ...other } = this.props;
		
		return (
			<div {...other} />
		);
	},
	
	componentDidMount: function() {
		L.Icon.Default.imagePath = '/assets/lib/leaflet/images/';
		this.map = L.map(this.getId()).setView(this.props.options.center, this.props.options.zoom);

		L.tileLayer(
				'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', 
			{
				attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
			}).addTo(this.map);

		var heat = L.heatLayer([
		                    	[38.35, -0.481, 1.0],
		                    	[38.34, -0.484, 1.0],
		                    	[38.352, -0.482, 1.0],
		                    	[38.348, -0.481, 1.0],
		                    	[38.353, -0.479, 1.0],
		                    	[38.341, -0.485, 1.0],
		                    	[38.342, -0.481, 1.0],
		                    	[38.342, -0.483, 1.0],
		                    	[38.347, -0.489, 1.0]
		                    ], {radius: 10, maxZoom: 11}).addTo(this.map);
		
	},

	componentWillUnmount : function() {
		this.map.remove();
	},

});

module.exports = LeafletMap;
