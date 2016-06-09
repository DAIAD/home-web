
var numeral = require('numeral');
var React = require('react');
var {Glyphicon} = require('react-bootstrap');

var PropTypes = React.PropTypes;

var MeasurementValue = React.createClass({
  
  propTypes: {
    title: PropTypes.string.isRequired,
    subtitle: PropTypes.string,
    field: PropTypes.string.isRequired,
    unit: PropTypes.string.isRequired,
    value: PropTypes.number,
    prevValue: PropTypes.number,
    // Appearence
    formatter: PropTypes.func,

  },

  getDefaultProps: function () {
    return {
      formatter: (y) => (numeral(y).format('0.0a')),
    };
  },

  render: function () {
    var {title, subtitle, unit, value, prevValue, formatter: f} = this.props;

    var dy, decr; 
    if (value) {
      dy = value - prevValue; 
      decr = (dy < 0);
    }
    
    return (
      <div className="measurement-value">
        <div className="title">{title}</div>
        <div className="subtitle">{subtitle}</div>
        <div className="current-value">
          <span className="value">{value? f(value) : '--.-'}</span>
          &nbsp;
          <span className="unit">{unit}</span>
        </div>
        <div className="delta" style={{display: (dy != null)? 'block' : 'none' }}>
          <span className={'sign' + ' ' + (decr? 'negative' : 'non-negative')}>
            <Glyphicon glyph={decr? 'arrow-down' : 'arrow-up'} />
          </span>
          &nbsp;
          <span className="value">{(dy != null)? f(Math.abs(dy)) : ''}</span>
          &nbsp;
          <span className="unit">{unit}</span>
        </div>
      </div>
    ); 
  } 
}); 

module.exports = MeasurementValue;
