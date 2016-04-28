var React = require('react');

function Footer (props) {
  return (
    <footer className="site-footer">
      <div className="container">
        <ul style={{listStyle: 'none', textAlign: 'center'}}>
          <li style={{display: 'inline-block'}}>
            <a href="#">2016 DAIAD</a>
          </li>
          <li style={{marginLeft: 20, display: 'inline-block'}}>
            <a href="http://daiad.eu">daiad.eu</a>
          </li>
          <li style={{marginLeft: 20, display: 'inline-block'}}>
            <a href="#">Terms of service</a>
          </li>
          <li style={{marginLeft: 20, display: 'inline-block'}}>
            <a href="#">Send feedback</a>
          </li>
        </ul>
      </div>
    </footer>
  );
}

module.exports = Footer;
