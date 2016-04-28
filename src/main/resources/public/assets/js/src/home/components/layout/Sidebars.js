var React = require('react');

function SidebarLeft (props) {
  return (
    <div className="sidebar-left" style={props.width?{width:props.width}:{}}>
      { props.children }
    </div>
  );
}

function SidebarRight (props) {
  return (
    <div className="sidebar-right" style={props.width?{width:props.width}:{}}>
      { props.children }
    </div>
  );
}
module.exports = { SidebarLeft, SidebarRight };
