var React = require('react');

var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');

var Bootstrap = require('react-bootstrap');
var Checkbox = require('./Checkbox');

var Tag = {};

Tag.Item = React.createClass({
    getInitialState: function() {
        return {
            activePage: 0
        };
    },
    
    getDefaultProps: function() {
        return {
            text: '',
            actions: [],
            checked: false
        };
    },

    render: function() { 
    	var self = this;

    	var actions = this.props.actions.map(function(a, index) {
    		return (
				<div key={index} style={{ float: 'left', padding: '0px 4px 0px 4px' }}>
					<i 	className={'fa fa-' + a.icon}
						style={{ cursor: 'pointer' }}
						onClick={a.handler.bind(self)}>
					</i>
				</div>
			);
    	});

        return (
        	<div style={{ float: 'left', margin: 10, border: '1px solid #7f8c8d', borderRadius: 5}}>
        		<div style={{ float: 'left', padding: 10}}>
	            	<div>
	            		<Checkbox checked={this.props.checked} />
	            		{this.props.text}
	            	</div>
	            </div>
	            <div style={{ float: 'left', padding: 10, borderLeft: '1px solid #7f8c8d'}}>
	    			{actions}
	    		</div>
    		</div>
        );
    }
});

Tag.Collection = React.createClass({
    contextTypes: {
        intl: React.PropTypes.object
    },
    
    getDefaultProps: function() {
        return {
            tags: []
        };
    },
    render: function() {    
        var _t = this.context.intl.formatMessage;
      
        return (
                <div className='clearfix'>
                	{this.props.children}
                </div>
        );
    }
});

module.exports = Tag;
