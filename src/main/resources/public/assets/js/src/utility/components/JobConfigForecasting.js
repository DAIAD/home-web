var React = require('react');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');
var Select = require('react-select');
var DateRangePicker = require('react-bootstrap-daterangepicker');
var Scheduler = require('./Scheduler');
var ForecastingJobParameters = require('./ForecastingJobParameters');

var JobConfigForecasting = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
    getInitialState: function() {
        return {
        	job: null,
			description: '',
            type: 'Analysis',
            granularity: 'Day',
            population: [{ value: 'Alicante', label: 'Alicante', type: 1 },
                         { value: 'User 1', label: 'User 1', type: 2 }],
            source: 'Both',
            ranges: {
				'Next 7 Days': [moment(), moment().add(7, 'days')]
			},
			interval:null
        };
    },

  	render: function() {
        var descriptionConfig, jobConfig, scheduleConfig, paramtersConfig;
        
        // Description
   	    var setDescription=  function(input) {
   	    	this.setState({
   				description: input.value
   			});
   	    };

        descriptionConfig = (
    		<div className='form-group col-md-12'>
				<label className='col-md-3 control-label' htmlFor='description'>Description</label>  
				<div className='col-md-9'>
					<Bootstrap.Input
						name='description'
						type="text"
			    		value={this.state.description}
						onChange={setDescription.bind(this)}
						placeholder="Enter job description ..." />
					<span className='help-block'>Job execution description</span>
				</div>
			</div>
		);

  		// Job
        var jobs = [
	       { value: 'Job-1', label: 'Weekly forecasting using weather forecast and historical data' }
  		];

   		var onChangeJob = function(val) {
   			this.setState({
   				job: (val ? val.value : null)
   			});
   		};

   		jobConfig = (				
			<div className='form-group col-md-12'>
 				<label className='col-md-3 control-label' htmlFor='type'>Job</label>  
 				<div className='col-md-9'>
 					<Select name='type'
 						value={this.state.job}
 			            	options={jobs}
 			            	onChange={onChangeJob.bind(this)}
 							clearable={false} 
 					/>
 					<span className='help-block'>Select job for execution</span>  
 				</div>
 			</div>
 		);

   		// Schedule
   		if(this.state.job !== null) {
   			scheduleConfig = (
				<div className='form-group col-md-12'>
   					<Scheduler />
				</div>
			);
   		}
   		
   		// Chart
   		if(this.state.job !== null) {
   			paramtersConfig = (
				<ForecastingJobParameters />
   			);
   		}

		return (
			<div className='clearfix' style={{ paddingTop: 10 }}>
				<div className='row'>
					<div className='col-md-4'>
						<div className='row'>
							{descriptionConfig}
		    			</div>
		    			<div className='row'>
							{jobConfig}
						</div>
					</div>
					<div className='col-md-4'>
						<div className='row'>
							{scheduleConfig}
						</div>
					</div>
				</div>
				{paramtersConfig}
				<div className='row' style={{float: 'right', paddingRight: 20}}>
					<div className='form-group col-md-6'>
						<button id='Execute'
			   				className='btn btn-success'>
							<i className='fa fa-cog fa-fw'></i>
							Execute
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

module.exports = JobConfigForecasting;
