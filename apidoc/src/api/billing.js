/**
 * @apiDefine AuthenticatedRequest
 * @apiParam {Object} credentials          User credentials used for authentication.
 * @apiParam {String} credentials.username User name
 * @apiParam {String} credentials.password User password
 */

/**
 * @api {post} /api/v1/billing/price-bracket Get price brackets
 * @apiVersion 0.0.1
 * @apiName PriceBracketCurrent
 * @apiGroup Billing
 * @apiPermission ROLE_USER, ROLE_SYSTEM_ADMIN, ROLE_UTILITY_ADMIN
 *
 * @apiDescription Returns all the currently applicable price brackets.
 *
 * @apiUse AuthenticatedRequest
 *
 * @apiParamExample {json} Request Example
 * POST /api/v1/billing/price-bracket
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   }
 * }
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
 * @api {post} /api/v1/billing/price-bracket/{referenceDate} Get price brackets historical data
 * @apiVersion 0.0.1
 * @apiName PriceBracketHistory
 * @apiGroup Billing
 * @apiPermission ROLE_USER, ROLE_SYSTEM_ADMIN, ROLE_UTILITY_ADMIN
 *
 * @apiDescription Returns all the applicable price brackets for the interval that contains the given reference date.
 *
 * @apiParam (QueryString)  {String}      referenceDate   Reference date as <code>yyyyMMdd</code>.
 *
 * @apiUse AuthenticatedRequest
 *
 * @apiParamExample {json} Request Example
 * POST /api/v1/billing/price-bracket/20160905
 * {
 *   "credentials": {
 *     "username":"user@daiad.eu",
 *     "password":"****",
 *   }
 * }
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
