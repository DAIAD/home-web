var React = require('react');
var Bootstrap = require('react-bootstrap');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');
var Table = require('./Table');
var Chart = require('./Chart');

var Device = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

  	render: function() {
  		var chartData = {
  			    series: [{
  			        legend: 'Amphiro Shower #1',
  			        xAxis: 'date',
  			        yAxis: 'volume',
  			        data: [{
  			            id: 1,
  			            name: 'Sales',
  			            volume: 25,
  			            date: new Date(2016, 1, 1),
  			            geometry: { }
  			        }, {
  			            id: 1,
  			            name: 'Sales',
  			            volume: 70,
  			            date: new Date(2016, 1, 2),
  			            geometry: { } 
  			        }, {
  			            id: 1,
  			            name: 'Sales',
  			            volume: 75,
  			            date: new Date(2016, 1, 3),
  			            geometry: { }
  			        }, {
  			            id: 1,
  			            name: 'Sales',
  			            volume: 62,
  			            date: new Date(2016, 1, 4),
  			            geometry: { }
  			        }, {
  			            id: 1,
  			            name: 'Sales',
  			            volume: 53,
  			            date: new Date(2016, 1, 5),
  			            geometry: { }
  			        }, {
  			            id: 1,
  			            name: 'Sales',
  			            volume: 27,
  			            date: new Date(2016, 1, 6),
  			            geometry: { }
  			        }, {
  			            id: 1,
  			            name: 'Sales',
  			            volume: 41,
  			            date: new Date(2016, 1, 7),
  			            geometry: { }
  			        }, {
  			            id: 1,
  			            name: 'Sales',
  			            volume: 45,
  			            date: new Date(2016, 1, 8),
  			            geometry: { }
  			        }, {
  			            id: 1,
  			            name: 'Sales',
  			            volume: 13,
  			            date: new Date(2016, 1, 9),
  			            geometry: { }
  			        }]
  			    }]
  			};
  	  		
  	        var chartOptions = {
  	            tooltip: {
  	                show: true
  	            }
  	        };

  	  		var sessions = {
  	  			fields: [{
  	  				name: 'id',
  	  				title: 'Id'
  	  			}, {
  	  				name: 'createdOn',
  	  				title: 'Date',
  	  				type: 'datetime'
  	  			}, {
  	  				name: 'volume',
  	  				title: 'Volume'
  	  			}, {
  	  				name: 'energy',
  	  				title: 'Energy'
  	  			}, {
  					name: 'bar-chart',
  					type:'action',
  					icon: 'bar-chart',
  					handler: function() {
  						console.log(this);
  					}
  				}],
  	  			rows: [{
  	  				id: 1,
  	  				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  	  				volume: 130,
  	  				energy: 2
  	  			}, {
  	  				id: 2,
  	  				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  	  				volume: 14,
  	  				energy: 0.1
  	  			}, {
  	  				id: 3,
  	  				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  	  				volume: 71,
  	  				energy: 3
  	  			}],
  	  			pager: {
  	  				index: 0,
  	  				size: 1,
  	  				count:3
  	  			}
  	  		};

  			const profileTitle = (
  					<span>
  						<i className='fa fa-user fa-fw'></i>
  						<span style={{ paddingLeft: 4 }}>Profile</span>
  						<span style={{float: 'right',  marginTop: -5 }}>
  							<Bootstrap.SplitButton title='Actions' id='profile-actions'>
  								<Bootstrap.MenuItem eventKey='1'>
  									<i className='fa fa-envelope-o fa-fw'></i>
									<span style={{ paddingLeft: 4 }}>Send Message</span>
								</Bootstrap.MenuItem>
								<Bootstrap.MenuItem eventKey='1'>
									<i className='fa fa-user-plus fa-fw'></i>
									<span style={{ paddingLeft: 4 }}>Add to group</span>
								</Bootstrap.MenuItem>
  								<Bootstrap.MenuItem divider />
  								<Bootstrap.MenuItem eventKey='2'>
  									<i className='fa fa-cloud-download fa-fw'></i>
  									<span style={{ paddingLeft: 4 }}>Export data</span>
								</Bootstrap.MenuItem>
  							</Bootstrap.SplitButton>
  					    </span>
  					</span>
  				);
  			
  			const consumptionTitle = (
  					<span>
  						<i className='fa fa-bar-chart fa-fw'></i>
  						<span style={{ paddingLeft: 4 }}>Consumption</span>
  					</span>
  				);
  			const sessionTitle = (
  					<span>
  						<i className='fa fa-calendar fa-fw'></i>
  						<span style={{ paddingLeft: 4 }}>Sessions</span>
  					</span>
  				);

  			const propertiesTitle = (
  					<span>
  						<i className='fa fa-list fa-fw'></i>
  						<span style={{ paddingLeft: 4 }}>Properties</span>
  					</span>
  				);
  			
  			const configTitle = (
  					<span>
  						<i className='fa fa-cogs fa-fw'></i>
  						<span style={{ paddingLeft: 4 }}>Configuration</span>
  					</span>
  				);

  		return (
			<div className='container-fluid' style={{ paddingTop: 10 }}>
			<div className='row'>
				<div className='col-md-5'>
					<div className='row'>
						<div className='col-md-12'>
						<Bootstrap.Panel header={profileTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<div className='row'>
										<div className='col-md-4'>
											<div style={{width: '100%',  border: '#3498db solid 3px', borderRadius: '50%', padding: 3 }}>
												<img src='/assets/images/demo/profile.png' style={{borderRadius: '50%', width: '100%'}} />
											</div>
										</div>
										<div className='col-md-8'>
											<table className='table table-profile'>
												<tbody>
													<tr>
														<td>First name</td>
														<td>User 1</td>
													</tr>
													<tr>
														<td>Last name</td>
														<td></td>
													</tr>
													<tr>
														<td>Email</td>
														<td><Link to='/user/1'>user1@daiad.eu</Link></td>
													</tr>
													<tr>
														<td>Registered on</td>
														<td><FormattedDate value={new Date()} day='numeric' month='long' year='numeric' /></td>
													</tr>	
												</tbody>
											</table>
										</div>
									</div>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
						</div>
					</div>
					<div className='row'>
						<div className='col-md-12'>
							<Bootstrap.Panel header={propertiesTitle}>
								<Bootstrap.ListGroup fill>
									<Bootstrap.ListGroupItem>
										<table className='table table-profile'>
											<tbody>
												<tr>
													<td>Name</td>
													<td>Amphiro Shower #1</td>
												</tr>
												<tr>
													<td>Manufacturer</td>
													<td>Amphiro</td>
												</tr>
												<tr>
													<td>Model</td>
													<td>B1</td>
												</tr>
												<tr>
													<td>Registered on</td>
													<td><FormattedDate value={new Date()} day='numeric' month='long' year='numeric' /></td>
												</tr>	
											</tbody>
										</table>
									</Bootstrap.ListGroupItem>
								</Bootstrap.ListGroup>
							</Bootstrap.Panel>
						</div>
					</div>
					<div className='row clearfix'>
						<div className='col-md-12'>
							<Bootstrap.Panel header={configTitle}>
								<Bootstrap.ListGroup fill>
									<Bootstrap.ListGroupItem>
										<table className='table table-profile'>
											<tbody>
												<tr>
													<td>Setting 1</td>
													<td>1</td>
												</tr>
												<tr>
													<td>Setting 2</td>
													<td>3</td>
												</tr>
												<tr>
													<td>Setting 3</td>
													<td>3</td>
												</tr>
												<tr>
													<td>Frame</td>
													<td>1</td>
												</tr>	
												<tr>
													<td>Duration</td>
													<td>4</td>
												</tr>
											</tbody>
										</table>
										<button id='edit'
							   				className='btn btn-primary'>
											<i className='fa fa-pencil fa-fw'></i>
							   				Edit
							   			</button>
									</Bootstrap.ListGroupItem>
								</Bootstrap.ListGroup>
							</Bootstrap.Panel>
						</div>
					</div>
				</div>
				<div className='col-md-7'>
					<div className='row'>
						<div className='col-md-12'>
							<Bootstrap.Panel header={consumptionTitle}>
								<Bootstrap.ListGroup fill>
									<Bootstrap.ListGroupItem>
										<Chart 	style={{ width: '100%', height: 400 }} 
												elementClassName='mixin'
													prefix='chart'
														options={chartOptions}
												data={chartData}/>
									</Bootstrap.ListGroupItem>
								</Bootstrap.ListGroup>
							</Bootstrap.Panel>
						</div>
					</div>
					<div className='row'>
						<div className='col-md-12'>
							<Bootstrap.Panel header={sessionTitle}>
								<Bootstrap.ListGroup fill>
									<Bootstrap.ListGroupItem>
										<Table data={sessions}></Table>
									</Bootstrap.ListGroupItem>
								</Bootstrap.ListGroup>
							</Bootstrap.Panel>
						</div>
					</div>
				</div>
				</div>
			</div>
 		);
  	}
});

module.exports = Device;
