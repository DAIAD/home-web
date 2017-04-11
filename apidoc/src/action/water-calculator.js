/**
 * @api {get} action/water-calculator/water-breakdown Get water breakdown data
 * @apiVersion 0.0.1
 * @apiName WaterBreakDown
 * @apiGroup WaterCalculator
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Returns water breakdown data for the authenticated user.
 *
 * @apiParamExample {json} Request Example
 * GET /action/water-calculator/water-breakdown
 * 
 * @apiSuccess {Boolean}      success             <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]}     errors              Empty array of error messages.
 * @apiSuccess {Object[]}     labels              Array of <code>WaterUse</code> objects.
 *
 * @apiSuccess (WaterUse) {String}   label        Category label.
 * @apiSuccess (WaterUse) {Number}   percent      Percent value.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [ ],
 *   labels: [{
 *     label: "Shower",
 *     percent: 40
 *   }, {
 *     label: "Bath",
 *     percent: 30
 *   }, {
 *     label: "Washing machine",
 *     percent: 15
 *   }, {
 *     label: "Other",
 *     percent: 15
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
function getWaterBreakDown() { return; }
