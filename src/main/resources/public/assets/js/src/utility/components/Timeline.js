var React = require('react');
var Bootstrap = require('react-bootstrap');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');

var _refresh = function() {
	var value;
	
	if(this.state.value === this.props.max) {
		value = this.props.min;
	} else {
		value = this.state.value + 1;
	}
	
	this.setState({value: value});
	
	if(typeof this.props.onChange === 'function') {
		this.props.onChange(parseInt(this.refs.slider.value));
	}
};

var _play = function() {
	var timeout = null;

	if(this.state.playing) {
		if(this.state.timeout) {
			clearInterval(this.state.timeout);
		}
	} else {
		timeout = setInterval(_refresh.bind(this), 2000);
	}
	
	this.setState({
		playing : !this.state.playing,
		timeout: timeout
	});
};

var _move = function(value, e) {
	this.setState({ value: parseInt(this.refs.slider.value)});
	
	if(typeof this.props.onChange === 'function') {
		this.props.onChange(parseInt(this.refs.slider.value));
	}
};

var Timeline = React.createClass({
    getInitialState: function() {
        return {
            playing: false,
            value: null,
            timeout: this.props.value
        };
    },

    componentWillMount : function() {
		this.setState({value : this.props.value});
	},

    getDefaultProps: function() {   	
        return {
            min: 1,
            max: 10,
            data: [],
            value: 1,
            type: 'string'
        };
    },

    getLabel: function() {
    	if((this.props.data) && (this.props.value >=1) && (this.props.value <= this.props.data.length)) {
    		var value = this.props.data[this.state.value-1];
    		switch(this.props.type) {
				case 'date':
					return (<FormattedDate value={value} day='numeric' month='long' year='numeric' />);
				case 'time':
	  				return (<FormattedTime value={value} hour='numeric' minute='numeric' />);
				default:
					return (<span>{value}</span>);
    		}
    	}
    	return (<span />);
    },

    render: function() {
    	var {onChange, ...others} = this.props;
        return (
        	<div className='row' {...others}>
        		<div className='col-md-3'>
		        	<Bootstrap.Button	bsStyle='default' className='btn-circle' style={{ marginRight: 10}}>
						<i className='fa fa-backward fa-fw'></i>
					</Bootstrap.Button>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' style={{ marginRight: 10}} onClick={_play.bind(this)}>
						<i className={'fa fa-' + (this.state.playing ? 'pause' : 'play') + ' fa-fw'}></i>
					</Bootstrap.Button>
					<Bootstrap.Button	bsStyle='default' className='btn-circle'>
						<i className='fa fa-forward fa-fw'></i>
					</Bootstrap.Button>
				</div>
				<div className='col-md-3' style={{ paddingTop: 6}}>{this.getLabel()}</div>
				<div className='col-md-6' style={{ paddingTop: 5}}>
    				<input type ="range" min={this.props.min} max={this.props.max} step ="1" value={this.state.value} ref='slider' onChange={_move.bind(this)}/>
    			</div>
			</div>
        );
    }
});

module.exports = Timeline;
