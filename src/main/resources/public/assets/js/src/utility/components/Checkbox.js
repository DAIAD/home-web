var React = require('react');

var Checkbox = React.createClass({
	getDefaultProps: function() {
		return {
			disabled: false,
			text: '',
			checked: false
	    };
	},
	
  	render: function() {
  		if(this.props.text) {
	  		return (
				<div className='checkbox c-checkbox'>
					<label>
						<input type='checkbox' disabled={this.props.disabled ? "disabled" : false} defaultChecked={this.props.checked}/>
							<span className='fa fa-check' style={{ marginRight: 30 }}></span>
							{' ' + this.props.text}
					</label>
				</div>
	 		);
  		}
  		return (
			<div className='checkbox c-checkbox'>
				<label>
					<input type='checkbox' disabled={this.props.disabled ? "disabled" : false} defaultChecked={this.props.checked}/>
						<span className='fa fa-check' style={{ marginRight: 0 }}></span>
				</label>
			</div>
 		);
  	}
});

module.exports = Checkbox;
