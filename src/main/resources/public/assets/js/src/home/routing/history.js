var { useRouterHistory } = require('react-router');
var { createHistory } = require('history');

const history = useRouterHistory(createHistory)({
  basename: '/home/'
});

module.exports = history;
