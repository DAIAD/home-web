
var _ = require('lodash');

var React = require('react');
var Bootstrap = require('react-bootstrap');

var {Button, ButtonGroup, ButtonToolbar, Tooltip, OverlayTrigger} = Bootstrap;
var PropTypes = React.PropTypes;

var buttonPropType = PropTypes.shape({
  key: PropTypes.string,
  text: PropTypes.string,
  tooltip: tooltipPropType,
  iconName: PropTypes.string,
  buttonProps: PropTypes.object, // props forwarded to Bootstrap.Button
});

var tooltipPropType = PropTypes.shape({
  id: PropTypes.string,
  message: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  placement: PropTypes.oneOf(['top', 'bottom', 'left', 'right']),
});

var randomString = (dd=9) => (
  parseInt(Math.random() * Math.pow(10, dd)).toString(36)
);

var _ButtonGroup = React.createClass({

  propTypes: {
    id: PropTypes.string,
    className: PropTypes.string,
    component: PropTypes.oneOfType([PropTypes.func, PropTypes.string]), 
    buttons: PropTypes.arrayOf(buttonPropType),
    onSelect: PropTypes.func.isRequired,
  },
  
  getDefaultProps: function () {
    return {
      component: ButtonGroup,
    };
  }, 

  getInitialState: function () {
    return {
      id: this.props.id || ('button-group-' + randomString()),
    };
  },

  render: function () {    
    var Wrapper = this.props.component;
    var wrapperProps = {id: this.state.id};
    if (this.props.className)
      wrapperProps.className = this.props.className;
    
    var buttons = this.props.buttons.map((spec) => {
      var icon = !spec.iconName? 
        null : (<i className={"fa fa-fw" + ' ' + ('fa-' + spec.iconName)}></i>);
      
      var button = (
        <Button key={spec.key} {...spec.buttonProps} onClick={() => this.props.onSelect(spec.key)}>
          {icon}{!spec.text? null : ((icon? ' ' : '') + spec.text)}
        </Button>
      );
      
      if (spec.tooltip) {
        var tooltip = (
          <Tooltip id={['tooltip', this.state.id, spec.key].join('--')}>
            {spec.tooltip.message}
          </Tooltip>
        );
        var triggerProps = {
          key: spec.key,
          placement: spec.tooltip.placement || 'bottom',
          overlay: tooltip,
        };
        return (<OverlayTrigger {...triggerProps}>{button}</OverlayTrigger>);
      } else {
        return button;
      }
    });
    
    return React.createElement(Wrapper, wrapperProps, buttons);
  },
});

_ButtonGroup.displayName = 'toolbars.ButtonGroup';

var _ButtonToolbar = React.createClass({
  
  statics: {
    defaults: {
      groupComponent: ButtonGroup,
    },
  },

  propTypes: {
    id: PropTypes.string,
    className: PropTypes.string,
    component: PropTypes.oneOfType([PropTypes.func, PropTypes.string]), 
    groups: PropTypes.arrayOf(PropTypes.shape({
      component: PropTypes.oneOfType([PropTypes.func, PropTypes.string]),
      key: PropTypes.string,
      buttons: PropTypes.arrayOf(buttonPropType)
    })),
    onSelect: PropTypes.func.isRequired,
  },
  
  getDefaultProps: function () {
    return {
      component: ButtonToolbar,
    };
  }, 

  getInitialState: function () {
    return {
      id: this.props.id || ('button-toolbar-' + randomString()),
    };
  },

  render: function () {
    var {defaults} = this.constructor;
    
    var Wrapper = this.props.component; 
    var wrapperProps = {id: this.state.id};
    if (this.props.className)
      wrapperProps.className = this.props.className;
    
    var buttonGroups = this.props.groups
      .filter(spec => spec.buttons.length > 0)
      .map((spec) => (
        <_ButtonGroup key={spec.key}
          id={this.state.id + '-group'}
          component={spec.component || defaults.groupComponent}
          buttons={spec.buttons}
          onSelect={(key) => (this.props.onSelect(spec.key, key))}
        />
      ));

    return React.createElement(Wrapper, wrapperProps, buttonGroups);
  },  

});

_ButtonToolbar.displayName = 'toolbars.ButtonToolbar';

module.exports = {
  ButtonGroup: _ButtonGroup,
  ButtonToolbar: _ButtonToolbar,
};
