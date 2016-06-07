var React = require('react');
var PortalMixin = require('./PortalMixin');

var L = require('leaflet');
require('leaflet.heat');
require('leaflet-draw');
require('leaflet-choropleth');

const MODE_VECTOR = 'vector';
const MODE_HEATMAP = 'heat';
const MODE_CHOROPLETH = 'choropleth';
const MODE_DRAW = 'draw';


var _initializeDraw = function(data) {
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
    var type = e.layerType, layer = e.layer;

    drawnItems.eachLayer(function (l) {
      drawnItems.removeLayer(l);
    });
    
    drawnItems.addLayer(layer);
    
    if(typeof this.props.onDraw === 'function') {
      this.props.onDraw.bind(this)(layer);
    }
  });  
};

var _initializeHeatMap = function(data) {
	if(this.heat) {
		this.map.removeLayer(this.heat);
	}
	this.heat = L.heatLayer(data, {radius: 30, maxZoom: 11}).addTo(this.map);
};

var _initializeChoroPleth = function(geojson) {
  if(this.choropleth) {
    this.map.removeLayer(this.choropleth);
  }
  if(geojson) {
    var choropleth = L.choropleth(geojson, {
      valueProperty: 'value',
      colors: ['#1a9850', '#91cf60', '#d9ef8b', '#ffffbf', '#fee08b', '#fc8d59', '#d73027'],
      steps: 7,
      mode: 'q',
      style: {
          color: '#fff',
          weight: 2,
          fillOpacity: 0.8
      },
      onEachFeature: function(feature, layer) {
          layer.bindPopup('<span><b>' + feature.properties.label + '</b></span></br><span>' + 
                                        feature.properties.value + '</span>');
      }
    }).addTo(this.map);
  }
};

var _initialize = function(data) {
  switch(this.props.mode) {
    case MODE_VECTOR:
      break;
    case MODE_DRAW:
      _initializeDraw.bind(this)(data);
      break;
    case MODE_HEATMAP:
      _initializeHeatMap.bind(this)(data);
      break;
    case MODE_CHOROPLETH:
      _initializeChoroPleth.bind(this)(data);
      break;
  }
  if(this.map) {
    this.map.invalidateSize();
  }
};

var LeafletMap = React.createClass({

	mixins: [PortalMixin],
	
	getDefaultProps: function() {
		return {
			center: [0 ,0],
			zoom: 13,
			data: [],
			mode: MODE_VECTOR
    };
	},

	render: function() {
		var { prefix, center, zoom, mode, data, onDraw, ...other } = this.props;

		return (
			<div {...other}/>
		);
	},

	componentWillReceiveProps : function(nextProps, nextContext) {
		if(this.map) {
		  _initialize.bind(this)(nextProps.data);
		}
	},	


	componentDidMount: function() {
		L.Icon.Default.imagePath = '/assets/lib/leaflet/images/';
		this.map = L.map(this.getId()).setView(this.props.center, this.props.zoom);

		L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
		  attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
		}).addTo(this.map);

		_initialize.bind(this)(this.props.data);
	},

	componentWillUnmount : function() {
		this.map.remove();
	},

});

LeafletMap.MODE_VECTOR = MODE_VECTOR;
LeafletMap.MODE_HEATMAP = MODE_HEATMAP;
LeafletMap.MODE_CHOROPLETH = MODE_CHOROPLETH;
LeafletMap.MODE_DRAW = MODE_DRAW;

module.exports = LeafletMap;
