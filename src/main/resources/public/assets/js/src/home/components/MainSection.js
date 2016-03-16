var React = require('react');
var FormattedMessage = require('react-intl').FormattedMessage;

function MainSection (props) {
  return (
    <section className="main-section" >
      <div className={props.id}>
        <div className="container">
            {props.children}
          </div>
        </div>
      </section>
  );
}

module.exports = MainSection;
