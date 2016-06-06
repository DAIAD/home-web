
//TODO: type label already present in messages
//remove extra maps
const combineMessages = function(categories) {
  return categories.map(cat => 
                            cat.values.map(msg => Object.assign({}, msg, {category: cat.name})))
                       .reduce(((prev, curr) => prev.concat(curr)), [])
                       .sort((a, b) => b.createdOn - a.createdOn);

};

const getTypeByCategory = function(category) {
  if (category === 'alerts') return 'ALERT';
  else if (category === 'announcements') return 'ANNOUNCEMENT';
  else if (category === 'recommendations') return 'RECOMMENDATION_DYNAMIC';
  else if (category === 'tips') return 'RECOMMENDATION_STATIC';
  else { throw new Error('category not supported: ', category); }
};

module.exports = {
  combineMessages,
  getTypeByCategory
};
