var React = require('react');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var Bootstrap = require('react-bootstrap');

var DismissableFilterTag = React.createClass({   
    
	getDefaultProps: function() {
        return {
            text: '',
            icon: '',
            filter: ''
        };
    },
    
    handleDFTagClose: function() {
    	this.props.onSelect(this.props.filter);
    },
    
    render: function() { 
        return (
    		<div className='clearfix' key={this.props.key} style={{ float: 'left', margin: 5, padding: 5, borderRadius: 2, background: '#2c3e50'}} >
        		<i className={'fa fa-' + this.props.icon} style={{ color : '#fff', paddingRight: 5 }}></i>
            	<span style={{ color: '#fff' }} >{this.props.text}</span>
            	<i className={'fa fa-times'} style={{ color : '#fff', cursor: 'pointer',  paddingLeft: 5 }} onClick={this.handleDFTagClose}></i>
    		</div>
        );
    }
});

module.exports = DismissableFilterTag;