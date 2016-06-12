var React = require('react');
var PortalMixin = require('./PortalMixin');

// TODO : Remove jquery dependency
var $ = require('jquery');

var L = require('leaflet');
require('leaflet.heat');
require('leaflet-draw');

const MODE_VECTOR = 'vector';
const MODE_HEATMAP = 'heat';
const MODE_CHOROPLETH = 'choropleth';
const MODE_DRAW = 'draw';


var _initializeDraw = function() {
  var self = this;
  
  if(this.draw) {
    this.map.removeControl(this.draw);
    this.map.off('draw:created', this.drawHandler);
  }

  if(!this.drawnItems) {
    this.drawnItems = new L.FeatureGroup();
    
    this.map.addLayer(this.drawnItems);
  }
  
  this.draw = new L.Control.Draw({
    edit: {
      featureGroup: this.drawnItems,
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
  
  this.map.addControl(this.draw);
  
  this.drawHandler = function (e) {
    var type = e.layerType, layer = e.layer;

    self.drawnItems.eachLayer(function (l) {
      self.drawnItems.removeLayer(l);
    });
    
    self.drawnItems.addLayer(layer);
    
    if(typeof self.props.onDraw === 'function') {
      self.props.onDraw.bind(self)(layer);
    }
  };
  
  this.map.on('draw:created', this.drawHandler);  
};

var _initializeHeatMap = function(data) {
	if(this.heat) {
		this.map.removeLayer(this.heat);
	}
	this.heat = L.heatLayer(data, {radius: 30, maxZoom: 11}).addTo(this.map);
};

var _initializeChoroPleth = function(geojson, colors, min, max) {
  var map = this.map;

  if(this.choropleth) {
    this.map.removeControl(this.choropleth.info);
    this.map.removeControl(this.choropleth.legend);
    this.map.removeLayer(this.choropleth.layer);
    
    delete this.choropleth;
  }

  if(geojson) {
    this.choropleth = {};
    
    var info = this.choropleth.info = L.control();

    info.onAdd = function (map) {
      this._div = L.DomUtil.create('div', 'map-choropleth-info');
      this.update();
      return this._div;
    };

    info.update = function (props) {
      this._div.innerHTML = (props ?
        '<b>' + props.label + '</b><br />' + props.value + ' lt'
        : 'Hover over an area');
    };

    info.addTo(this.map);

    if(min != max) {
      max = Math.ceil(max/1000) * 1000;
      if(max === 0) {
        max = 1000;
      }
      min = Math.floor(min/1000) * 1000;
      
      var step = Math.round((max - min) / colors.length);
      
      var getColor = function(d) {
        var index = Math.floor((d-min) / step);
        if(index == colors.lenth) {
          index--;
        }
        return colors[index];
      };
  
      var style = function(feature) {
        return {
          weight: 2,
          opacity: 1,
          color: 'white',
          dashArray: '3',
          fillOpacity: 0.7,
          fillColor: getColor(feature.properties.value)
        };
      };
  
      var overlays = this.overlays;

      var highlightFeature = function(e) {
        var layer = e.target;
  
        layer.setStyle({
          weight: 5,
          color: '#666',
          dashArray: '',
          fillOpacity: 0.7
        });
  
        if (!L.Browser.ie && !L.Browser.opera) {
          layer.bringToFront();
          for(var index in overlays){ 
            overlays[index].bringToFront();
          }
        }
  
        info.update(layer.feature.properties);
      };
   
      var layer;

      var resetHighlight = function(e) {
        layer.resetStyle(e.target);
        info.update();
      };
  
      var zoomToFeature = function(e) {
        map.fitBounds(e.target.getBounds());
      };
  
      var onEachFeature = function(feature, layer) {
        layer.on({
          mouseover: highlightFeature,
          mouseout: resetHighlight,
          click: zoomToFeature
        });
      };
  
      layer = this.choropleth.layer = L.geoJson(geojson, {
        style: style,
        onEachFeature: onEachFeature
      }).addTo(map);
  
      for(var index in this.overlays){ 
        this.overlays[index].bringToFront();
      }

      var legend = this.choropleth.legend = L.control({position: 'bottomright'});

      legend.onAdd = function (map) {
        var div = L.DomUtil.create('div', 'map-choropleth-info map-choropleth-legend'),
            from, to;
            
        for (var i = 0; i < colors.length; i++) {
          from = min + i * step;
          to = min + (i + 1) * step;
          if(to > max) {
            to = max;
          }
    
          div.innerHTML += '<i style="background:' + colors[i] + '"></i>' + 
          from + ' &ndash; ' + to + '<br>';
        }
        return div;
      };
    
      legend.addTo(this.map);
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
      _initializeChoroPleth.bind(this)(props.data, props.colors, props.min, props.max);
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
			overlays: []
    };
	},

	render: function() {
		var { prefix, center, zoom, mode, data, onDraw, colors, overlays, min, max, ...other } = this.props;

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
		
		if(this.props.overlays) {
		  var self = this;
		  
		  self.overlays = [];
		  
		  var callback = function( data, index ) {
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
          },
          onEachFeature: function onEachFeature(feature, layer) {
            if (feature.properties && feature.properties[self.props.overlays[index].popupContent]) {
              layer.bindPopup(feature.properties[self.props.overlays[index].popupContent]);
            }
          }
        }));
		    
		    self.overlays[self.overlays.length-1].addTo(self.map);
      };

      // Create closure to capture the index (declared here to make jshint happy...)
      var closureBuilder = function(index) {
        return function(data) {
          callback(data, index);
        };
      };

		  for(var index in this.props.overlays) {
		    $.getJSON(this.props.overlays[index].url, closureBuilder(index));
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
