//http://stackoverflow.com/questions/46155/validate-email-address-in-javascript
const validateEmail = function(email) {
     const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
};

//flattens nested object
// {a: {a1: '1', a2: '2'}, b: {b1: '1', b2: '2'}} -> 
// {a.a1: '1', a.a2: '2', b.b1: '1', b.b2: '2'}
const flattenMessages = function(nestedMessages, prefix) {
    return Object.keys(nestedMessages).reduce((messages, key) => {
        let value = nestedMessages[key];
        let prefixedKey = prefix ? `${prefix}.${key}` : key;

        if (typeof value === 'string') {
            messages[prefixedKey] = value;
        } else {
            Object.assign(messages, flattenMessages(value, prefixedKey));
        }

        return messages;
    }, {});
};

const addZero = function(input) {
  return input<10?`0${input}`:`${input}`;
};

const getFriendlyDuration = function(seconds) {
  if (!seconds) { return null; }
  
  if (seconds>3600) {
    return  (addZero(Math.floor(seconds/3600))) + ":" +
            addZero(Math.floor((seconds % 3600)/60)) + ":" +
            addZero(Math.floor((seconds % 3600)/60) % 60);
  }
  else if (seconds>60) {
    return addZero(Math.floor(seconds/60)) + ":" +
           addZero(Math.floor(seconds/60)%60);
  }
  else {
    return "00:" + addZero(seconds);
  }
};

const getEnergyClass = function(energy) {
  let scale;
  
  if (energy >= 3675) {
    scale = "G-";
  }
  else if (energy >= 3500) {
    scale = "G";
  }
  else if (energy >= 3325) {
    scale = "G+";
  }
  else if (energy >= 3150) {
    scale = "F-";
  }
  else if (energy >= 2975) {
    scale = "F";
  }
  else if (energy >= 2800) {
    scale = "F+";
  }
  else if (energy >= 2625) {
    scale = "E-";
  }
  else if (energy >= 2450) {
    scale = "E";
  }
  else if (energy >= 2275) {
    scale = "E+";
  }
  else if (energy >= 2100) {
    scale = "D-";
  }
  else if (energy >= 1925) {
    scale = "D";
  }
  else if (energy >= 1750) {
    scale = "D+";
  }
  else if (energy >= 1575) {
    scale = "C-";
  }
  else if (energy >= 1400) {
    scale = "C";
  }
  else if (energy >= 1225) {
    scale = "C+";
  }
  else if (energy >= 1050) {
    scale = "B-";
  }
  else if (energy >= 875) {
    scale = "B";
  }
  else if (energy >= 700) {
    scale = "B+";
  }
  else if (energy >= 525) {
    scale = "A-";
  }
  else if (energy >= 351) {
    scale = "A";
  }
  else if (energy <= 350) {
    scale = "A+";
  }
  return scale;
};

const getMetricMu = function(metric) {
  if (metric === 'showers') return '';
  else if (metric === 'volume' || metric === 'difference') return 'lt';
  else if (metric === 'energy') return 'W';
  else if (metric === 'duration') return 'sec';
  else if (metric === 'temperature') return '°C';
};

const lastNFilterToLength = function (filter) {
  if (filter === 'ten') return 10;
  else if (filter === 'twenty') return 20;
  else if (filter === 'fifty') return 50;
  else throw new Error('unrecognized filter', filter);
};

module.exports = {
  validateEmail,
  flattenMessages,
  getFriendlyDuration,
  getEnergyClass,
  getMetricMu,
  lastNFilterToLength
};
