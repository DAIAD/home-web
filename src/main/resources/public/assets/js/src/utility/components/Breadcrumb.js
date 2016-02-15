var React = require('react');
var ReactDOM = require('react-dom');
var { Link } = require('react-router');
var FormattedMessage = require('react-intl').FormattedMessage;
var FormattedNumber = require('react-intl').FormattedNumber;

var Breadcrumb = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

  	render: function() {
  		var _t = this.context.intl.formatMessage;
 		 		
  		var items = this.props.routes.map(function(item, index) {
  			if(item.component.hasOwnProperty('title')) {
  				return (
					<li key={index}>
						<Link onlyActiveOnIndex={true} activeClassName='breadcrumb-active' to={item.path || '/'}>
							<i className={'fa fa-' + item.component.icon + ' fa-fw'}></i>{' ' + _t({ id: item.component.title})}
						</Link>
					</li>
				);
  			}
  			return null;
  		});
  		
  		return (<ul className="breadcrumbs-list">
  					{ items.filter( (item) => { return !!item; }) }
				</ul>);
  	}
});

module.exports = Breadcrumb;
