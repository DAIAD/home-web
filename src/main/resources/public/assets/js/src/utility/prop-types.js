
var React = require('react');

var population = require('./population');
var TimeSpan = require('./timespan');

var PropTypes = React.PropTypes;

// A collection of commonly used (component) prop types

var populationPropType = PropTypes.oneOfType([
  PropTypes.instanceOf(population.Group),
  PropTypes.instanceOf(population.Cluster),
]);

var seriesPropType = PropTypes.shape({
  population: populationPropType,
  metric: PropTypes.string,
  source: PropTypes.string,
  ranking: PropTypes.object,
  data: PropTypes.arrayOf(
    PropTypes.arrayOf(PropTypes.number)
  ),
});

var timespanPropType = PropTypes.oneOfType([
  PropTypes.oneOf(TimeSpan.commonNames()),
  (props, propName, componentName) => ( 
    (PropTypes.arrayOf(PropTypes.number)(props, propName, componentName)) ||
    ((props[propName].length == 2)? null : (
      new Error(propName + ' should be an array of length 2')))
  ),
]);

module.exports = {
  timespanPropType,
  populationPropType,
  seriesPropType,
};


