var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var Counter = require('../Counter');
var Table = require('../Table');
var Checkbox = require('../Checkbox');

var Alerts = React.createClass({
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
				name: 'status',
				title: 'Status',
				className: function(value) {
					return 'danger';
				},
				hidden: true
			}, {
				name: 'acknowledged',
				title: '',
				type: 'boolean',
				align: 'center'
			}],
			rows: [{
				id: 1,
				text: 'Job \'Daily pre aggregation MR job\' has started',
				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
				status: 'Pending',
				acknowledged: false
			}, {
				id: 2,
				text: 'Excessive water consumption detected',
				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
				status: 'Pending',
				acknowledged: true
			}, {
				id: 3,
				text: 'Server master-c1-n01 has gone offline',
				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
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
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
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
					<div className="col-md-2">
						<div style={{ marginBottom: 20 }}>
							<Counter text={'Errors'} value={4} color='#cb3f30' />
						</div>
					</div>
					<div className="col-md-2">
						<div style={{ marginBottom: 20 }}>
							<Counter text={'Warnings'} value={1} color='#f39c12'/>
						</div>
					</div>
					<div className="col-md-2">
						<div style={{ marginBottom: 20 }}>
							<Counter text={'Information'} value={2} color='#3498db' />
						</div>
					</div>
					<div className="col-md-6">
						<div style={{ marginBottom: 20 }}>
							<Counter text={'Acknowledged Messages'} value={1} color='#7f8c8d' />
						</div>
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

Alerts.icon = 'bell';
Alerts.title = 'Section.Alerts';

module.exports = Alerts;
