var fetch = require('isomorphic-fetch');
require('es6-promise').polyfill();

var callAPI = function(url, data, method="POST") {
  const { csrf } = data;
  delete data.csrf;

  let fetchObj = {
    method: method,
    credentials: 'same-origin',
    headers: {
      'Accept': "application/json",
      'Content-Type': "application/json",
      'X-CSRF-TOKEN': csrf
    }
  };
  fetchObj = Object.assign({}, fetchObj, Object.keys(data).length>0?{body:JSON.stringify(data)}:{});

  return fetch(url, fetchObj) 
  .then(response => { if (response.status >= 200 && response.status < 300) return response; else throw new Error(response.statusText); })
  .then(response => response.json().then(json => Object.assign({}, json, {csrf:response.headers.get('X-CSRF-TOKEN')})));
};

module.exports = callAPI;
