var React = require('react');
var bs = require('react-bootstrap');
var assign = require('object-assign');

var UserStore = require('../../stores/UserStore');

// Actions
var HomeActions = require('../../actions/HomeActions');

function capitalize(word) {
		return word.charAt(0).toUpperCase() + word.slice(1);
}

var Device = React.createClass({
	render: function() {
		return (
			<div className="col-xs-5" >
				
				<bs.Input type="text" label="Device Name" value={this.props.name} onChange={this._onChange} ref="name" />
				<bs.Input type="text" label="Device Key" value={this.props.deviceKey} readOnly={true} />
					{(() => {
						if (this.props.type === 'AMPHIRO') {
							return (
								<bs.Input type="text" label="Mac Address" value={this.props.macAddress} readOnly={true} />
								
								);
						}
						else if (this.props.type === 'METER') {
							return (
								<bs.Input type="text" label="serial" value={this.props.serial} readOnly={true} />
								);
						}
					})()}
					<hr />	
					<h4>Properties</h4>
						{
							this.props.properties.map(function(property){
								return (
									<bs.Input key={property.key} type="text" label={capitalize(property.key)} value={property.value} readOnly={true} />
									);
								})
						}
			</div>
		);
	},
	_onChange: function() {
		this.props.handleSetName(this.props.deviceKey, this.refs.name.getValue());
	},
});

var DevicesForm = React.createClass({
	getInitialState: function() {
		return {
			devices:this.props.profile.devices 
		};
	},
	handleSetName: function(deviceKey, name) {
		if (!deviceKey || !name){
			return;
		}

		this.props.profile.devices.forEach(function(device, i) {
			if (deviceKey === device.deviceKey) {
				var devices = this.props.profile.devices;
				device.name = name;
				devices[i] = device;
				
				this.setState({
					devices: devices
				});
			}
		}.bind(this));

	},
	_onSubmit: function(e) {
		e.preventDefault();
		var profile = assign({}, this.props.profile);
		profile.devices = this.state.devices;
		HomeActions.updateProfile(profile);
	},
	render: function() {
		var handleSetName = this.handleSetName;
		return (
			<form>
				<bs.Accordion className="col-xs-10">
					{
						this.props.profile.devices.map(function(device, i){
							return (
								<bs.Panel key={device.deviceKey}
									header={device.name || device.deviceKey}
									eventKey={i}>
									<Device {...device} handleSetName={handleSetName} />
								</bs.Panel>
								);
						})
					}
				<bs.ButtonInput style={{marginTop: "20px"}} type="submit" value="Submit" onClick={this._onSubmit} />
				</bs.Accordion>
			</form>
		);
	}
});

var DevicesSection = React.createClass({
	render: function() {
		return (
			<div className="section-devices">
				<h3>Devices</h3>
				<DevicesForm profile={UserStore.getProfile()}/>	
			</div>
		);
	}
});

module.exports = DevicesSection;
