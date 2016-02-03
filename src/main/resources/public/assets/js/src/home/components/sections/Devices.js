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
	getInitialState: function() {
		return {
			name:this.props.name 
		};
	},	
	render: function() {
		return (
			<div className="col-xs-5" >
				
				<bs.Input type="text" label="Device Name" value={this.state.name} onChange={this._onChange} ref="name" />
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
						<bs.ButtonInput style={{marginTop: "20px"}} type="submit" value="Submit" onClick={this._onSubmit} />
			</div>
		);
	},
	_onSubmit: function(e) {
		e.preventDefault();
		var deviceKey = this.props.deviceKey;
		var name = this.refs.name.getValue();

		var profile = assign({}, this.props.profile);

		this.props.profile.devices.forEach(function(device, i) {
			if (deviceKey === device.deviceKey) {
				device.name = name;
				profile.devices[i] = device;
			}
		}.bind(this));

		HomeActions.updateProfile(profile);
	},
	_onChange: function() {
		this.setState({
			name: this.refs.name.getValue()
		});
	},
});

var DevicesForm = React.createClass({
	componentDidMount: function() {
		UserStore.addProfileUpdateListener(this._onUpdate);
	},
	componentWillUnmount: function() {
		UserStore.removeProfileUpdateListener(this._onUpdate);
	},
	render: function() {
		var handleSetName = this.handleSetName;
		var profile = this.props.profile;
		return (
			<form>
				<bs.Accordion className="col-xs-10">
					{
						this.props.profile.devices.map(function(device, i){
							return (
								<bs.Panel key={device.deviceKey}
									header={device.name || device.deviceKey}
									eventKey={i}>
									<Device {...device} profile={profile}/>
								</bs.Panel>
								);
						})
					}
				</bs.Accordion>
			</form>
		);
	},
	_onUpdate: function() {
		this.forceUpdate();
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
