var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var Chart = require('../Chart');
var LeafletMap = require('../LeafletMap');

var Search = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

  	render: function() {
  		var _t = this.context.intl.formatMessage;

  		var data = {
  				fields: [{
  					name: 'deviceId',
  					title: 'Id',
  					hidden: true
  				}, {
  					name: 'userId',
  					hidden: true
  				}, {
  					name: 'type',
  					title: 'Type'
  				}, {
  					name: 'chart',
  					type:'action',
  					icon: 'bar-chart',
  					handler: function() {
  						console.log(this);
  					}
  				}, {
  					name: 'name',
  					title: 'Description',
  					link: '/device/{deviceId}'
  				}, {
  					name: 'registeredOn',
  					title: 'Registered On',
  					type: 'datetime'
  				}, {
  					name: 'lastUpdatedOn',
  					title: 'Last Update On',
  					type: 'datetime'
  				}, {
  					name: 'user',
  					title: 'Username',
  					link: '/user/{userId}'
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
  					name: 'add-to-group',
  					type:'action',
  					icon: 'user-plus',
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
  				}],
  				rows: [{
  					deviceId: 1,
  					userId: 1,
  					type: 'Amphiro',
  					name: 'Amphiro #2',
  					registeredOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  					lastUpdatedOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  					user: 'User 1',
  					email: 'user1@daiad.eu'
  				}, {
  					deviceId: 2,
  					userId: 1,
  					type: 'Meter',
  					name: 'I94854C384',
  					registeredOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  					lastUpdatedOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  					user: 'User 1',
  					email: 'user1@daiad.eu'
  				}],
  				pager: {
  					index: 0,
  					size: 1,
  					count:2
  				}
  			};

  		var chartData = {
  			    series: [{
  			        legend: 'I94854C384',
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
  			            volume: 27,
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
  			            volume: 13,
  			            date: new Date(2016, 1, 9)
  			        }]
  			    }, {
  			        legend: 'Amphiro #2',
  			        xAxis: 'date',
  			        yAxis: 'volume',
  			        data: [{
  			            id: 1,
  			            volume: 15,
  			            date: new Date(2016, 1, 1)
  			        }, {
  			            id: 1,
  			            volume: 30,
  			            date: new Date(2016, 1, 2) 
  			        }, {
  			            id: 1,
  			            volume: 44,
  			            date: new Date(2016, 1, 3)
  			        }, {
  			            id: 1,
  			            volume: 32,
  			            date: new Date(2016, 1, 4)
  			        }, {
  			            id: 1,
  			            volume: 23,
  			            date: new Date(2016, 1, 5)
  			        }, {
  			            id: 1,
  			            volume: 11,
  			            date: new Date(2016, 1, 6)
  			        }, {
  			            id: 1,
  			            volume: 18,
  			            date: new Date(2016, 1, 7)
  			        }, {
  			            id: 1,
  			            volume: 11,
  			            date: new Date(2016, 1, 8)
  			        }, {
  			            id: 1,
  			            volume: 5,
  			            date: new Date(2016, 1, 9)
  			        }]
  			    }]
  			};
  	  		
  	        var chartOptions = {
  	            tooltip: {
  	                show: true
  	            }
  	        };
  	        
  	        
		const filterTitle = (
			<span>
				<i className="fa fa-filter fa-fw"></i>
				<span style={{ paddingLeft: 4 }}>Text Filtering</span>
			</span>
		);
		const mapTitle = (
			<span>
				<i className="fa fa-map fa-fw"></i>
				<span style={{ paddingLeft: 4 }}>Spatial Filtering</span>
			</span>
		);
		const resultTitle = (
			<span>
				<i className="fa fa-table fa-fw"></i>
				<span style={{ paddingLeft: 4 }}>Results</span>
			</span>
		);	
		const chartTitle = (
				<span>
					<i className="fa fa-bar-chart fa-fw"></i>
					<span style={{ paddingLeft: 4 }}>Comparison</span>
				</span>
			);	
		var mapOptions = {
			center:	[51.75692, -0.32678], 
			zoom: 13,
			draw: true
		};
		
  		return (
			<div className='container-fluid' style={{ paddingTop: 10 }}>
				<div className='row'>
					<div className='col-md-12'>
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				<div className='row'>
					<div className="col-md-5">
						<Bootstrap.Panel header={filterTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
										<Bootstrap.Input type='text' ref='username' 
												placeholder='Search by user name, email address ...'
												addonBefore={<i className='fa fa-user fa-fw'></i>}
												defaultValue='User 1'/>
										<Bootstrap.Input type='text' ref='device'
												placeholder='Searh by meter serial number, Amphiro name ...'
												addonBefore={<i className='fa fa-barcode fa-fw'></i>} />
										<button id='search'
							   				className='btn btn-primary'>
											<i className='fa fa-search fa-fw'></i>
							   				Search
							   			</button>										
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
					<div className="col-md-7">
						<Bootstrap.Panel header={mapTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<LeafletMap style={{ width: '100%', height: 300}} 
												elementClassName='mixin'
												prefix='map'
					              center={[38.35, -0.48]} 
					              zoom={13}
					              mode={LeafletMap.MODE_DRAW} />
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
				<div className="row">
					<div className="col-md-12">
						<Bootstrap.Panel header={resultTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<Table data={data}></Table>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
				<div className="row">
					<div className="col-md-12">
						<Bootstrap.Panel header={chartTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<Chart 	style={{ width: '100%', height: 400 }} 
											elementClassName='mixin'
											prefix='chart'
											options={chartOptions}
											data={chartData}/>
								</Bootstrap.ListGroupItem>
								<Bootstrap.ListGroupItem>
									<span style={{ paddingLeft : 7}}> </span>
									<Link to='/analytics' style={{ paddingLeft : 7, float: 'right'}}>View analytics</Link>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
			</div>
 		);
  	}
});

Search.icon = 'search';
Search.title = 'Section.Search';

module.exports = Search;
