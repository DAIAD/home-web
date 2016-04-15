var React = require('react');

var IndeterminateCheckbox = React.createClass({
	
	
	getDefaultProps: function() {
		return {
			propertyName: '',
			disabled: false,
			checked: true,
			action: function(){}
		};
	},
	
	getInitialState: function() {
		return {
			mode: 'indeterminate',
			checked: true
    	};
	},
	
	getNextMode: function(currentMode){
		switch(currentMode){
		case 'indeterminate':
			return 'selectAll';
		case 'selectAll':
			return 'unSelectAll';
		case 'unSelectAll':
			return 'indeterminate';
		}
	},
	
	shiftMode: function(){
		switch(this.state.mode) {
		case 'indeterminate':
			this.setState({ mode : this.getNextMode(this.state.mode), checked: true});
			break;
		case 'selectAll':
			this.setState({ mode : this.getNextMode(this.state.mode), checked: false});
			break;
		case 'unSelectAll':
			this.setState({ mode : this.getNextMode(this.state.mode), checked: true});
			break;
		}
	},
	
	onChange: function(){
		var mode = this.getNextMode(this.state.mode);
		this.shiftMode();
		this.props.action(this.props.propertyName, mode);
	},
	
  	render: function() {
  		var classNames = 'checkbox c-checkbox';
  		  		
  		var checked, symbol;  		
  		switch(this.state.mode){
	  		case 'indeterminate':
	  			symbol = 'fa-minus';
				break;
			case 'selectAll':
	  			symbol = 'fa-check';
				break;
			case 'unSelectAll':
	  			symbol = '';
				break;
  		}
  		
  		return (
			<div className={classNames}>
				<label>
					<input type='checkbox' 
						disabled={this.props.disabled ? "disabled" : false} checked={this.state.checked}
						ref="checkbox"
						onChange={this.onChange}/>
						
						<span className={'fa ' + symbol} style={{ marginRight: 0 }}></span>
				</label>
			</div>
 		);
  	}
});

module.exports = IndeterminateCheckbox;
