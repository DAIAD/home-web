var React = require('react');
var bs = require('react-bootstrap');
var classNames = require('classnames');

var intl = require('react-intl');
var { injectIntl, FormattedMessage, FormattedRelative } = require('react-intl');

var { Responsive, WidthProvider } = require('react-grid-layout');
var ResponsiveGridLayout = WidthProvider(Responsive);
//const { reduxForm } = require('redux-form');
var { Link } = require('react-router');

var PureRenderMixin = require('react-addons-pure-render-mixin');

var MainSection = require('../layout/MainSection');

var ChartBox = require('../helpers/ChartBox');

const { IMAGES } = require('../../constants/HomeConstants');


var timeUtil = require('../../utils/time');

function ErrorDisplay (props) {
  return props.errors ? 
    <div style={{ zIndex: 100}}>
      <img src={`${IMAGES}/alert.svg`} /><span className="infobox-error">{`${props.errors}`}</span>
    </div>
    :
     (<div/>);
}


/* Be Polite, greet user */
function SayHello (props) {
  return (
    <div style={{margin: '40px 30px 20px 30px'}}>
      <h3><FormattedMessage id="dashboard.hello" values={{name:props.firstname}} /></h3>
    </div>
  );
}

function InfoBox (props) {
  const { mode, infobox, updateInfobox, removeInfobox, intl, linkToHistory, } = props;
  const { id, error, period, type, display, periods, displays, time } = infobox;

  const _t = intl.formatMessage;
  return (
    <div className='infobox'>
      <div className='infobox-header'>
        <div className='header-left'>
          <h4>{infobox.title}</h4>
        </div>

        <div className='header-right'>
          <div style={{marginRight:10}}>
            {
              /*displays.map(t => t.id!==display?(
                <a key={t.id} onClick={() => updateInfobox(id, {display:t.id})} style={{marginLeft:5}}>{t.title}</a>
                ):<span key={t}/>)
                */
            }
          </div>
          
          <div>
            {
              periods.map(p => (
                <a key={p.id} onClick={() => updateInfobox(id, {period:p.id})} style={{marginLeft:5}}>{(p.id===period)?(<u>{_t({id: p.title})}</u>):(_t({id: p.title}))}</a>
                ))
            }
          </div>
          {
            (() => type === 'last' && time ? 
             <FormattedRelative value={time} /> : <span/>
             )()
          }
          {
            //TODO: disable delete infobox until add is created
               <a className='infobox-x' style={{float: 'right', marginLeft: 5, marginRight:5}} onClick={()=>removeInfobox(infobox.id)}><i className="fa fa-times"></i></a>
          }
        </div>
      </div>
      
      <div className='infobox-body'>
         {
           (()=>{
             if (error) {
               return (<ErrorDisplay errors={error} />);
               } 
             else {
               if (display==='stat') {
                 return (
                   <StatBox {...props} /> 
                 );
               } 
               else if (display==='chart') {
                 return (
                   <ChartBox {...infobox} /> 
                   );
               }
               else if (display==='tip') {
                 return (
                   <TipBox {...props} />
                   );
               }
             }
           })()
         }
       </div>
       <div className='infobox-footer'>
         <a onClick={() => linkToHistory(infobox)}>See more</a>
       </div>
    </div>
  );
}

function StatBox (props) {
  const { id, title, type, deviceType, improved, data, highlight, metric, measurements, period, device, deviceDetails, index, time, better, comparePercentage, mu } = props.infobox;
  let improvedDiv = <div/>;
  if (improved === true) {
    improvedDiv = (<img src={`${IMAGES}/success.svg`}/>);
  }
  else if (improved === false) {
    improvedDiv = (<img src={`${IMAGES}/warning.svg`}/>);
  }
  const duration = data?(Array.isArray(data)?null:data.duration):null;
  const arrowClass = better?"fa-arrow-down green":"fa-arrow-up red";
  const bow = (better==null || comparePercentage == null) ? false : true;
  return (
    <div>
      <div style={{float: 'left', width: '50%'}}>
        <h2>{highlight}<span style={{fontSize:'0.5em', marginLeft:5}}>{mu}</span></h2>
      </div>
      <div style={{float: 'left', width: '50%'}}>
        <div>
          {
            (() => bow ? 
             <span><i className={`fa ${arrowClass}`}/>{deviceType === 'AMPHIRO' ? (better ? `${comparePercentage}% better than last ${period}!` : `${comparePercentage}% worse than last ${period}`): (better ? `${comparePercentage}% better than last ${period} so far!` : `${comparePercentage}% worse than last ${period} so far`)}</span>
             :
               <span>No comparison data</span>
               )()
          }
        </div>
      </div>
    </div>
  );
}

function TipBox (props) {
  const { title, type, highlight } = props.infobox;
  return (
    <div >
      <p>{highlight}</p>
    </div>
  );
}

function InfoPanel (props) {
  const { mode, layout, infoboxes, updateLayout, switchMode,  updateInfobox, removeInfobox, chartFormatter, intl, periods, displays, linkToHistory } = props;
  return (
      <ResponsiveGridLayout 
        className='layout'
        layouts={{sm: layout}}
        breakpoints={{sm: 850}}
        cols={{sm: 4}}
        rowHeight={160}
        draggableHandle='.infobox-header'
        isDraggable={true}
        isResizable={false}
        onBreakpointChange={(newBreakpoint, newCols) => {
          //console.log('new break!', newBreakpoint, newCols);
          //return newCols;
        }}
        onResizeStop={(layout, oldItem, newItem, placeholder) => { 
          updateLayout(layout);  
        }}
        onDragStop={(layout) => {
          updateLayout(layout); 
        }}
       >
       {
         infoboxes.map(function(infobox) {
           return (
             <div key={infobox.id}>
               <InfoBox {...{mode, periods, displays, chartFormatter, infobox, updateInfobox, removeInfobox, intl, linkToHistory}} /> 
           </div>
           );
         })
       }
      </ResponsiveGridLayout>
  );
}

function ButtonToolbar (props) {
  const { switchMode, setDirty, resetDirty, saveToProfile, mode, dirty } = props;
  return (
      <div className='dashboard-button-toolbar'>
        <a className='btn dashboard-add-btn' onClick={()=> switchMode("add")} active={false}>Add Widget</a>
        {
          (()=> dirty === true ?(
            <div className='dashboard-save'>
            <h6>Save changes?</h6>
              <div className='dashboard-save-prompt'>
                <a className='btn dashboard-save-btn' onClick={()=> saveToProfile().then(() => resetDirty())} active={false}>Yes</a>
                <a className='btn dashboard-discard-btn' onClick={()=> resetDirty()} active={false}>No</a>
              </div>
            </div>
            ):(
            <div/>
            ))()
        }
      </div>
  );
}

function AddInfoboxForm (props) {
  const {infoboxToAdd, metrics, types, deviceTypes, setForm } = props;
  const setInfoboxToAdd = (data) => setForm('infoboxToAdd', data);
  const { deviceType, title, type } = infoboxToAdd;
  return (
    <div>
      
      <bs.Tabs 
        className="history-time-nav" 
        position='top' tabWidth={3} 
        activeKey={deviceType} 
        onSelect={(key) => setInfoboxToAdd({deviceType: key, title: null, type: key === 'METER' ? 'totalDifferenceStat' : 'totalVolumeStat'})}
        >
               {
                deviceTypes.map(devType =>
                            <bs.Tab key={devType.id} eventKey={devType.id} title={devType.title} />)
               } 
       </bs.Tabs>

      
      <div className="add-infobox">
        <div className="add-infobox-left">
          
          <div>
            <ul className='add-infobox-types'>
            {
              types.map((t, idx) =>
                        <li key={idx}>
                          <a className={(type ===t.id)?'selected':''}  onClick={() => setInfoboxToAdd({type: t.id, title: null})} value={t.id}>
                            {t.title}</a>
                        </li>
              )
            }
          </ul>
          </div>    
        </div>

        <div className="add-infobox-right">
          <div>
            <input 
              type='text' 
              placeholder='Enter title...'
              value={title || (types.find(t => t.id === type) ? types.find(t => t.id === type).title : null)}
              onChange={(e) => setInfoboxToAdd({title: e.target.value})}
            />
            <p>{types.find(t => t.id === infoboxToAdd.type) ? types.find(t => t.id === infoboxToAdd.type).description :null }</p>
          </div>
          
        </div>
      </div>
       
    </div>
  );
}

function AddInfoboxModal (props) {
  const { showModal, switchMode, addInfobox, metrics, types, deviceTypes, infoboxToAdd, setForm } = props;
  return (
    <bs.Modal animation={false} className='add-infobox-modal' show={showModal} onHide={() => switchMode('normal')} bsSize="large" backdrop='static'>
        <bs.Modal.Header closeButton>
          <bs.Modal.Title>
            <FormattedMessage id="dashboard.add" />
          </bs.Modal.Title>
        </bs.Modal.Header>
        <bs.Modal.Body>
          
          <AddInfoboxForm {...{infoboxToAdd, metrics, types, deviceTypes, setForm}}/> 

        </bs.Modal.Body>
        <bs.Modal.Footer>
          <a onClick={() => switchMode('normal')}>Cancel</a>
          <a style={{marginLeft: 20}} onClick={() => { addInfobox(); switchMode('normal');}}>Add</a>
        </bs.Modal.Footer>
      </bs.Modal> 

  );
}

var Dashboard = React.createClass({
  mixins: [ PureRenderMixin ],

  componentWillMount: function() {
    const { fetchAllInfoboxesData, switchMode } = this.props;
    //switchMode("normal");
    //fetchAllInfoboxesData();

  },
  /*
  componentWillReceiveProps: function(nextProps) {
    console.log('history receiving props');   
    //console.log(nextProps);
    //console.log(this.props);
    for (let key in nextProps) {
      let prop = nextProps[key];
      if (typeof prop === 'function') { continue; }
      if (this.props[key] === prop) { continue; }
      console.log('new', key, prop);
      console.log('old', key, this.props[key]);
    }

   
  },
  */
  render: function() {
    const { firstname, mode, dirty, switchMode, addInfobox, saveToProfile, setDirty, resetDirty, deviceCount, meterCount, metrics , types, deviceTypes, infoboxToAdd, setForm } = this.props;
    return (
      <MainSection id="section.dashboard">
        <div className='dashboard'>
          <div className='dashboard-infopanel'>

            <SayHello firstname={firstname} />
            <AddInfoboxModal {...{showModal:mode==='add', switchMode, addInfobox, infoboxToAdd,  metrics, types, deviceTypes, setForm }}/>
            
            <InfoPanel {...this.props} />
          </div>
          <div className='dashboard-right'>
            <div className='dashboard-device-info'>
              <Link to='/settings/devices'><h6><img src={`${IMAGES}/amphiro_small.svg`} /><span>{deviceCount > 1 ? `${deviceCount} devices` : (deviceCount == 1 ? `1 device` :`No devices`)}</span></h6></Link>
              <Link to='/settings/devices'><h6><img src={`${IMAGES}/water-meter.svg`} /><span>{meterCount > 1 ? `${meterCount} SWMs` : (meterCount == 1 ? `1 SWM` :`No SWM`)}</span></h6></Link>
            </div>
            <ButtonToolbar {...{switchMode, setDirty, resetDirty, saveToProfile, mode, dirty}}/>
          </div>
        </div>
        
      </MainSection>
    );
  }
});

//Dashboard = injectIntl(Dashboard);
module.exports = Dashboard;
