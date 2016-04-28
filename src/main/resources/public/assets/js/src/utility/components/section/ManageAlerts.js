var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var GroupDropDown = require('../GroupDropDown');

var ManageAlertsActions = require('../../actions/ManageAlertsActions');
var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

    var ManageAlerts = React.createClass({
        contextTypes: {
            intl: React.PropTypes.object
        },
        getDefaultProps: function() {
            return {
                defaultDropDownTitle: 'Select Group'
	    };
	},
        onSelect: function(value) {
            this.setState({groupTitle : value});
	},        
        render: function() {
            console.log('rendering MANAGE ALERTS');
            var _t = this.context.intl.formatMessage;
            var data = {
            filters: [{
            id: 'groupName',
                name: 'Group',
                field: 'groupName',
                icon: 'group',
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

            rows: [{
            id: 1,
                text: 'Consider buying an efficient showerhead',
                description: 'Modern showerheads mix water with air to save water without affecting your shower comfort. You will have the same pressure but spend less water. Using one can cut your shower water use in half!',
                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
                modifiedOn: new Date((new Date()).getTime() + (2 + Math.random()) * 3600000),
                status: 'Pending',
                acknowledged: false
            }, {
            id: 2,
                text: 'Don’t multi-task!',
                description: 'Some tips advise you to brush your teeth in the shower - DON’T. If you brush for three minutes that’s about 20 liters wasted. Brush in the sink with the tap turned off',
                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
                modifiedOn: new Date((new Date()).getTime() + (3 + Math.random()) * 3600000),
                status: 'Pending',
                acknowledged: true
            }, {
            id: 3,
                text: 'Consider turning the tap off while brushing your teeth or shaving',
                description: 'A running tap for a few minutes every day can amount to losses over 1000 liters in a year! Using a glass of water instead can save enough water for 20 showers.',
                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
                modifiedOn: new Date((new Date()).getTime() + (10 + Math.random()) * 3600000),
                status: 'Pending',
                acknowledged: false
            }, {
            id: 4,
                text: 'Knocking a minute off your shower will save about 4 liters each time',
                description: 'This is one of the easiest ways to save water and energy. By spending just one minute less in the shower a family of four can save in a year up to 6,000 liters of water and 300 Euros. / In a household usually 40% of the hot water are used for showering. When reducing the shower time you can reduce your hot water consumption.',
                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
                modifiedOn: new Date((new Date()).getTime() + (10 + Math.random()) * 3600000),
                status: 'Pending',
                acknowledged: false
            }, {
            id: 5,
                text: 'Try showering with slightly less hot water',
                description: 'You can reduce your energy bills by slightly reducing the temperature of water in the shower. Even 1-2 degrees can make a difference. Give it a try and find a temperature that is comfortable for you / For example for a two person household, showering one minute shorter and with a water temperature of one degree colder as usual, you can save about 100€ a year when using a boiler.',
                createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
                modifiedOn: new Date((new Date()).getTime() + (10 + Math.random()) * 3600000),
                status: 'Pending',
                acknowledged: false
            }],

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
                               <GroupDropDown  
                                   title = {this.props.group ? this.props.group.label : this.props.defaultDropDownTitle}                                   
                                   options={utilityOptions}
                                   disabled={false}
                                   onSelect={this.props.setGroup}  

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
        }
    });

ManageAlerts.icon = 'server';
ManageAlerts.title = 'Section.ManageAlerts';


function mapStateToProps(state) {
    return {
        group: state.alerts.group
    };
}

function mapDispatchToProps(dispatch) {
    return {
        setGroup: function (event, group){
            dispatch(ManageAlertsActions.setGroup(event, group));
        }
    };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(ManageAlerts);
