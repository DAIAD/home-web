var React = require('react');

var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');

var Bootstrap = require('react-bootstrap');

var FilterTag = React.createClass({   
    getDefaultProps: function() {
        return {
            text: '',
            icon: '',
            onClick: null
        };
    },

    render: function() { 
        return (
    		<div className='clearfix' style={{ float: 'left', margin: 5, padding: 5, borderRadius: 2, background: '#2c3e50'}} >
        		<i className={'fa fa-' + this.props.icon} style={{ color : '#fff', paddingRight: 5 }}></i>
            	<span style={{ color: '#fff', cursor: 'pointer', textDecoration: 'underline' }} onClick={this.props.onClick}>{this.props.text}</span>
    		</div>
        );
    }
});

module.exports = FilterTag;
