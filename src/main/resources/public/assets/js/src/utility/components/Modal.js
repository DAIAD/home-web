var React = require('react');
var Bootstrap = require('react-bootstrap');

var Modal = React.createClass({
	
	render: function(){
		
		var actions = [];
		this.props.actions.forEach(function (action, i){
			var button;
			if(action.style){
				button = (<Bootstrap.Button bsStyle={action.style} key={action.name} onClick={action.action}>{action.name}</Bootstrap.Button>);
			} else {
				button = (<Bootstrap.Button key={action.name} onClick={action.action}>{action.name}</Bootstrap.Button>);
			}
			actions.push((
				button
			));
		});
					
		return (
			<Bootstrap.Modal animation={false} show={this.props.show} onHide={this.props.onClose}>
				<Bootstrap.Modal.Header closeButton>
					<Bootstrap.Modal.Title>{this.props.title}</Bootstrap.Modal.Title>
				</Bootstrap.Modal.Header>
				<Bootstrap.Modal.Body>
					{this.props.text}
				</Bootstrap.Modal.Body>
				<Bootstrap.Modal.Footer>
					{actions}
				</Bootstrap.Modal.Footer>
			</Bootstrap.Modal>
		);
	}
});

module.exports = Modal;
