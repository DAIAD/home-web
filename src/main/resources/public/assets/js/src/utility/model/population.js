
// Provide simple representations for population-related entities:
// groups, clusters, ranking. 

// Represent a ranking clause on a data query

class Ranking {
  
  constructor () { 
    if (arguments.length == 1) {
      let {type, field, metric, limit} = arguments[0];
      this._initialize(type, field, metric, limit);
    } else {
      this._initialize(...arguments);
    }
  }

  _initialize (type, field, metric, limit=3) {
    this.type = (['TOP', 'BOTTOM'].indexOf(type) < 0)? 'TOP' : type;
    this.field = field;
    this.metric = (['AVERAGE', 'MIN', 'MAX', 'SUM', 'COUNT'].indexOf(metric) < 0)? 
      'AVERAGE' : metric;
    this.limit = parseInt(limit);
  }

  toJSON () {
    return {
      type: this.type,
      field: this.field,
      metric: this.metric,
      limit: this.limit,     
    };
  }

  toString () {
    return [
      'RANK', this.field, this.metric, this.type, this.limit.toString(), 
    ].join('/');
  }

  get comparator () {
    return (this.type == 'BOTTOM')?
      ((a, b) => (a - b)) : ((a, b) => (b - a));
  }
}

Ranking.fromString = function (label) {
  var re = new RegExp(
    '^(?:RANK)[/](\\w+)[/](AVERAGE|MIN|MAX|SUM|COUNT)[/](TOP|BOTTOM)[/]([\\d]+)$');
  var m = re.exec(label);
  return m? (new Ranking(m[3], m[1], m[2], m[4])) : null;
};

// Represent a population group

class Group {
  
  constructor (key, name=null) {
    this.type = 'GROUP';
    this.name = name;
    this.key = key;
  }

  toJSON () {
    return {
      type: this.type,
      label: this.toString(),
      group: this.key,
    };
  }

  toString () {
    return this.type + ':' + this.key;
  }
}

// Represent the universe of a population group

class Utility extends Group {
  
  constructor (key, name=null) {
    super(key, name);
    this.type = 'UTILITY';
  }
  
  toJSON () {
    return {
      type: this.type,
      label: this.toString(),
      utility: this.key,
    };
  }
}

// Represent a group of population groups (aka cluster)

class Cluster {
  
  constructor (key, name=null) {
    this.key = key;
    this.name = name;
  }
  
  toString () {
    return 'CLUSTER' + ':' + this.key;
  }
  
  toJSON () {
    return {
      type: 'CLUSTER',
      label: this.toString(),
      cluster: this.key,
    };  
  }
}

// Represent a population group that belongs to a certain cluster

class ClusterGroup extends Group {
  
  constructor (clusterKey, key, name=null) {
    super(key, name);
    this.clusterKey = clusterKey;
  }
  
  toString () {
    return 'CLUSTER' + ':' + this.clusterKey + ':' + this.key;
  }
}

// A factory for Group instances

Group.fromString = function (label) {

  var r, m = (new RegExp('^([\\w]+)(?:[:]([-\\w]+))?[:]([^/]+)$')).exec(label);
  if (!m)
    return null;
  
  switch (m[1]) {
    case 'GROUP':
      r = (m[2] == null)? (new Group(m[3])) : null;
      break;
    case 'UTILITY':
      r = (m[2] == null)? (new Utility(m[3])) : null;
      break;
    case 'CLUSTER':
      r = (m[2] != null)? (new ClusterGroup(m[2], m[3])) : null;
      break;
    default:
      r = null;
      break;
  }
  return r;
};

// A factory for cluster instances

Cluster.fromString = function (label) {  
  var m = (new RegExp('^CLUSTER[:]([-\\w]+)$')).exec(label);
  return !m? null : (new Cluster(m[1]));
};

// Parse label and create a [Group, Ranking] pair
// This pair represents a population target for data (aka measurement) queries

var fromString = function (label) {
  var g, r;  
  var i = label.indexOf('/');
  if (i < 0) {
    g = Group.fromString(label) || Cluster.fromString(label);
    r = null;
  } else {
    var label1 = label.substr(0, i), label2 = label.substr(i +1);
    g = Group.fromString(label1) || Cluster.fromString(label1); 
    r = Ranking.fromString(label2);
  }
  return g? [g, r] : null;
};

module.exports = {Group, Cluster, Utility, ClusterGroup, Ranking, fromString};
