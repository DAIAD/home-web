var React = require('react');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');
var Select = require('react-select');
var DateRangePicker = require('react-bootstrap-daterangepicker');
var DateTimeField = require('react-bootstrap-datetimepicker');

var Message = require('./Message');

var Scheduler = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
    getInitialState: function() {
        return {
        	repeat: 'None',
			startDate:moment(),
			minDate: moment()
        };
    },

  	render: function() {
        var startDateConfig, repeatConfig, repeatIntervalConfig;

  		// Repeat
  		var repeatOptions = [
            	{ value: 'None', label: 'None (run once)' },
            	{ value: 'Second', label: 'Per Second' },
            	{ value: 'Minute', label: 'Per Minute' },
            	{ value: 'Hour', label: 'Hourly' },
            	{ value: 'Day', label: 'Daily' },
            	{ value: 'Week', label: 'Weekly' },
            	{ value: 'Month', label: 'Monthly' },
            	{ value: 'Year', label: 'Yearly' }
        ];

        
   		var onChangeRepeat = function(val) {
   			this.setState({
   				repeat: val.value
   			});
        };    	

        switch(this.state.repeat) {
        	case 'Day':
        		repeatIntervalConfig = (
    			<div>
	 				<label className='col-md-2 control-label' style={{paddingTop: 8}} htmlFor='every'>every</label>  
	 				<div className='col-md-5'>
	 					<Select name='every'
	 						value={this.state.repeat}
	   		            	options={repeatOptions}
	   		            	onChange={onChangeRepeat.bind(this)}
	 						clearable={false} 
	 					/> 
	 				</div>
				</div>
        		);
        		break;
        	default:
        		repeatIntervalConfig = ( <span /> );
        }

        repeatConfig = (
 			<div className='form-group col-md-12'>
 				<label className='col-md-4 control-label' style={{paddingTop: 8}} htmlFor='repeat'>Repeat</label>  
 				<div className='col-md-8'>
 					<Select name='repeat'
 						value={this.state.repeat}
   		            	options={repeatOptions}
   		            	onChange={onChangeRepeat.bind(this)}
 						clearable={false} 
 					/>
 					<span className='help-block'>Select recurrence frequency</span>  
 				</div>
 				{repeatIntervalConfig}
 			</div>
 		);

        // Start date
        var label ='';
        if(this.state.startDate) {
        	label = this.state.startDate.format('DD/MM/YYYY');
        }

      	var onChangeStartDate = function (event, picker) {
     		this.setState({
     			startDate: picker.startDate
     		});
     	};
     	
     	startDateConfig = (
 			<div className='form-group col-md-12'>
 				<label className='col-md-4 control-label' htmlFor='dimension'>Date Start</label>  
 				<div className='col-md-8'>
 				<DateTimeField inputFormat='DD/MM/YYYY hh:mm A'/>
         			<span className='help-block'>Select start date</span>
     			</div>
 			</div>
 		);
  		 		
     	// Repeat interval
     	
		return (
			<div className='col-md-12'>
				<div className='row'>
					{startDateConfig}					
				</div>
    			<div className='row'>
					{repeatConfig}
				</div>
			</div>
 		);
  	}
});

module.exports = Scheduler;
