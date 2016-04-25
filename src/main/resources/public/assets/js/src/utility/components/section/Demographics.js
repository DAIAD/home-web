var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var Chart = require('../Chart');

var Helpers = require('../../helpers/helpers');

var DemographicsActions = require('../../actions/DemographicsActions');

var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

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
	
	componentWillMount : function() {
	  this.props.getGroups();  
	  this.props.getFavourites();
  },

	selectSection(key) {
		this.setState({key : key});
  	},
  	
  	showAddNewUserForm: function (){
  		this.setState({showAddNewUserForm : true});
  	},
  	
  	setGroupsFilter : function(){
  	  this.props.setGroupsFilter(this.refs.searchGroups.getValue());
  	},
  	
  	clearGroupsFilter : function(){
  	  this.props.setGroupsFilter('');
    },
    
    setFavouritesFilter : function(){
      this.props.setFavouritesFilter(this.refs.searchFavourites.getValue());
    },
    
    clearFavouritesFilter : function(){
      this.props.setFavouritesFilter('');
    },
  	
  	computeVisibleItems : function(items, filter){
  	  var visibleItems = {
  	      fields : items.fields,
  	      rows : [],
  	      pager : {
  	        index : items.pager.index,
  	        size : items.pager.size,
  	        count : 1
  	      }
  	  };
  	  
  	  if (filter){
  	    visibleItems.rows = Helpers.pickQualiffiedOnSusbstring(items.rows, 'name', filter, false);
  	  } else {
  	    visibleItems.rows = items.rows;
  	  }
  	  visibleItems.pager.count = Math.ceil(visibleItems.rows.length / visibleItems.pager.size);
  	  return visibleItems;
  	},
	
  	render: function() {
  		var _t = this.context.intl.formatMessage;
  		
  		var visibleGroups = this.computeVisibleItems(this.props.groups, this.props.groupsFilter);
  		var visibleFavourites = this.computeVisibleItems(this.props.favourites, this.props.favouritesFilter);
  		
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
				title: 'Demographics.JobManagement.Description'
			}, {
				name: 'owner',
				title: 'Demographics.JobManagement.Owner'			
			}, {
				name: 'createdOn',
				title: 'Demographics.JobManagement.CreatedOn',
				type: 'datetime'
			}, {
				name: 'scheduledOn',
				title: 'Demographics.JobManagement.NextExecution',
				type: 'datetime'
			}, {
				name: 'status',
				title: 'Demographics.JobManagement.Status'
			}, {
				name: 'progress',
				title: 'Demographics.JobManagement.Progress',
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
		

  		return (
			<div className="container-fluid" style={{ paddingTop: 10 }}>
				<div className="row">
					<div className="col-md-12">
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				<div className='row'>
					<div className='col-md-6'>
					 	<Bootstrap.Input 	type="text" 
					 	          ref="searchGroups"
					 						placeholder='Search groups ...'
					 						onChange={this.setGroupsFilter}
					 						buttonAfter={<Bootstrap.Button onClick={this.clearGroupsFilter}><i className='fa fa-trash fa-fw'></i></Bootstrap.Button>} 
					 	/>
			 		</div>
					<div className='col-md-6'>
					 	<Bootstrap.Input 	type="text" 
					 	          ref="searchFavourites"
					 						placeholder='Search favourites ...' 
					 						onChange={this.setFavouritesFilter}
					 						buttonAfter={<Bootstrap.Button onClick={this.clearFavouritesFilter}><i className='fa fa-trash fa-fw'></i></Bootstrap.Button>} 
					 	/>
			 		</div>
			 	</div>
				<div className='row'>
					<div className='col-md-6'>
						<Bootstrap.Panel header={groupTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<Table data={visibleGroups}></Table>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
					<div className='col-md-6'>
						<Bootstrap.Panel header={favouriteTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>	
									<Table data={visibleFavourites}></Table>
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


function mapStateToProps(state) {
  return {
    isLoading : state.demographics.isLoading,
    groupsFilter : state.demographics.groupsFilter,
    groups : state.demographics.groups,
    favouritesFilter : state.demographics.favouritesFilter,
    favourites : state.demographics.favourites
  };
}

function mapDispatchToProps(dispatch) {
  return {
    getGroups : bindActionCreators(DemographicsActions.getGroups, dispatch),
    getFavourites : bindActionCreators(DemographicsActions.getFavourites, dispatch),
    setGroupsFilter : bindActionCreators(DemographicsActions.setGroupsFilter, dispatch),
    setFavouritesFilter : bindActionCreators(DemographicsActions.setFavouritesFilter, dispatch)
  };
}


module.exports = connect(mapStateToProps, mapDispatchToProps)(Demographics);

