var React = require('react');
var ReactDOM = require('react-dom');
var PortalMixin = require('./PortalMixin');
var L = require('leaflet');
require('leaflet.heat');
require('leaflet-draw');

var createHeatMap = function(points) {
	if(this.heat) {
		this.map.removeLayer(this.heat);
	}
	this.heat = L.heatLayer(points, {radius: 30, maxZoom: 11}).addTo(this.map);
};

var LeafletMap = React.createClass({

	mixins: [PortalMixin],
	
	getDefaultProps: function() {
		return {
			center: [0 ,0],
			zoom: 13,
			points: []
	    };
	},

	render: function() {
		var { prefix, options, points, ...other } = this.props;
		
		return (
			<div {...other}/>
		);
	},

	componentWillReceiveProps : function(nextProps, nextContext) {
		if(this.map) {
			createHeatMap.bind(this)(nextProps.points);
			this.map.invalidateSize();			
		}
	},	


	componentDidMount: function() {
		L.Icon.Default.imagePath = '/assets/lib/leaflet/images/';
		this.map = L.map(this.getId()).setView(this.props.options.center, this.props.options.zoom);

		L.tileLayer(
				'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', 
			{
				attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
			}).addTo(this.map);

		var points = this.props.points;

		createHeatMap.bind(this)(points);
		
		if(this.props.options.draw) {
			var drawnItems = new L.FeatureGroup();
			this.map.addLayer(drawnItems);

			var drawControl = new L.Control.Draw({
			    edit: {
			        featureGroup: drawnItems,
			        edit: false,
			        remove: true
			    },
			    draw: {
			    	polyline: false,
			    	rectangle: false,
			    	circle: false,
				    marker: false,
				    polygon: {
				    	shapeOptions: {
				    		color: '#2c3e50',
			                fillColor: '#2980b9'
			            }
				    }
			    }
			});
			this.map.addControl(drawControl);
			
			
			this.map.on('draw:created', function (e) {
				var type = e.layerType,	layer = e.layer;

				drawnItems.eachLayer(function (l) {
					drawnItems.removeLayer(l);
				});
				
				drawnItems.addLayer(layer);
			});
		}		
	},

	componentWillUnmount : function() {
		this.map.remove();
	},

});

module.exports = LeafletMap;
