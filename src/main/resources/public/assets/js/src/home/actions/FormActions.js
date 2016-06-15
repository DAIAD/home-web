    /*
/**
 * Form Actions module.
 * Action creators for generic form handling
 * 
 * @module FormActions
 */

var types = require('../constants/ActionTypes');

/**
 * Sets form data
 * @param {String} form - The id of the form
 * @param {Object} formData - The data to set 
 * 
 */
const setForm = function(form, formData) {
  return {
    type: types.FORM_SET,
    form,
    formData
  };
};

/**
 * Resets form data to initial state
 * @param {String} form - The id of the form
 * 
 */
const resetForm = function(form) {
  return {
    type: types.FORM_RESET,
    form,
  };
};

module.exports = {
  setForm,
  resetForm
};

