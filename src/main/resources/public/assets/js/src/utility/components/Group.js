var React = require('react');
var Bootstrap = require('react-bootstrap');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');
var Table = require('./Table');
var Chart = require('./Chart');

var Group = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

  	render: function() {
  		var chartData = {
  			    series: [{
  			        legend: 'Average',
  			        xAxis: 'date',
  			        yAxis: 'volume',
  			        data: [{
  			            id: 1,
  			            volume: 25,
  			            date: new Date(2016, 1, 1)
  			        }, {
  			            id: 1,
  			            volume: 70,
  			            date: new Date(2016, 1, 2)
  			        }, {
  			            id: 1,
  			            volume: 75,
  			            date: new Date(2016, 1, 3)
  			        }, {
  			            id: 1,
  			            volume: 62,
  			            date: new Date(2016, 1, 4)
  			        }, {
  			            id: 1,
  			            volume: 53,
  			            date: new Date(2016, 1, 5)
  			        }, {
  			            id: 1,
  			            volume: 82,
  			            date: new Date(2016, 1, 6)
  			        }, {
  			            id: 1,
  			            volume: 41,
  			            date: new Date(2016, 1, 7)
  			        }, {
  			            id: 1,
  			            volume: 45,
  			            date: new Date(2016, 1, 8)
  			        }, {
  			            id: 1,
  			            volume: 52,
  			            date: new Date(2016, 1, 9)
  			        }]
  			    }, {
  			        legend: 'User 1',
  			        xAxis: 'date',
  			        yAxis: 'volume',
  			        data: [{
  			            id: 1,
  			            volume: 2,
  			            date: new Date(2016, 1, 1)
  			        }, {
  			            id: 1,
  			            volume: 4,
  			            date: new Date(2016, 1, 2) 
  			        }, {
  			            id: 1,
  			            volume: 7,
  			            date: new Date(2016, 1, 3)
  			        }, {
  			            id: 1,
  			            volume: 8,
  			            date: new Date(2016, 1, 4)
  			        }, {
  			            id: 1,
  			            volume: 0,
  			            date: new Date(2016, 1, 5)
  			        }, {
  			            id: 1,
  			            volume: 0,
  			            date: new Date(2016, 1, 6)
  			        }, {
  			            id: 1,
  			            volume: 1,
  			            date: new Date(2016, 1, 7)
  			        }, {
  			            id: 1,
  			            volume: 2,
  			            date: new Date(2016, 1, 8)
  			        }, {
  			            id: 1,
  			            volume: 4,
  			            date: new Date(2016, 1, 9)
  			        }]
  			    }]
  			};
  	  		
  	        var chartOptions = {
  	            tooltip: {
  	                show: true
  	            }
  	        };

  	  		var members = {
  	  				fields: [{
  	  					name: 'id',
  	  					hidden: true
  	  				}, {
  	  					name: 'user',
  						title: 'Username',
  	  					link: '/user/{id}'
  	  				}, {
  	  					name: 'registeredOn',
  	  					title: 'Registered On',
  	  					type: 'datetime'
  	  				}, {
  	  					name: 'email',
  	  					title: 'Email'
  	  				}, {
  	  					name: 'map',
  	  					type:'action',
  	  					icon: 'map-o',
  	  					handler: function() {
  	  						console.log(this);
  	  					}
  	  				}, {
  	  					name: 'message',
  	  					type:'action',
  	  					icon: 'envelope-o',
  	  					handler: function() {
  	  						console.log(this);
  	  					}
  	  				}, {
  	  					name: 'bookmark',
  	  					type:'action',
  	  					icon: 'bookmark-o',
  	  					handler: function() {
  	  						console.log(this);
  	  					}
  	  				}, {
  	  					name: 'export',
  	  					type:'action',
  	  					icon: 'cloud-download',
  	  					handler: function() {
  	  						console.log(this);
  	  					}
  	  				}, {
  	  					name: 'add-chart',
  	  					type:'action',
  	  					icon: 'bar-chart',
  	  					handler: function() {
  	  						console.log(this);
  	  					}
  	  				}, {
  	  					name: 'remove',
  	  					type:'action',
  	  					icon: 'remove',
  	  					handler: function() {
  	  						console.log(this);
  	  					}
  	  				}],
  	  				rows: [{
  	  					id: 1,
  	  					user: 'User 1',
  	  					registeredOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  	  					email: 'user1@daiad.eu'
  	  				}, {
  	  					id: 2,
  	  					user: 'User 2',
  	  					registeredOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  	  					email: 'user2@daiad.eu'
  	  				}],
  	  				pager: {
  	  					index: 0,
  	  					size: 1,
  	  					count:2
  	  				}
  	  			};
  			const groupTitle = (
  					<span>
  						<i className='fa fa-group fa-fw'></i>
  						<span style={{ paddingLeft: 4 }}>Alicante</span>
  						<span style={{float: 'right',  marginTop: -5 }}>
  							<Bootstrap.SplitButton title='Actions' id='profile-actions'>
  								<Bootstrap.MenuItem eventKey='1'>
  									<i className='fa fa-envelope-o fa-fw'></i>
									<span style={{ paddingLeft: 4 }}>Send Message</span>
								</Bootstrap.MenuItem>
								<Bootstrap.MenuItem eventKey='1'>
									<i className='fa fa-bookmark-o fa-fw'></i>
									<span style={{ paddingLeft: 4 }}>Add to favourites</span>
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


  			const memberTitle = (
  					<span>
  						<i className='fa fa-user fa-fw'></i>
  						<span style={{ paddingLeft: 4 }}>Members</span>
  					</span>
  				);
  		return (
			<div className='container-fluid' style={{ paddingTop: 10 }}>
				<div className='row'>
					<div className='col-md-4'>
						<Bootstrap.Panel header={groupTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<div className='row'>
										<table className='table table-profile'>
											<tbody>
												<tr>
													<td>Name</td>
													<td>Alicante</td>
												</tr>
												<tr>
													<td>Description</td>
													<td>Alicante DAIAD Trial</td>
												</tr>
												<tr>
													<td>Created on</td>
													<td><FormattedDate value={new Date()} day='numeric' month='long' year='numeric' /></td>
												</tr>
												<tr>
													<td>Country</td>
													<td>Spain</td>
												</tr>
												<tr>
													<td>Size</td>
													<td>97</td>
												</tr>	
											</tbody>
										</table>
									</div>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
					<div className='col-md-8'>
						<Bootstrap.Panel header={memberTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									 	<Bootstrap.Input 	type="text" 
									 						placeholder='Search for user ...' 
									 						buttonAfter={<Bootstrap.Button><i className='fa fa-plus fa-fw'></i></Bootstrap.Button>} 
									 	/>
									<Table data={members}></Table>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
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
			</div>
 		);
  	}
});

module.exports = Group;
