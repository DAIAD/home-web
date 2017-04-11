/**
 * @api {get} action/billing/price-bracket Get price brackets
 * @apiVersion 0.0.1
 * @apiName PriceBracketCurrent
 * @apiGroup Billing
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Returns all the currently applicable price brackets.
 *
 * @apiParamExample {json} Request Example
 * GET /action/billing/price-bracket
 *
 * @apiSuccess {Boolean}      success             <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]}     errors              Empty array of error messages.
 * @apiSuccess {Object[]}     brackets            Array of <code>PriceBracket</code> objects.
 *
 * @apiSuccess (PriceBracket) {Number}    minVolume   Volume interval min value.
 * @apiSuccess (PriceBracket) {Number}    maxVolume   Volume interval max value. If this is the last interval, <code>null</code> is returned.
 * @apiSuccess (PriceBracket) {Number}    price       Price
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [ ],
 *   brackets: [{
 *     minVolume: 0,
 *     maxVolume: 9,
 *     price: 0.02
 *   }, {
 *     minVolume: 9,
 *     maxVolume: 30,
 *     price: 0.55
 *   }, {
 *     minVolume: 30,
 *     maxVolume: 60,
 *     price: 1.85
 *   }, {
 *     minVolume: 60,
 *     maxVolume: null,
 *     price: 2.49
 *   }],
 *   success: true
 * }
 *
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String}   code          Unique error code.
 * @apiError (Error) {String}   description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "SharedErrorCode.AUTHENTICATION_USERNAME",
 *     description: "Authentication has failed for user user@daiad.eu."
 *   }],
 *   success: false
 * }
 */
function getCurrentPriceBrackets() { return; }

/**
 * @api {get} action/billing/price-bracket/{referenceDate} Get price brackets historical data
 * @apiVersion 0.0.1
 * @apiName PriceBracketHistory
 * @apiGroup Billing
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Returns all the applicable price brackets for the interval that contains the given reference date.
 *
 * @apiParam (QueryString)  {String}      referenceDate   Reference date as <code>yyyyMMdd</code>.
 *
 * @apiParamExample {json} Request Example
 * GET action/billing/price-bracket/20160905
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [ ],
 *   brackets: [{
 *     minVolume: 0,
 *     maxVolume: 9,
 *     price: 0.02
 *   }, {
 *     minVolume: 9,
 *     maxVolume: 30,
 *     price: 0.55
 *   }, {
 *     minVolume: 30,
 *     maxVolume: 60,
 *     price: 1.85
 *   }, {
 *     minVolume: 60,
 *     maxVolume: null,
 *     price: 2.49
 *   }],
 *   success: true
 * }
 *
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String}   code          Unique error code.
 * @apiError (Error) {String}   description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "SharedErrorCode.AUTHENTICATION_USERNAME",
 *     description: "Authentication has failed for user user@daiad.eu."
 *   }],
 *   success: false
 * }
 */
function getPriceBrackets() { return; }
