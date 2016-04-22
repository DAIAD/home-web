var fetch = require('isomorphic-fetch');
require('es6-promise').polyfill();

var formAPI = function(url, formData, method="POST") {
  
  const { data, csrf } = formData;

  let fetchObj = {
    method: method,
    credentials: 'same-origin',
    headers: {
      'Accept': "application/json",
      //'Content-Type': "application/json",
      //'Content-Type': "application/x-www-form-urlencoded; charset=UTF-8",
      'X-CSRF-TOKEN': csrf
    },
    body: data 
  };

  return fetch(url, fetchObj) 
    .then(response => { console.log('got response', response); return response; })
    //.then(response => (response.status >= 200 && response.status < 300)?response:new Error(response.statusText))
    //.then(response => response.text())
    //.then(text => { console.log('got text', text); return text; })
    .then(response => response.json().then(json => Object.assign({}, json, {csrf:response.headers.get('X-CSRF-TOKEN')})))
    .then(response => { console.log('got json response', response); return response; });
};

module.exports = formAPI;

