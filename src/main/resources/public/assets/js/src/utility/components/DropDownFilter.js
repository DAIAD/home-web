var React = require('react');
var Bootstrap = require('react-bootstrap');

var DropDownFilter = React.createClass({   
	getDefaultProps: function() {
		return {
			title: '',
			options: [],
			onSelect: null,
			disabled: true
	    };
	},
	
	render: function(){
		var options = [];
		var self = this;

		this.props.options.forEach(function(value){
			var eventKey = {
					filter: value.filter,
					value: value.value,
					label: value.label,
					icon: self.props.icon
			};
			options.push(
					(
						<Bootstrap.MenuItem 
							eventKey={eventKey}
							key={value.key} 
							onSelect={self.props.onSelect}>
								{value.label}
						</Bootstrap.MenuItem>
					)
			);
		});
		
		return (
			<div className='clearfix' style={{float: 'left', marginLeft: 5, paddingLeft: 5}}>
				<Bootstrap.DropdownButton
					title={this.props.title}
					disabled={self.props.disabled} 
					id="bg-nested-dropdown">
			      {options}
			    </Bootstrap.DropdownButton>
			</div>
		);
	}
});

module.exports = DropDownFilter;