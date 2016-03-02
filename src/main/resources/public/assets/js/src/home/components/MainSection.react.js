var React = require('react');
var FormattedMessage = require('react-intl').FormattedMessage;

var MainSection = React.createClass({
	propTypes: {
		id: React.PropTypes.string,
		className: React.PropTypes.string
	},
	render: function() {
		return (
			<section className="main-section" >
				<div className={this.props.id}>
					<div className="container">
            {
              //<h2><FormattedMessage id={this.props.id} /></h2>
             }
							{this.props.children}
						</div>
					</div>
				</section>
		);
	}
});

module.exports = MainSection;
