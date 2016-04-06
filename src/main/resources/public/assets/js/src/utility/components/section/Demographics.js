var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var Chart = require('../Chart');

var Demographics = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getInitialState() {
		return {
			key: 1,
			showAddNewUserForm: false
    	};
	},

	selectSection(key) {
		this.setState({key : key});
  	},
  	
  	showAddNewUserForm: function (){
  		this.setState({showAddNewUserForm : true});
  	},
	
  	render: function() {
  		var _t = this.context.intl.formatMessage;

  		var groups = {
			fields: [{
				name: 'id',
				title: 'Table.Group.id',
				hidden: true
			}, {
				name: 'name',
				title: 'Table.Group.name',
				link: '/group/{id}'
			}, {
				name: 'size',
				title: 'Table.Group.size'
			}, {
				name: 'createdOn',
				title: 'Table.Group.createdOn'
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
  					name: 'add-favourite',
  					type:'action',
  					icon: 'bookmark-o',
  					handler: function() {
  						console.log(this);
  					}
			}, {
  					name: 'chart',
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
				name: 'Alicante DAIAD Trial',
				size: 97,
				createdOn: new Date()
			}, {
				id: 2,
				name: 'St. Albans DAIAD Trial',
				size: 32,
				createdOn: new Date()
			}],
			pager: {
				index: 0,
				size: 10,
				count:45
			}
		};

  		var favourites = {
			fields: [{
				name: 'id',
				hidden: true
			}, {
				name: 'type',
				title: 'Type'
			}, {
				name: 'name',
				title: 'Name',
				link: function(row) {
					switch(row.type) {
						case 'User':
							return '/user/{id}';
						case 'Commons': case 'Group':
							return '/group/{id}';
					}
					return null;
				}
			}, {
				name: 'addedOn',
				title: 'Added On',
				type: 'datetime'
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
				name: 'chart',
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
				type: 'User',
				name: 'User 1',
				addedOn: new Date((new Date()).getTime() + Math.random() * 3600000)
			}, {
				id: 11,
				type: 'Commons',
				name: 'Alicante DAIAD Trial',
				addedOn: new Date((new Date()).getTime() + Math.random() * 3600000)
			}],
			pager: {
				index: 0,
				size: 1,
				count:2
			}
		};
  		
  		var chartData = {
		    series: [{
		        legend: 'Alicante DAIAD Trial (average)',
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
		        legend: 'User 1',
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


  		var jobs = {
			fields: [{
				name: 'id',
				hidden: true
			}, {
				name: 'description',
				title: 'Description'
			}, {
				name: 'owner',
				title: 'Owner'			
			}, {
				name: 'createdOn',
				title: 'Created On',
				type: 'datetime'
			}, {
				name: 'scheduledOn',
				title: 'Next Execution',
				type: 'datetime'
			}, {
				name: 'status',
				title: 'Status'
			}, {
				name: 'progress',
				title: 'Progress',
				type: 'progress'
			}, {
				name: 'edit',
				type:'action',
				icon: 'pencil',
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
				description: 'Find top 20 consumers for January 2016',
				owner: 'Yannis',
				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
				scheduledOn: new Date((new Date()).getTime() + Math.random() * 3600000),
				status: 'Running',
				progress: 45
			}, {
				id: 2,
				description: 'Create clusters of users based on consumption behavior patterns',
				owner: 'Yannis',
				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
				scheduledOn: new Date((new Date()).getTime() + Math.random() * 3600000),
				status: 'Pending',
				progress: null
			}],
			pager: {
				index: 0,
				size: 1,
				count:2
			}
		};
  		
        var chartOptions = {
            tooltip: {
                show: true
            }
        };

		const groupTitle = (
			<span>
				<i className='fa fa-group fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Groups</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle="default" className="btn-circle">
						<Bootstrap.Glyphicon glyph="plus" />
					</Bootstrap.Button>
				</span>
			</span>
		);
		const favouriteTitle = (
			<span>
				<i className='fa fa-bookmark fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Favourites</span>
			</span>
		);

		const chartTitle = (
			<span>
				<i className="fa fa-bar-chart fa-fw"></i>
				<span style={{ paddingLeft: 4 }}>Compare groups and users</span>
			</span>
		);	

		const scheduleTitle = (
			<span>
				<i className='fa fa-clock-o fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Job Management</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle="default" className="btn-circle">
						<Bootstrap.Glyphicon glyph="plus" />
					</Bootstrap.Button>
				</span>
			</span>
		);
		
		var addNewUserForm;
		
		if (this.state.showAddNewUserForm){
			addNewUserForm = (
				<div className='row'>
					<div className='col-md-12'>
						<Bootstrap.ListGroup>
							<Bootstrap.ListGroupItem>
								<div className='clearfix'>
									<div style={{ float: 'right'}}>
										<Bootstrap.Button bsStyle="danger"	onClick={function(){alert('Hello new user!');}}>
											<i className='fa fa-plus' style={{ paddingRight: 5 }}></i>
											Add New User
										</Bootstrap.Button>
									</div>
								</div>
							</Bootstrap.ListGroupItem>
						</Bootstrap.ListGroup>
					</div>
				</div>
			);
		} else {
			addNewUserForm = (
				<div className='row'>
					<div className='col-md-12'>
						<Bootstrap.ListGroup>
								<div className='clearfix'>
									<div style={{ float: 'right'}}>
										<Bootstrap.Button bsStyle="success"	onClick={this.showAddNewUserForm}>
											<i className='fa fa-plus' style={{ paddingRight: 5 }}></i>
											Add New User
										</Bootstrap.Button>
									</div>
								</div>
						</Bootstrap.ListGroup>
					</div>
				</div>
			);
		}

  		return (
			<div className="container-fluid" style={{ paddingTop: 10 }}>
				<div className="row">
					<div className="col-md-12">
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				{addNewUserForm}
				<div className='row'>
					<div className='col-md-6'>
					 	<Bootstrap.Input 	type="text" 
					 						placeholder='Search groups ...' 
					 						buttonAfter={<Bootstrap.Button><i className='fa fa-search fa-fw'></i></Bootstrap.Button>} 
					 	/>
			 		</div>
					<div className='col-md-6'>
					 	<Bootstrap.Input 	type="text" 
					 						placeholder='Search favourites ...' 
					 						buttonAfter={<Bootstrap.Button><i className='fa fa-search fa-fw'></i></Bootstrap.Button>} 
					 	/>
			 		</div>
			 	</div>
				<div className='row'>
					<div className='col-md-6'>
						<Bootstrap.Panel header={groupTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<Table data={groups}></Table>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
					<div className='col-md-6'>
						<Bootstrap.Panel header={favouriteTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>	
									<Table data={favourites}></Table>
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
				<div className="row">
					<div className='col-md-12'>
						<Bootstrap.Panel header={scheduleTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>	
									<Table data={jobs}></Table>
								</Bootstrap.ListGroupItem>
								<Bootstrap.ListGroupItem>
									<span style={{ paddingLeft : 7}}> </span>
									<Link to='/scheduler' style={{ paddingLeft : 7, float: 'right'}}>View job management</Link>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
			</div>
 		);
  	}
});

Demographics.icon = 'bookmark';
Demographics.title = 'Section.Demographics';

module.exports = Demographics;
