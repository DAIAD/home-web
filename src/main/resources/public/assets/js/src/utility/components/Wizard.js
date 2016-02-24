var React = require('react');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');

var Bootstrap = require('react-bootstrap');

var Wizard = React.createClass({
	getInitialState: function() {
		return {
			activeStep: 0
    	};
	},

    getDefaultProps: function() {
        return {
            steps: []
        };
    },

	onStepIndexChange(activeStep) {
		this.setState({
			activeStep: activeStep
    	});
	},
	  
  	render: function() { 		
  		return (
			<div className='clearfix' style={{ margin: 10}}>
				<Header steps={this.props.steps} selected={this.state.activeStep} onSelect={this.onStepIndexChange}/>
				<Step component={this.props.steps[this.state.activeStep].component} options={this.props.steps[this.state.activeStep].props} />
			</div>
 		);
  	}
});

var Header = React.createClass({

	render: function() {	
  		var items = this.props.steps.map(function(s, index) {
			return (<Bootstrap.NavItem key={index} eventKey={index} href='#'>{s.text}</Bootstrap.NavItem>); 			
  		});
  		
  		return (
			<Bootstrap.Nav bsStyle="tabs" activeKey={this.props.selected} onSelect={this.props.onSelect}>
				{items}
			</Bootstrap.Nav>
 		);
  	}
});

var Step = React.createClass({
  	render: function() {	
  		return (
			<this.props.component {...this.props.options}/>
 		);
  	}
});


Wizard.Step = Step;

module.exports = Wizard;
