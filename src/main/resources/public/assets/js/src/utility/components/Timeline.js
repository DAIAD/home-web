var React = require('react');
var Bootstrap = require('react-bootstrap');

var _refresh = function() {
	var index;
	
	if(this.state.index === (this.props.values.length - 1)) {
		index = 0;
	} else {
		index = this.state.index + 1;
	}
	
	this.setState({index: index});
	
	if(typeof this.props.onChange === 'function') {
	  this.props.onChange(this.props.values[index], this.props.labels[index], index);
	}
};

var _play = function() {
	var timeout = null;

	if(this.state.animate) {
		if(this.state.timeout) {
			clearInterval(this.state.timeout);
		}
	} else {
		timeout = setInterval(_refresh.bind(this), this.props.speed);
	}
	
	this.setState({
	  animate : !this.state.animate,
		timeout: timeout
	});
};

var _click = function() {
  if(this.state.animate){
    _play.bind(this)();
  }
};

var _move = function(value, e) {
  var index = parseInt(this.refs.slider.value) -1;
  
	this.setState({ 
	  index: index
  });
	
	if(typeof this.props.onChange === 'function') {
	  this.props.onChange(this.props.values[index], this.props.labels[index], index);
	}
};

var Timeline = React.createClass({
  getInitialState: function() {
    return {
      index: null,
      animate: false,
      timeout: this.props.value
    };
  },

  componentWillMount : function() {
		this.setState({
		  index: this.props.defaultIndex
		});
	},

  getDefaultProps: function() {   	
    return {
      labels: [],
      values: [],
      defaultIndex: 0,
      speed: 1000
    };
  },

  getLabel: function() {
    var index = this.state.index;

  	if((this.props.labels) && (index >=0) && (index < this.props.labels.length)) {
		  return (
	      <span>
	        {this.props.labels[index]}
        </span>
      );
  	}
  	return (<span />);
  },

  render: function() {
  	var {onChange, ...others} = this.props;

    return (
    	<div className='row' {...others}>
    	    <div style={{float: 'left', marginLeft: 12 }}>
     	      <Bootstrap.Button	bsStyle='default' className='btn-circle' style={{ marginRight: 10}} onClick={_play.bind(this)}>
    	        <i className={'fa fa-' + (this.state.animate ? 'pause' : 'play') + ' fa-fw'}></i>
  	        </Bootstrap.Button>
        </div>
			  <div style={{ float: 'left', paddingTop: 6}}>
			    {this.getLabel()}
			  </div>
		    <div className='col-md-6' style={{ float: 'right', paddingTop: 5}}>
				  <input type ="range" min={1} 
			                         max={this.props.labels.length}
				                       step ={1}
			                         value={this.state.index+1}
				                       ref='slider'
			                         onChange={_move.bind(this)}
				                       onClick={_click.bind(this)}
				  />
			  </div>
		  </div>
    );
  }
});

module.exports = Timeline;
