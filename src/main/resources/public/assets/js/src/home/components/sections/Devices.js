var React = require('react');
var bs = require('react-bootstrap');
var connect = require('react-redux').connect;
var injectIntl = require('react-intl').injectIntl;
var { FormattedMessage } = require('react-intl');

var MainSection = require('../MainSection');


function Device (props) {
  const _t = props.intl.formatMessage;
  return (
    <div className="col-xs-5" >
      
      <bs.Input type="text" label={_t({id:"devices.name"})} defaultValue={props.name} />
      <bs.Input type="text" label={_t({id:"devices.key"})} defaultValue={props.deviceKey} readOnly={true} />
        {(() => {
          if (props.type === 'AMPHIRO') {
            return (
              <bs.Input type="text" label={_t({id:"devices.mac"})} defaultValue={props.macAddress} readOnly={true} />
              
              );
          }
          else if (props.type === 'METER') {
            return (
              <bs.Input type="text" label={_t({id:"devices.serial"})} defaultValue={props.serial} readOnly={true} />
              );
          }
        })()}
        <hr />  
        <h4><FormattedMessage id="devices.properties" /></h4>
          {
            props.properties.map(function(property){
              if (!property.key){
                return (<div/>);
              }
              return (
                <bs.Input key={property.key} type="text" label={_t({id:`devices.${property.key}`})} defaultValue={property.value} readOnly={true} />
                );
              })
          }
    </div>
  );
}


function DevicesForm (props) {
  const { intl, devices } = props;
  const _t = intl.formatMessage;
  return (
    <form>
      <bs.Accordion className="col-xs-10">
        {
          devices.map(function(device, i){
            return (
              <bs.Panel key={device.deviceKey}
                header={device.name || device.deviceKey}
                eventKey={i}>
                <Device {...device} intl={intl}/>
              </bs.Panel>
              );
          }.bind(this))
        }
        <bs.ButtonInput style={{marginTop: "20px"}} type="submit" value={_t({id:"forms.submit"})} onClick={(e) => { e.preventDefault(); }} />
      </bs.Accordion>
    </form>
  );
}


var Devices = React.createClass({

  render: function() {
    return (
      <MainSection id="section.devices">
        <br/>
        <DevicesForm {...this.props} /> 
      </MainSection>
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
