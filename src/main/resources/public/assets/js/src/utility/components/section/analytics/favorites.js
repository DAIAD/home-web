var React = require('react');
var ReactDOM = require('react-dom');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../../Breadcrumb');
var Chart = require('../../Chart');
var ClusterChart = require('../../ClusterChart');

var ChartWizard = require('../../ChartWizard');
var ChartConfig = require('../../ChartConfig');
var Tag = require('../../Tag');
var Message = require('../../Message');
var JobConfigAnalysis = require('../../JobConfigAnalysis');
var FilterTag = require('../../chart/dimension/FilterTag');
var Timeline = require('../../Timeline');

var LeafletMap = require('../../LeafletMap');
var Table = require('../../Table');
var { Link } = require('react-router');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

var { openFavourite, closeFavourite } 
 = require('../../../actions/FavouritesActions');

var createPoints = function() {
	var points = [];
	
	for(var i=0; i<50; i++) {
		points.push([38.35 + 0.02 * Math.random(), -0.521 + 0.05 * Math.random(), Math.random()]);
	}
	
	return points;
};

var createSeries = function(ref, days, baseConsumption, offset) {
	var series = [];
	for(var d=0; d < days; d++) {
		series.push({
			volume: (baseConsumption + Math.random() * offset).toFixed(0),
			date: ref.clone().toDate()
		});
		ref.add(1, 'days');
	}

	return series;
};

var createTimeLabels = function(ref, hours) {
	var series = [];

	for(var h=0; h < hours; h++) {
		series.push(ref.clone().toDate());
		ref.subtract(1, 'hours');
	}

	return series.reverse();
};

var Favourites = React.createClass({
	 contextTypes: {
	   intl: React.PropTypes.object
	 },

	 toggleView(view) {
		  this.setState({chart : !this.state.chart});
  },

	 toggleMode(mode, e) {
		  this.setState({
			   mode : mode
		  });
  },
  	
  toggleExpanded() {
    this.setState({expanded: !this.state.expanded});
  },
    
  componentWillMount : function() {
		  this.setState({points : createPoints()});
	 },
	
  clickedOpenFavourite(favourite) {
    this.props.actions.openFavourite(favourite);
  },
 
  render: function() {
 
 		 var icon = 'list';
    var self = this;
   
  	 var chartData = {
		    series: [{
		        legend: 'Alicante (average)',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: createSeries(moment(new Date()).subtract(28, 'days'), 29, 180, 60)
		    }, {
		        legend: 'User 1',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: createSeries(moment(new Date()).subtract(28, 'days'), 29, 150, 30)
		    }]
		  };
  		
    var chartOptions = {
            tooltip: {
                show: true
            }
    };

		  const dashboardLinkFooter = (
			   <Bootstrap.ListGroupItem>
			 		  <span style={{ paddingLeft : 7}}> </span>
			 			   <Link to='/' style={{ paddingLeft : 7, float: 'right'}}>View Dashboard</Link>
			 		</Bootstrap.ListGroupItem>
	 		); 
 
		  const configTitle = (
		 		 <span>
			 		  <i className={'fa fa-' + icon + ' fa-fw'}></i>
			 		  <span style={{ paddingLeft: 4 }}>{'Favourite Selection'}</span>
			 	 </span>
	 		);
  
   var title, dataContent, footerContent, toggleTitle, togglePanel, today = new Date();
   var onChangeTimeline = function(value) {
	 	  this.setState({points: createPoints()});
 		};  
  
   if(this.props.selectedFavourite){
     switch(this.props.selectedFavourite.type) {
       case 'Map':
       
		       title = 'Map'; 
		       dataContent = (
			        <Bootstrap.ListGroupItem>
				         <LeafletMap style={{ width: '100%', height: 400}} 
							        elementClassName='mixin'
							        prefix='map'
						         center={[38.35, -0.48]} 
               zoom={13}
				           mode={LeafletMap.MODE_HEATMAP}
							        data={this.state.points} />
 				        <Timeline 	onChange={onChangeTimeline.bind(this)} 
	 						       style={{paddingTop: 10}}
		 					       min={1}
			 				       max={24}
				 			       value={24}
					 		       type='time'
						         data={createTimeLabels(moment(new Date(today.getFullYear(), today.getMonth(), today.getDate(), today.getHours(), 0, 0)), 24)}>
				         </Timeline>
			        </Bootstrap.ListGroupItem>
		       );  
     
 				    footerContent = (
	 				     <Bootstrap.ListGroupItem>
		 				      <span style={{ paddingLeft : 7}}> </span>
			 			      <Link to='/analytics/map' style={{ paddingLeft : 7, float: 'right'}}>View Maps</Link>
				 	     </Bootstrap.ListGroupItem>
				     );  
           break;
       case 'Chart':
     
         title = 'Chart';
		       dataContent = (
			        <Bootstrap.ListGroupItem>
 
	 		       </Bootstrap.ListGroupItem>
         );
      
 				    footerContent = (
	 				     <Bootstrap.ListGroupItem>
		 				      <span style={{ paddingLeft : 7}}> </span>
			 			      <Link to='/analytics/panel' style={{ paddingLeft : 7, float: 'right'}}>View Charts</Link>
				 	     </Bootstrap.ListGroupItem>
				     ); 
           break;
       default:
         title = this.props.selectedFavourite.type;
     }
    
    //
 		  toggleTitle = (
    	  <span>
		 		    <span>
			 		    <i className={'fa fa-' + icon + ' fa-fw'}></i>
				 	    <span style={{ paddingLeft: 4 }}>{title}</span>
				     </span>
				     <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					      <Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.props.actions.closeFavourite}>
						       <i className='fa fa-remove fa-fw'></i>
					      </Bootstrap.Button>
 				    </span> 
       </span>
		 	 );    
   
     togglePanel = (
       <Bootstrap.Panel expanded={this.state.expanded} onSelect={this.toggleExpanded} header={toggleTitle}>
				     <Bootstrap.ListGroup fill>
           {dataContent}
           {footerContent}
						   </Bootstrap.ListGroup>
				   </Bootstrap.Panel>
     ); 
 
   } else{
  
     var infoText = (<span>Click a favourite to view ...</span>);
     togglePanel = 	(        
       <Bootstrap.Panel >
         <Bootstrap.ListGroup fill>
           <Bootstrap.ListGroupItem>
             {infoText}
           </Bootstrap.ListGroupItem>
         </Bootstrap.ListGroup>
       </Bootstrap.Panel>  
     );
   }
   
  	var favs = {
			fields: [{
				name: 'id',
				hidden: true
			}, {
				name: 'type',
				title: 'Type'
			}, {
				name: 'label',
				title: 'Label'			
			}, {
				name: 'owner',
				title: 'Owner'			
			}, {
				name: 'createdOn',
				title: 'Created On',
				type: 'datetime'
			}, {
				name: 'view',
				type:'action',
				icon: 'eye',
				handler: function() {
					console.log(this);
     self.clickedOpenFavourite(this.props.row);
				}
			}, {
				name: 'edit',
				type:'action',
				icon: 'pencil',
				handler: function() {
     console.log('go to corresponding menu tab in edit mode');
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
				type: 'Chart',
				owner: 'Admin@daiad',
				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000)
			},{
				id: 2,
				type: 'Map',
				owner: 'Admin@daiad',
				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000)
			}],
			pager: {
				index: 0,
				size: 2,
				count:3
			}
	 	};

	 	var favouriteContent = (
		   <div style={{ padding: 10}}>
			 	  <Table data={favs}></Table>
			 	</div>
		 );

 		return (
	 		 <div className='container-fluid' style={{ paddingTop: 10 }}>
		 		  <div className='row'>
			 		   <div className='col-md-12'>
				 		    <Breadcrumb routes={this.props.routes}/>
					    </div>
 				  </div>
	 			  <div className='row'>
		 			   <div className='col-lg-12'>
			 			    <Bootstrap.Panel header={configTitle}>
				 			     <Bootstrap.ListGroup fill>
               {favouriteContent}
						 	     </Bootstrap.ListGroup>
						     </Bootstrap.Panel>
             {togglePanel}
             {dashboardLinkFooter}
 					   </div>
	 		   </div>
    </div>
   );
  }
});

function mapStateToProps(state) {
  console.log('map state to props');
  console.log(state);
  return {
    showSelected: state.favourites.showSelected,
    selectedFavourite: state.favourites.selectedFavourite
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { openFavourite, closeFavourite}) , dispatch)                                                     
  };
}

Favourites.icon = 'bar-chart';
Favourites.title = 'Section.Analytics.Fav';

module.exports = connect(mapStateToProps, mapDispatchToProps)(Favourites);
