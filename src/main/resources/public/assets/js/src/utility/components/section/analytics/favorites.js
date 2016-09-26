var React = require('react');
var ReactDOM = require('react-dom');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../../Breadcrumb');
var Chart = require('../../Chart');
var ClusterChart = require('../../ClusterChart');

var Timeline = require('../../Timeline');
var LeafletMap = require('../../LeafletMap');
var Table = require('../../Table');
var { Link } = require('react-router');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

var { fetchFavouriteQueries, openFavourite, closeFavourite, setActiveFavourite, addCopy } 
 = require('../../../actions/FavouritesActions');

//var ViewChart = require('../../../report-measurements/pane');
var rows;
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
	   intl: React.PropTypes.object,
    router: function() { return React.PropTypes.func.isRequired; } 
    //using this instead of  router: React.PropTypes.func due to warning
    //https://github.com/react-bootstrap/react-router-bootstrap/issues/91
	 },

  componentWillMount : function() {
    this.props.actions.fetchFavouriteQueries();
		  this.setState({points : createPoints()});
	 },
	
  clickedOpenFavourite(favourite) {
    this.props.actions.openFavourite(favourite);
  },

  editFavourite(favourite) {
    this.props.actions.setActiveFavourite(favourite);
    switch (favourite.type) {
      case 'MAP':
        this.context.router.push('/analytics/map'); 
        break;
      case 'Chart':
        this.context.router.push('/analytics/panel');  
        break;
      default:
        console.log('Favourite type [' + favourite.type + '] is not supported.');
        break;
    }
  },

  duplicateFavourite(namedQuery) {
    var request =  {
      'namedQuery' : namedQuery
    };
    namedQuery.title = namedQuery.title + ' (copy)';
    this.props.actions.addCopy(request);   
    this.props.actions.fetchFavouriteQueries();
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
       case 'MAP':   
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
			 		       <span style={{ paddingLeft : 7}}> </span>
			 			      <Link to='/' style={{ paddingLeft : 7, float: 'right'}}>View Dashboard</Link>            
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
			 		       <span style={{ paddingLeft : 7}}> </span>
			 			      <Link to='/' style={{ paddingLeft : 7, float: 'right'}}>View Dashboard</Link>              
				 	     </Bootstrap.ListGroupItem>
				     ); 
           break;
       default:
         title = this.props.selectedFavourite.type;
     }
    
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
             {dashboardLinkFooter}
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
				   name: 'title',
				   title: 'Label'			
			  }, {
				   name: 'tags',
				   title: 'Tags'			
			  }, {
				   name: 'createdOn',
				   title: 'Date',
				   type: 'datetime'
			  }, {
				   name: 'view',
				   type:'action',
				   icon: 'eye',
				   handler: function() {
         self.clickedOpenFavourite(this.props.row);
				   }
			  }, {
				   name: 'edit',
				   type:'action',
				   icon: 'pencil',
				   handler: function() {
         self.editFavourite(this.props.row);
				   }
			  }, {
				   name: 'copy',
				   type:'action',
				   icon: 'copy',
				   handler: function() {
         self.duplicateFavourite(this.props.row);
				   }
			  }, {
				   name: 'link',
				   type:'action',
				   icon: 'link',
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
			  rows: this.props.favourites ? this.props.favourites : rows,
			  pager: {
				   index: 0,
				   size: 5,
				   count: this.props.favourites ? this.props.favourites.length : 0
			  }
	 	};

	 	var favouriteContent = (
		   <div style={{ padding: 10}}>
			 	  <Table data={favs}></Table>
			 	</div>
		 );

   if(this.props.favourites && !this.props.isLoading){
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
 					     </div>
	 		     </div>
      </div>
     );   
   }
   else{
      return (
        <div>
          <img className='preloader' src='/assets/images/utility/preloader-counterclock.png' />
          <img className='preloader-inner' src='/assets/images/utility/preloader-clockwise.png' />
        </div>
      );    
   }

  }
});

function mapStateToProps(state) {
  return {
    showSelected: state.favourites.showSelected,
    selectedFavourite: state.favourites.selectedFavourite,
    favourites: state.favourites.favourites
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { fetchFavouriteQueries, openFavourite, 
                                                     closeFavourite, setActiveFavourite, addCopy}) , dispatch)                                                     
  };
}

Favourites.icon = 'bar-chart';
Favourites.title = 'Section.Analytics.Fav';

module.exports = connect(mapStateToProps, mapDispatchToProps)(Favourites);
