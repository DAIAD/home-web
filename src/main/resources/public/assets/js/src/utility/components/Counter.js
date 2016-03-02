var React = require('react');
var ReactDOM = require('react-dom');
var { Link } = require('react-router');
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
		color: React.PropTypes.string,
		variance: React.PropTypes.number,
		minusColor: React.PropTypes.string,
		plusColor: React.PropTypes.string,
		link: React.PropTypes.string
	},

	getDefaultProps: function() {
		return {
			text: '',
			value: 0,
			format: Format.Number,
			color: '#5bc0de',
			variance: 0,
			minusColor: '#c0392b',
			plusColor: '#27ae60',
			link: ''
	    };
	},

  	render: function() {
 		var content, variance, footer;

  		if(this.props.variance !== 0) {
  			if(this.props.variance > 0) {
  				variance = ( 
					<span style={{ fontSize: 14 }}>
						<i className='fa fa-arrow-up fa-fw' style={{ color: this.props.plusColor }}></i>
						<span style={{ color: this.props.plusColor }}>
							<FormattedNumber value={Math.abs(this.props.variance)} />
						</span>
						<span> since last week</span>
					</span>);
  			} else {
  				variance = (
					<span style={{ fontSize: 14 }}>
						<i className='fa fa-arrow-down fa-fw' style={{ color: this.props.minusColor }}></i>
						<span style={{ color: this.props.minusColor }}>
							<FormattedNumber value={Math.abs(this.props.variance)} />
						</span>
						<span> since last week</span>
					</span>);
  			}
  		}
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

  		if(this.props.link) {
  			footer = (
				<h6 className='text-right' style={{ margin: 0 }}>
					<Link to={this.props.link}>See more...</Link>
				</h6>
			);
  		}
  		return (
			<div style={{ 	borderRadiusRight: 3, 
							borderLeft: '5px solid ' + this.props.color}}>
				<div style={{ 	borderRadiusRight: 3, 
					borderLeft: '0px none !important', 
					border: '1px solid #F5F5F5', 
					padding: 10 }}>
					<h3 style={{ marginTop: 0 }}>
						<FormattedMessage
							id={this.props.text}
						/>
					</h3>
					<h4>
						{content}
						{variance}
					</h4>
					{footer}
				</div>
			</div>
 		);
  	}
});

Counter.Format = Format;

module.exports = Counter;
