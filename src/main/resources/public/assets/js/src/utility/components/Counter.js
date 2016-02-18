var React = require('react');
var ReactDOM = require('react-dom');

var FormattedMessage = require('react-intl').FormattedMessage;
var FormattedNumber = require('react-intl').FormattedNumber;

var Format = {
	Number : 'number',
	Currency : 'currency',
	Percent : 'percent',
	color: ''
};

var Counter = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	propTypes : {
		text : React.PropTypes.string,
		value : React.PropTypes.number,
		format: function(props, propName, componentName) {
			if (!/number|currency|percent/.test(props[propName])) {
				return new Error('Validation failed!');
			}
		},
		color: React.PropTypes.string
	},

	getDefaultProps: function() {
		return {
			text: '',
			value: 0,
			format: Format.Number,
			color: '#5bc0de'
	    };
	},

  	render: function() {
 		var content = null;

  		switch(this.props.format) {
  			case Format.Number:
  				content = ( <FormattedNumber value={this.props.value} /> );
  				break;
			case Format.Percent:
  				content = ( <FormattedNumber value={this.props.value} style='percent' /> );
  				break;
  			case Format.Currency:
  				content = ( <FormattedNumber value={this.props.value} style='currency' currency='EUR' /> );
  				break;
  			default: 
  				content = ( <span /> );
  				break;
  		}
  		return (
			<div style={{ 	borderRadiusRight: 3, 
							borderLeft: '5px solid ' + this.props.color}}>
				<div style={{ 	borderRadiusRight: 3, 
					borderLeft: '0px none !important', 
					border: '1px solid #F5F5F5', 
					padding: 10 }}>
					<FormattedMessage
						id={this.props.text}
					/>
					<h2>
						{content}
					</h2>
				</div>
			</div>
 		);
  	}
});

Counter = Counter;

Counter.Format = Format;

module.exports = Counter;
