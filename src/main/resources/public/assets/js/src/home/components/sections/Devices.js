var React = require('react');
var bs = require('react-bootstrap');

var UserStore = require('../../stores/UserStore');

// Actions
var HomeActions = require('../../actions/HomeActions');

function capitalize(word) {
		return word.charAt(0).toUpperCase() + word.slice(1);
}

var Device = React.createClass({
	/*getInitialState: function() {
		return {
			name: this.props.name 
		};
		},*/
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
		/*this.setState({
			name: this.refs.name.getValue()
			});
			*/
		this.props.handleSetName(this.props.deviceKey, this.refs.name.getValue());
	},
	_onClick: function() {
		return;
	}
});

var DevicesForm = React.createClass({
	getInitialState: function() {
		return {
			profile: UserStore.getProfile() 
		};
	},
	handleSetName: function(deviceKey, name) {
		var profile = this.state.profile;
		if (!deviceKey.length || !name.length){
			return;
		}
		var self = this;
		
		profile.devices.forEach(function(device, i) {
			if (deviceKey === device.deviceKey) {
				device.name = name;
				profile.devices[i] = device;
				
				self.setState({
					profile: profile
				});
			}
		});
		//TODO: bind(this) doesnt work
		//.bind(this);

	},
	_onSubmit: function(e) {
		e.preventDefault();
		HomeActions.updateProfile(this.state.profile);
	},
	//_onChange: function() {
	//		var profile = this.state.profile;
		//profile.firstname = this.refs.firstname.getValue();
		//profile.lastname = this.refs.lastname.getValue();
	//	this.setState({
	//		profile: profile
	//	});
	//},
	render: function() {
		var profile = UserStore.getProfile();
		var handleSetName = this.handleSetName;
		return (
			<form>
				<bs.Accordion className="col-xs-10">
					{
						profile.devices.map(function(device, i){
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
var Devices = React.createClass({
	render: function() {
		return (
			<div className="section-devices">
				<h3>Devices</h3>
				<DevicesForm />	
			</div>
		);
	}
});

module.exports = Devices;
