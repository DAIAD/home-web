var fetch = require('isomorphic-fetch');
require('es6-promise').polyfill();

var formAPI = function(url, data, method="POST") {
  
  const { formData, csrf } = data; 

  let fetchObj = {
    method: method,
    credentials: 'same-origin',
    headers: {
      'Accept': "application/json",
      'X-Requested-With':'XMLHttpRequest',
      'X-CSRF-TOKEN': csrf
    },
    body: formData 
  };

  return fetch(url, fetchObj) 
  .then(response => response.json()
                    .then(json => Object.assign({}, json, {csrf:response.headers.get('X-CSRF-TOKEN')})))
  .catch(error => { throw new Error('response parsing failed', error); });
  
};

module.exports = formAPI;

