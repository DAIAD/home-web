var React = require('react');
var Link = require('react-router').Link;
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { FormattedMessage, FormattedRelative } = require('react-intl');
var { IMAGES } = require('../constants/HomeConstants');

//var SessionsChart = require('./SessionsChart');


function SessionListItem (props) {
  const { firstname } = props;
  const { id, index, device, devType, devName, volume, difference, energyClass, timestamp, duration, better, temperature, history, measurements } = props.data;
  const arrowClass = better===null?"":better?"fa-arrow-down green":"fa-arrow-up red";
  const highlight = devType === 'METER' ? difference : volume;
  const mu = ' lt';
  return (
    <li className="session-item"> 
      <a onClick={() => props.onOpen(device, id, timestamp)} >
        
        <ul className="session-item-details"> 
          <li className='col-md-2'> 
            <span style={{fontSize: '2.5em'}}>{highlight}<span style={{fontSize: '0.6em'}}>{mu}</span></span>
        </li>
            {(() => devType === 'AMPHIRO' ?
            <li>
              <span>{devName}</span>
            </li>
              :
              <span/>
            )()}
            <li>
              <span>{firstname}</span>
            </li>
            <li>
              <span><i className="fa fa-calendar"/><FormattedRelative value={timestamp}/></span> 
            </li>
            {(() => duration ? 
             <li>
              <span><i className="fa fa-clock-o"/>{duration}</span>
            </li>
             :
             <span/>
            )()}
            {(() => energyClass ? 
             <li>
              <span><i className="fa fa-flash"/>{energyClass}</span>
            </li>
            :
            <span/>
            )()}
            {(() => temperature ? 
             <li>
              <span><i className="fa fa-temperature"/>{temperature} ºC</span>
            </li>
            :
            <span/>
            )()}
            {(() => id!=null ? 
               <li>
                <span style={{fontSize: '0.8m!important'}} >{`#${id}`}</span>
              </li>
              :
              <span/>
            )()}
        {
          /*
             <SparklineChart history={history} data={measurements} intl={props.intl}/>
          */
          }
          <li className="pull-right">
            <img src={`${IMAGES}/arrow-big-right.svg`} />
          </li>
      </ul>
    </a>
  </li>
  );
}

/*
function SparklineChart (props) {
  if (!props.data || !props.data.length || props.data.length<=1 || props.history) {
    return (<h3>-</h3>);
  }
  return (
    <SessionsChart
      height={80}
      width='100%'  
      title=""
      subtitle=""
      mu=""
      formatter={(x) => props.intl.formatDate(x)}
      type="line"
      sparkline={true}
      xMargin={5}
      yMargin={0}
      x2Margin={2}
      y2Margin={0}
      data={[{title: 'Consumption', data:props.data}]}
    />);
}
*/

var SessionsList = React.createClass({

  onOpen: function (device, id, timestamp) {
    this.props.setActiveSession(device, id, timestamp);
  },
  /*
  onClose: function() {
    this.props.resetActiveSessionIndex();
    //set session filter to volume for sparkline
    this.props.setSessionFilter('volume');
  },
  onNext: function() {
    this.props.increaseActiveSessionIndex();
    //this.props.getActiveSession(this.props.activeDevice, this.props.time);
    //this.props.getDeviceSession(this.props.nextSessionId, this.props.nextDevice, this.props.time);  
  },
  onPrevious: function() {
    this.props.decreaseActiveSessionIndex();
    //this.props.getActiveSession(this.props.activeDevice, this.props.time);
    //this.props.getDeviceSession(this.props.previousSessionId, this.props.previousDevice, this.props.time);  
    },
    */
  render: function() {
    const { sortOptions, sortFilter, sortOrder, handleSortSelect, csvData } = this.props;
    return (
      <div className="history-list-area">
        <div className="history-list-header">
          <h3 style={{float: 'left'}}>In detail</h3>

          { 
            csvData ?  
           (<a style={{float: 'left', marginLeft: 10}} className='btn' href={"data:application/csv;charset=utf-8,"+csvData}
                download="Data.csv">CSV</a>)
             :
             <span/>
             }
          <div style={{float: 'right'}}> 
            <h5 style={{float: 'left', marginTop: 5}}>Sort by:</h5>
            <div className="sort-options" style={{float: 'right', marginLeft:10, textAlign: 'right'}}>
              <bs.DropdownButton
                title={sortOptions.find(sort=> sort.id===sortFilter)?sortOptions.find(sort=> sort.id===sortFilter).title:'Volume'}
                id="sort-by"
                defaultValue={sortFilter}
                onSelect={handleSortSelect}>
                {
                  sortOptions.map(sort => 
                     <bs.MenuItem key={sort.id} eventKey={sort.id} value={sort.id} >{sort.title}</bs.MenuItem>)
                } 
              </bs.DropdownButton>

            <div style={{float: 'right', marginLeft: 10}}>
              {
                (() => sortOrder === 'asc' ? 
                 <a onClick={()=> this.props.setSortOrder('desc')}><i className="fa fa-arrow-down"/></a>
                   :
                     <a onClick={()=> this.props.setSortOrder('asc')}> <i className="fa fa-arrow-up"/></a>
                 )()
              }
            </div>
          </div>
        
        </div>
      </div>

        <ul className="sessions-list">
          {
            this.props.sessions.map((session, idx) => (
              <SessionListItem
                firstname={this.props.firstname}
                intl={this.props.intl}
                key={idx}
                index={idx}
                data={session}
                onOpen={this.onOpen}
              />  
              ))
          }
        </ul>
        </div>
    );
  }
});

module.exports = SessionsList;
