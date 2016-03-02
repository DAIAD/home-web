var React = require('react');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');
var Select = require('react-select');
var DateRangePicker = require('react-bootstrap-daterangepicker');

var Wizard = require('./Wizard');
var Message = require('./Message');

var AnalysisJobParameters  = React.createClass({
    getInitialState: function() {
        return {
        	granularity: 'Day',
            population: [{ value: 'Alicante', label: 'Alicante', type: 1 },
                         { value: 'User 1', label: 'User 1', type: 2 }],
            source: 'Both',
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
        
  		var onChangeSource = function(val) {
  			this.setState({
  				source: val.value
  			});
        };        
        
  		var onChangePopulation = function(val) {
  			this.setState({
  				population: (val ? val.map( (item) => { return item.value; } ) : [])
  			});
        };

        var renderOption = function(option) {
        	switch(option.type){
        		case 1:
            		return <span><i className='fa fa-group fa-fw'></i>{option.label}</span>;
        		case 2:
        			return <span><i className='fa fa-user fa-fw'></i>{option.label}</span>;
        	}
    	};

    	var onChangeInterval = function (event, picker) {
    		this.setState({
    			interval: [picker.startDate, picker.endDate]
    		});
    	};
    	
        var intervalConfig, populationConfig, sourceConfig, granularityConfig;
        sourceConfig = (
				<div className='form-group col-md-4'>
					<label className='col-md-3 control-label' htmlFor='source'>Data Source</label>  
					<div className='col-md-9'>
						<Select name='source'
							value={this.state.source}
	  		            	options={[
		            	          { value: 'Both', label: 'Both' },
		            	          { value: 'Meter', label: 'Meter' },
		            	          { value: 'Amphiro', label: 'Amphiro' },
		                    ]}
	  		            	onChange={onChangeSource.bind(this)}
							clearable={false} 
						/>
						<span className='help-block'>Select data source</span>  
					</div>
				</div>
			);
		
		granularityConfig = (
			<div className='form-group col-md-4'>
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

		populationConfig = (
				<div className='form-group col-md-4'>
					<label className='col-md-3 control-label' htmlFor='population'>Population</label>  
					<div className='col-md-9'>
						<Select name='population'
							multi={true}
							value={this.state.population}
	  		            	options={[
  		            	          { value: 'Alicante', label: 'Alicante', type: 1 },
  		            	          { value: 'St. Albans', label: 'St. Albans', type: 1 },
  		            	          { value: 'User 1', label: 'User 1', type: 2 },
  		                    ]}
							optionRenderer={renderOption.bind(this)}
	  		            	onChange={onChangePopulation.bind(this)}
							clearable={true} 
						/>
						<span className='help-block'>Select users or groups</span>  
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
			<div className='form-group col-md-4'>
				<label className='col-md-3 control-label' htmlFor='interval'>Interval</label>  
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
			<div className='row'>
				<div className='col-md-12'>  
					<div className='row'>
						{intervalConfig}
					</div>
					<div className='row'>
						{populationConfig}
					</div>
					<div className='row'>
						{granularityConfig}
					</div>
					<div className='row'>
						{sourceConfig}
					</div>
				</div>
			</div>
 		);
  	}
});


module.exports = AnalysisJobParameters;
