var React = require('react');
var ReactDOM = require('react-dom');

var moment = require('moment');

var Select = require('react-select');
var DateRangePicker = require('react-bootstrap-daterangepicker');

var FormattedMessage = require('react-intl').FormattedMessage;
var FormattedNumber = require('react-intl').FormattedNumber;

var TimeDimensionSelect = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
    getInitialState: function() {
        return {
            granularity: 'Day',
            ranges: {
				'Today': [moment(), moment()],
				'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
				'Last 7 Days': [moment().subtract(6, 'days'), moment()],
				'Last 30 Days': [moment().subtract(29, 'days'), moment()],
				'This Month': [moment().startOf('month'), moment().endOf('month')],
				'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
			},
			interval:null
        };
    },

  	render: function() {
   		var onChangeGranularity = function(val) {
   			this.setState({
   				granularity: val.value
   			});
   		};
                 
     	var onChangeInterval = function (event, picker) {
     		this.setState({
     			interval: [picker.startDate, picker.endDate]
     		});
     	};
     	
        var intervalConfig, granularityConfig;
 		
 		granularityConfig = (
 			<div className='form-group col-md-12'>
 				<label className='col-md-3 control-label' htmlFor='granularity'>Granularity</label>  
 				<div className='col-md-9'>
 					<Select name='granularity'
 						value={this.state.granularity}
   		            	options={[
 	            	          { value: 'Hour', label: 'Hour' },
 	            	          { value: 'Day', label: 'Day' },
 	            	          { value: 'Month', label: 'Month' },
 	                    ]}
   		            	onChange={onChangeGranularity.bind(this)}
 						clearable={false} 
 					/>
 					<span className='help-block'>Select time granularity for time interval</span>  
 				</div>
 			</div>
 		);

        var label ='';
        if(this.state.interval) {
         	var start = this.state.interval[0].format('DD/MM/YYYY');
         	var end = this.state.interval[1].format('DD/MM/YYYY');
         	label = start + ' - ' + end;
         	if (start === end) {
         		label = start;
         	}
        }
         
 		intervalConfig = (
 			<div className='form-group col-md-12'>
 				<label className='col-md-3 control-label' htmlFor='dimension'>Interval</label>  
 				<div className='col-md-9'>
 					<DateRangePicker	startDate={this.state.interval ? this.state.interval[0] : moment() } 
 										endDate={this.state.interval ? this.state.interval[1] : moment()} 
 										ranges={this.state.ranges} 
 										onEvent={onChangeInterval.bind(this)}>
 						<div className='clearfix Select-control' style={{ cursor: 'pointer', padding: '5px 10px', width: '100%'}}>
 							<span>{label}</span>
 						</div>
         			</DateRangePicker>
         			<span className='help-block'>Select data time interval</span>
     			</div>
 			</div>
 		);
		
 		
   		return (
 			<div className='clearfix' style={{ paddingTop: 10 }}>
 				<div className='row'>
					{intervalConfig}
				</div>
				<div className='row'>
					{granularityConfig}
				</div>
 				<div className='row' style={{float: 'right', paddingRight: 20}}>
 					<div className='form-group col-md-6'>
 						<button id='Execute'
 			   				className='btn btn-success'>
 							<i className='fa fa-save fa-fw'></i>
 							Save
 			   			</button>
 	   				</div>
 	   				<div className='form-group col-md-6'>
 						<button id='Cancel'
 			   				className='btn btn-danger'>
 							<i className='fa fa-remove fa-fw'></i>
 			   				Cancel
 			   			</button>
 	   				</div>
 				</div>
 			</div>
  		);
   	}
 });


module.exports = TimeDimensionSelect;
