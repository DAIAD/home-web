var React = require('react');
var bs = require('react-bootstrap');

var injectIntl = require('react-intl').injectIntl;
var connect = require('react-redux').connect;

// Actions
//var HomeActions = require('../../actions/HomeActions');

function capitalize(word) {
		return word.charAt(0).toUpperCase() + word.slice(1);
}

var Device = React.createClass({

	render: function() {
		return (
			<div className="col-xs-5" >
				
				<bs.Input type="text" label="Device Name" defaultValue={this.props.name} ref="name" />
				<bs.Input type="text" label="Device Key" defaultValue={this.props.deviceKey} readOnly={true} />
					{(() => {
						if (this.props.type === 'AMPHIRO') {
							return (
								<bs.Input type="text" label="Mac Address" defaultValue={this.props.macAddress} readOnly={true} />
								
								);
						}
						else if (this.props.type === 'METER') {
							return (
								<bs.Input type="text" label="serial" defaultValue={this.props.serial} readOnly={true} />
								);
						}
					})()}
					<hr />	
					<h4>Properties</h4>
						{
							this.props.properties.map(function(property){
								return (
									<bs.Input key={property.key} type="text" label={capitalize(property.key)} defaultValue={property.value} readOnly={true} />
									);
								})
						}
			</div>
		);
	},
});

var DevicesForm = React.createClass({
	
	_onSubmit: function(e) {
		e.preventDefault();
		//HomeActions.updateProfile(this.state.profile);
	},

	render: function() {
		var devices = this.props.devices;
		return (
			<form>
				<bs.Accordion className="col-xs-10">
					{
						devices.map(function(device, i){
							return (
								<bs.Panel key={device.deviceKey}
									header={device.name || device.deviceKey}
									eventKey={i}>
									<Device {...device} />
								</bs.Panel>
								);
						})
					}
				<bs.ButtonInput style={{marginTop: "20px"}} type="submit" value="Update" onClick={this._onSubmit} />
				</bs.Accordion>
			</form>
		);
	}
});

var Devices = React.createClass({

	render: function() {
		return (
			<div className="section-devices">
				<h3>Devices</h3>
				<DevicesForm {...this.props} />	
			</div>
		);
	}
});

function mapStateToProps(state) {
	return {
		devices: state.user.profile.devices
	};
}

Devices = connect(mapStateToProps)(Devices);
Devices = injectIntl(Devices);
module.exports = Devices;
