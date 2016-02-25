var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');

var Announcements = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

  	render: function() {
  		var _t = this.context.intl.formatMessage;
 
  		var data = {
  				fields: [{
  					name: 'id',
  					title: 'Id',
  					hidden: true
  				}, {
  					name: 'text',
  					title: 'Message'
  				}, {
  					name: 'createdOn',
  					title: 'Created On',
  					type: 'datetime'
  				}, {
  					name: 'dispatchedOn',
  					title: 'Dispatched On',
  					type: 'datetime'
  				}, {
  					name: 'status',
  					title: 'Status',
  					className: function(value) {
  						return 'danger';
  					},
  					hidden: true
  				}, {
  					name: 'edit',
  					type:'action',
  					icon: 'pencil',
  					handler: function() {
  						console.log(this);
  					}
  				}, {
  					name: 'copy',
  					type:'action',
  					icon: 'copy',
  					handler: function() {
  						console.log(this);
  					}
  				}, {
  					name: 'cancel',
  					type:'action',
  					icon: 'remove',
  					handler: function() {
  						console.log(this);
  					}
  				}],
  				rows: [{
  					id: 1,
  					text: 'New water tariff policy applied on 1/1/2017',
  					createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  					dispatchedOn: new Date((new Date()).getTime() + (2+Math.random()) * 3600000),
  					status: 'Pending',
  					acknowledged: false
  				}, {
  					id: 2,
  					text: 'Save 10% on your monthly bill by installing Amphiro B1',
  					createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  					dispatchedOn: new Date((new Date()).getTime() + (3+Math.random()) * 3600000),
  					status: 'Pending',
  					acknowledged: true
  				}, {
  					id: 3,
  					text: 'Energy-saving hints for washing machines',
  					createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  					dispatchedOn: new Date((new Date()).getTime() + (10+Math.random()) * 3600000),
  					status: 'Pending',
  					acknowledged: false
  				}],
  				pager: {
  					index: 2,
  					size: 3,
  					count:22
  				}
  			};

		const historyTitle = (
				<span>
					<i className='fa fa-calendar fa-fw'></i>
					<span style={{ paddingLeft: 4 }}>History</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle="default" className="btn-circle">
						<Bootstrap.Glyphicon glyph="plus" />
					</Bootstrap.Button>
				</span>
				</span>
			);
		
  		return (
			<div className="container-fluid" style={{ paddingTop: 10 }}>
				<div className="row">
					<div className="col-md-12">
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				<div className="row">
					<div className="col-md-12">
						<Bootstrap.Panel header={historyTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>	
									<Table data={data}></Table>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
			</div>
 		);
  	}
});

Announcements.icon = 'volume-up';
Announcements.title = 'Section.Announcements';

module.exports = Announcements;
