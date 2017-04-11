/**
 * @api {get} action/report/download/{year}/{month} Get report
 * @apiVersion 0.0.1
 * @apiName CurrentUserReport
 * @apiGroup Reporting
 * @apiPermission ROLE_USER
 *
 * @apiDescription Returns the report for the authenticated user as a PDF file.
 *
 * @apiParam (QueryString)  {Number}      year    Reference year.
 * @apiParam (QueryString)  {Number}      month   Reference month.
 *
 * @apiParamExample {json} Request Example
 * GET action/report/download/2017/2
 *
 * @apiError {Boolean}  success Always <code>false</code>.
 * @apiError {Object[]} errors  Array of <code>Error</code> objects.
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
function getCurrentUserReport() { return; }

/**
 * @api {get} action/report/download/{userKey}/{year}/{month} Get user report
 * @apiVersion 0.0.1
 * @apiName UserReport
 * @apiGroup Reporting
 * @apiPermission ROLE_ADMIN
 *
 * @apiDescription Returns the report for any user as a PDF file.
 *
 * @apiParam (QueryString)  {String}      userKey User unique key (UUID).
 * @apiParam (QueryString)  {Number}      year    Reference year.
 * @apiParam (QueryString)  {Number}      month   Reference month.
 *
 * @apiParamExample {json} Request Example
 * GET action/report/download/da1decc7-552f-429d-1b8e-083879f3ed51/2017/2
 *
 * @apiError {Boolean}  success Always <code>false</code>.
 * @apiError {Object[]} errors  Array of <code>Error</code> objects.
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
function getCurrentUserReport() { return; }
