var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var UtilityDropDown = require('../UtilityDropDown');

var ManageAlertsActions = require('../../actions/ManageAlertsActions');
var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var { getUtilities } = require('../../actions/ManageAlertsActions');

//var utilityOptions2;

    var ManageAlerts = React.createClass({
        contextTypes: {
            intl: React.PropTypes.object
        },
        getDefaultProps: function() {
            return {               
                defaultDropDownTitle: 'Select Utility',
                utilityOptions2 : ManageAlertsActions.getUtilities()               
	    };
            
	},
        getInitialState: function() {            
            console.log('initial state');                   
            return {value: 'initial return'};     
        },                       
        render: function() {
            console.log('rendering MANAGE ALERTS');
            
            
            if (!this.props.isLoading ){ //&& this.props.tips
                console.log('not loading');
                
                
            var _t = this.context.intl.formatMessage;
            var data = { 
            filters: [{
            id: 'utilityName',
                name: 'Utility',
                field: 'utilityName',
                icon: 'utility',
                type: 'text'
            }],

            fields: [{
            name: 'id',
                title: 'ID',
                hidden: false
            }, {
            name: 'text',
                title: 'Title'
            }, {
            name: 'description',
                title: 'Description',
                type: 'description'
            }, {
            name: 'createdOn',
                title: 'Created On',
                type: 'datetime'
            }, {
            name: 'modifiedOn',
                title: 'Modified On',
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
//            rows: [this.props.tips ? this.props.tips : {
//            id: 1,
//                text: 'Consider buying an efficient showerhead',
//                description: 'Modern showerheads mix water with air to save water without affecting your shower comfort. You will have the same pressure but spend less water. Using one can cut your shower water use in half!',
//                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
//                modifiedOn: new Date((new Date()).getTime() + (2 + Math.random()) * 3600000),
//                status: 'Pending',
//                acknowledged: false
//            }],
            //test
            rows: [this.props.tips ? {id:this.props.tips[4].id, text: this.props.tips[4].title, description : this.props.tips[4].description } : "lala"],
            pager: {
                index: 0,
                    size: 15,
                    count:1
                }
            };

            var utilityOptions = [{label: 'Alicante', value: '1', key: '1' },{label: 'St. Albans', value: '2', key: '2'},{label: 'DAIAD', value: '3', key: '3'}];
            
            const staticTipsTitle = (
               < span >
                   < i className = 'fa fa-list-ol fa-fw' > < /i>
                       < span style = {{ paddingLeft: 4 }} > Static Tips < /span>
                           < span style = {{float: 'right', marginTop: - 3, marginLeft: 5 }} >
                               < Bootstrap.Button	bsStyle = "default" className = "btn-circle" >
                                   < Bootstrap.Glyphicon glyph = "plus" />
                               < /Bootstrap.Button>
                       < /span>
               < /span>
           );

           const filter = (
               < span >
                   < i className = 'fa fa-filter fa-fw' > < /i>
                       < span style = {{ paddingLeft: 4 }} > Filter < /span>
                           < span style = {{float: 'left', marginTop: - 3, marginLeft: 5 }} >

                       < /span>
               < /span>
           );
           return (
               < div className = "container-fluid" style = {{ paddingTop: 10 }} >
                   < div className = "row" >
                       < div className = "row" >
                           < Breadcrumb routes = {this.props.routes} />
                       < /div>
                   < /div>
                   < div className = "row" >
                       < div className = "row" >
                            
                           < Bootstrap.Panel header = {filter} >       
                               <UtilityDropDown  
                                   title = {this.props.utility ? this.props.utility.label : this.props.defaultDropDownTitle}                                   
                                   options={utilityOptions}
                                   disabled={false}
                                   onSelect={this.props.setUtility}  

                               />
                             < /Bootstrap.Panel>
                       < /div>
                       < div className = "row" >

                           < Bootstrap.Panel header = {staticTipsTitle} >
                               < Bootstrap.ListGroup fill >
                                   < Bootstrap.ListGroupItem >
                                       < Table data = {data} > < /Table>
                                   < /Bootstrap.ListGroupItem>
                               < /Bootstrap.ListGroup>
                           < /Bootstrap.Panel>
                       < /div>
                   < /div>
               < /div>
           );                
                
                
                
 
                
            } else {
                return (
                  <div>
                    <img className='preloader' src='/assets/images/utility/preloader-counterclock.png' />
                    <img className='preloader-inner' src='/assets/images/utility/preloader-clockwise.png' />
                  </div>
                );
            }
            
            

        }
    });

ManageAlerts.icon = 'server';
ManageAlerts.title = 'Section.ManageAlerts';


function mapStateToProps(state) {
    console.log('state utility: ' + state.alerts.utility);
    //console.log('state tips: ' + state.alerts.tips);

    for(var obj in state.alerts.tips){
        console.log('\n\NEW \n\ ');
       if(state.alerts.tips.hasOwnProperty(obj)){
           for(var prop in state.alerts.tips[obj]){
               if(state.alerts.tips[obj].hasOwnProperty(prop)){
                   console.log(prop + ':' + state.alerts.tips[obj][prop]);
               }
           }
        }    
    }         
    

    return {
        utility: state.alerts.utility,
        tips: state.alerts.tips
    };
}

function mapDispatchToProps(dispatch) {
    return {
                      
        setUtility: function (event, utility){
            console.log('mapDispatchToProps setUtility');
            dispatch(ManageAlertsActions.setUtility(event, utility));
            //dispatch(ManageAlertsActions.setTips(event, utility)); 
            dispatch(ManageAlertsActions.getStaticTips(event, utility));
                       
        },
        setRowsTips : function (event, utility){
            //console.log('mapDispatchToProps setRowsTips');
            //dispatch(ManageAlertsActions.receivedTips(event, utility)); 
        }
    };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(ManageAlerts);
//            rows: [{
//            id: 1,
//                text: 'Consider buying an efficient showerhead',
//                description: 'Modern showerheads mix water with air to save water without affecting your shower comfort. You will have the same pressure but spend less water. Using one can cut your shower water use in half!',
//                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
//                modifiedOn: new Date((new Date()).getTime() + (2 + Math.random()) * 3600000),
//                status: 'Pending',
//                acknowledged: false
//            }, {
//            id: 2,
//                text: 'Don’t multi-task!',
//                description: 'Some tips advise you to brush your teeth in the shower - DON’T. If you brush for three minutes that’s about 20 liters wasted. Brush in the sink with the tap turned off',
//                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
//                modifiedOn: new Date((new Date()).getTime() + (3 + Math.random()) * 3600000),
//                status: 'Pending',
//                acknowledged: true
//            }, {
//            id: 3,
//                text: 'Consider turning the tap off while brushing your teeth or shaving',
//                description: 'A running tap for a few minutes every day can amount to losses over 1000 liters in a year! Using a glass of water instead can save enough water for 20 showers.',
//                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
//                modifiedOn: new Date((new Date()).getTime() + (10 + Math.random()) * 3600000),
//                status: 'Pending',
//                acknowledged: false
//            }, {
//            id: 4,
//                text: 'Knocking a minute off your shower will save about 4 liters each time',
//                description: 'This is one of the easiest ways to save water and energy. By spending just one minute less in the shower a family of four can save in a year up to 6,000 liters of water and 300 Euros. / In a household usually 40% of the hot water are used for showering. When reducing the shower time you can reduce your hot water consumption.',
//                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
//                modifiedOn: new Date((new Date()).getTime() + (10 + Math.random()) * 3600000),
//                status: 'Pending',
//                acknowledged: false
//            }, {
//            id: 5,
//                text: 'Try showering with slightly less hot water',
//                description: 'You can reduce your energy bills by slightly reducing the temperature of water in the shower. Even 1-2 degrees can make a difference. Give it a try and find a temperature that is comfortable for you / For example for a two person household, showering one minute shorter and with a water temperature of one degree colder as usual, you can save about 100€ a year when using a boiler.',
//                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
//                modifiedOn: new Date((new Date()).getTime() + (10 + Math.random()) * 3600000),
//                status: 'Pending',
//                acknowledged: false
//            }],