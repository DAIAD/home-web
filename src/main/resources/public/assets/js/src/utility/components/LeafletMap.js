var React = require('react');
var PortalMixin = require('./PortalMixin');

// TODO : Remove jquery dependency
var $ = require('jquery');

var L = require('leaflet');
require('leaflet.heat');
require('leaflet-draw');
require('leaflet-choropleth');

const MODE_VECTOR = 'vector';
const MODE_HEATMAP = 'heat';
const MODE_CHOROPLETH = 'choropleth';
const MODE_DRAW = 'draw';


var _initializeDraw = function() {
  var self = this;

  var drawnItems = new L.FeatureGroup();
  
  self.map.addLayer(drawnItems);

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
  self.map.addControl(drawControl);
  
  self.map.on('draw:created', function (e) {
    var type = e.layerType, layer = e.layer;

    drawnItems.eachLayer(function (l) {
      drawnItems.removeLayer(l);
    });
    
    drawnItems.addLayer(layer);
    
    if(typeof self.props.onDraw === 'function') {
      self.props.onDraw.bind(self)(layer);
    }
  });  
};

var _initializeHeatMap = function(data) {
	if(this.heat) {
		this.map.removeLayer(this.heat);
	}
	this.heat = L.heatLayer(data, {radius: 30, maxZoom: 11}).addTo(this.map);
};

var _initializeChoroPleth = function(geojson, colors) {
  if(this.choropleth) {
    this.map.removeLayer(this.choropleth);
  }
  if(geojson) {
    this.choropleth = L.choropleth(geojson, {
      valueProperty: 'value',
      colors: colors,
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
    
    for(var index in this.overlays){ 
      this.overlays[index].bringToFront();
    }
  }
};

var _initialize = function(props) {
  switch(this.props.mode) {
    case MODE_VECTOR:
      break;
    case MODE_DRAW:
      _initializeDraw.bind(this)();
      break;
    case MODE_HEATMAP:
      _initializeHeatMap.bind(this)(props.data);
      break;
    case MODE_CHOROPLETH:
      _initializeChoroPleth.bind(this)(props.data, props.colors);
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
			mode: MODE_VECTOR,
			colors: ['#2166ac', '#67a9cf', '#d1e5f0', '#f7f7f7', '#fddbc7', '#ef8a62', '#b2182b'],
			urls: []
    };
	},

	render: function() {
		var { prefix, center, zoom, mode, data, onDraw, colors, urls, ...other } = this.props;

		return (
			<div {...other}/>
		);
	},

	componentWillReceiveProps : function(nextProps, nextContext) {
		if(this.map) {
		  _initialize.bind(this)(nextProps);
		}
	},	


	componentDidMount: function() {
		L.Icon.Default.imagePath = '/assets/lib/leaflet/images/';
		this.map = L.map(this.getId()).setView(this.props.center, this.props.zoom);

		L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
		  attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
		}).addTo(this.map);
		
		if(this.props.urls) {
		  var self = this;
		  
		  self.overlays = [];
		  
		  var callback = function( data ) {
		    self.overlays.push(L.geoJson(data, { 
          pointToLayer: function (feature, latlng) {
            return L.circleMarker(latlng, {
              radius: 8,
              fillColor: "#ff7800",
              color: "#000",
              weight: 1,
              opacity: 1,
              fillOpacity: 0.8
            });
          }
        }));
		    
		    self.overlays[self.overlays.length-1].addTo(self.map);
      };

		  for(var index in this.props.urls) {
		    $.getJSON(this.props.urls[index], callback);
		  }
		}

		_initialize.bind(this)(this.props);
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
