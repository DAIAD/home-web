var React = require('react');
var ReactDOM = require('react-dom');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../../Breadcrumb');
var ClusterChart = require('../../ClusterChart');
var Modal = require('../../Modal');
var Timeline = require('../../Timeline');
var LeafletMap = require('../../LeafletMap');
var Table = require('../../Table');
var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

var Chart = require('../../reports-measurements/chart');
var {timespanPropType, populationPropType, seriesPropType, configPropType} = require('../../../prop-types');
var population2 = require('../../../model/population');

var { setTimezone, fetchFavouriteQueries, openFavourite, 
      closeFavourite, setActiveFavourite, 
      addCopy, deleteFavourite, openWarning,
      closeWarning, resetMapState, getFavouriteMap,
      getFavouriteChart, getFeatures} = require('../../../actions/FavouritesActions');
          
//var ViewChart = require('../../../report-measurements/pane');

var _getTimelineValues = function(timeline) {
  if(timeline) {
    return timeline.getTimestamps();
  } 
  return [];
};

var _getTimelineLabels = function(timeline) {
  if(timeline) {
    return timeline.getTimestamps().map(function(timestamp) {
      return (
        <FormattedTime  value={new Date(timestamp)} 
                        day='numeric' 
                        month='numeric' 
                        year='numeric'/>
      );      
    });
  } 
  return [];
};

var createPoints = function() {
	var points = [];
	
	for(var i=0; i<50; i++) {
		points.push([38.35 + 0.02 * Math.random(), -0.521 + 0.05 * Math.random(), Math.random()]);
	}
	
	return points;
};

var _onChangeTimeline = function(value, label, index) {
  this.props.actions.getFeatures(index, value);
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
    config: configPropType,
    router: function() { return React.PropTypes.func.isRequired; },
    //using this instead of  router: React.PropTypes.func due to warning
    //https://github.com/react-bootstrap/react-router-bootstrap/issues/91
	 },

  componentWillMount : function() {
    this.props.actions.resetMapState();
    this.props.actions.fetchFavouriteQueries();
		  this.setState({points : createPoints()});
	 },
  
  componentDidMount : function() {
    var utility = this.props.profile.utility;
    this.props.actions.setTimezone(utility.timezone);
	 },	
  componentWillUnmount : function() {
    //TODO - in case we want the 'Map' section to display the default view of the map,
    //even when there is an active favourite open in the favourite's section, we should de-activate the favourite
    //when unmounting, based on the cause of the unmount. 
    //Keep the favourite active only after the clickedEditFavourite action.
  },
  
  clickedOpenFavourite(favourite) {
    favourite.timezone = this.props.profile.utility.timezone;
    this.props.actions.closeFavourite();
    if(favourite.type == 'MAP'){
      this.props.actions.getFavouriteMap(favourite);
    }
    else if(favourite.type == 'CHART'){
      this.props.actions.getFavouriteChart(favourite);    
    }
    else{
        console.error(sprintf('Favourite type (%s) not supported.', favourite.type));
    }

    this.props.actions.openFavourite(favourite);
  },

  editFavourite(favourite) {
    this.props.actions.setActiveFavourite(favourite);
    switch (favourite.type) {
      case 'MAP':
        this.context.router.push('/analytics/map'); 
        break;
      case 'CHART':
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
  
  clickedDeleteFavourite(namedQuery) {
    var request =  {
      'namedQuery' : namedQuery
    };
    this.props.actions.openWarning(request);  
  },
  
  render: function() {
 		 var icon = 'list';
    var self = this;

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
	 	  //this.setState({points: createPoints()});
 		};  
   
    var defaults= {
      chartProps: {
        width: 780,
        height: 300,
      }
    };
    

   if(this.props.selectedFavourite){
     switch(this.props.selectedFavourite.type) {
       case 'MAP':   
		       title = 'Map: ' + this.props.selectedFavourite.title; 
		       dataContent = (
			        <Bootstrap.ListGroupItem>
				         <LeafletMap style={{ width: '100%', height: 400}} 
               elementClassName='mixin'
               prefix='map'
               center={[38.36, -0.479]} 
               zoom={13}
               mode={[LeafletMap.MODE_DRAW, LeafletMap.MODE_CHOROPLETH]}
               choropleth= {{
                 colors : ['#2166ac', '#67a9cf', '#d1e5f0', '#fddbc7', '#ef8a62', '#b2182b'],
                 min : this.props.map.timeline ? this.props.map.timeline.min : 0,
                 max : this.props.map.timeline ? this.props.map.timeline.max : 0,
                 data : this.props.map.features
               }}
               overlays={[
                 { url : '/assets/data/meters.geojson',
                   popupContent : 'serial'
                 }
               ]} />
 				        <Timeline 	onChange={_onChangeTimeline.bind(this)} 
               labels={ _getTimelineLabels(this.props.map.timeline) }
               values={ _getTimelineValues(this.props.map.timeline) }
               defaultIndex={this.props.map.index}
               speed={1000}
               animate={false}>
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
       case 'CHART':
         title = 'Chart: ' + this.props.selectedFavourite.title;
           console.log('RENDERING CHART, SERIES:');
           console.log(this.props.data);
		         dataContent = (
             <Bootstrap.ListGroup fill>
               <Bootstrap.ListGroupItem className="report-chart-wrapper">
                 <Chart 
                   {...defaults.chartProps}
                   draw={this.props.draw}
                   field={this.props.selectedFavourite.field} 
                   level={this.props.selectedFavourite.level} 
                   reportName={this.props.selectedFavourite.reportName} 
                   finished={this.props.finished}
                   series={this.props.data}
                   context={this.props.config}
                 />
              </Bootstrap.ListGroupItem>
            </Bootstrap.ListGroup>
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
         self.clickedDeleteFavourite(this.props.row);
				   }
			  }],
			  rows: this.props.favourites ? this.props.favourites : [],
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

    if(this.props.showDeleteMessage){
      var modal;
      var warning = 'Delete Announcement?';
			   var actions = [{
				      action: this.props.actions.closeWarning,
				      name: "Cancel"
			     }, {
				      action: this.props.actions.deleteFavourite,
				      name: "Delete",
				      style: 'danger'
			     }];    
        
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
  		      <Modal show = {this.props.showDeleteMessage}
            onClose = {this.props.actions.closeWarning}
            title = {warning}
            text = {'You are about to delete the favourite with label "' + 
              this.props.favouriteToBeDeleted.namedQuery.title + '". Are you sure?'}
  		        actions = {actions}
  		      />
        </div> 
      );   
    }

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
    profile: state.session.profile,
    showSelected: state.favourites.showSelected,
    selectedFavourite: state.favourites.selectedFavourite,
    favourites: state.favourites.favourites,
    showDeleteMessage: state.favourites.showDeleteMessage,
    favouriteToBeDeleted: state.favourites.favouriteToBeDeleted,
    map: state.favourites.map,
    source: state.favourites.source,
    geometry: state.favourites.geometry,
    population: state.favourites.population,
    interval: state.favourites.interval,
    config: state.config,
    draw: state.favourites.draw,
    finished: state.favourites.finished,
    data: state.favourites.data    
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { setTimezone, fetchFavouriteQueries, 
                                                     openFavourite, closeFavourite, setActiveFavourite, 
                                                     addCopy, deleteFavourite, openWarning, closeWarning, 
                                                     resetMapState, getFavouriteMap, getFavouriteChart, 
                                                     getFeatures}) , dispatch)                                                     
  };
}

Favourites.icon = 'bar-chart';
Favourites.title = 'Section.Analytics.Fav';

module.exports = connect(mapStateToProps, mapDispatchToProps)(Favourites);
