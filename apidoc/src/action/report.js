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
 * @api {get} action/report/status/{year}/{month} Check report
 * @apiVersion 0.0.1
 * @apiName CurrentUserReportStatus
 * @apiGroup Reporting
 * @apiPermission ROLE_USER
 *
 * @apiDescription Checks if a report exists for the authenticated user.
 *
 * @apiParam (QueryString)  {Number}      year    Reference year.
 * @apiParam (QueryString)  {Number}      month   Reference month.
 *
 * @apiParamExample {json} Request Example
 * GET action/report/status/2017/2
 *
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>.
 * @apiSuccess {Object}   [status]            Report status if the file exists.
 * @apiSuccess {Number}   status.createdOn    Report file creation timestamp.
 * @apiSuccess {Number}   status.size         Report file size in bytes.
 * @apiSuccess {String}   status.url          Resource for downloading the report.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "status": {
 *     "createdOn": 1463051902000,
 *     "size": 243755,
 *     "url": "https://app.dev.daiad.eu/action/report/download/2017/2"
 *   },
 *   "success": true
 * }
 *
 * @apiError {Boolean}          success Always <code>false</code>.
 * @apiError {Object[]}         errors  Array of <code>Error</code> objects.
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
function getCurrentUserReportStatus() { return; }

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

/**
 * @api {get} action/report/status/{userKey}/{year}/{month} Check user report
 * @apiVersion 0.0.1
 * @apiName UserReportStatus
 * @apiGroup Reporting
 * @apiPermission ROLE_ADMIN
 *
 * @apiDescription Checks if a report exists for the selected user.
 *
 * @apiParam (QueryString)  {String}      userKey User unique key (UUID).
 * @apiParam (QueryString)  {Number}      year    Reference year.
 * @apiParam (QueryString)  {Number}      month   Reference month.
 *
 * @apiParamExample {json} Request Example
 * GET action/report/status/da1decc7-552f-429d-1b8e-083879f3ed51/2017/2
 *
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>.
 * @apiSuccess {Object}   [status]            Report status if the file exists.
 * @apiSuccess {Number}   status.createdOn    Report file creation timestamp.
 * @apiSuccess {Number}   status.size         Report file size in bytes.
 * @apiSuccess {String}   status.url          Resource for downloading the report.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "status": {
 *     "createdOn": 1463051902000,
 *     "size": 243755,
 *     "url": "https://app.dev.daiad.eu/action/report/download/da1decc7-552f-429d-1b8e-083879f3ed51/2017/2"
 *   },
 *   "success": true
 * }
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
 *     code: "SharedErrorCode.RESOURCE_NOT_FOUND",
 *     description: "Resource was not found."
 *   }],
 *   success: false
 * }
 */
function getCurrentUserReportStatus() { return; }

/**
 * @api {get} action/report/status/{year} Year report status
 * @apiVersion 0.0.1
 * @apiName YearReportStatus
 * @apiGroup Reporting
 * @apiPermission ROLE_USER
 *
 * @apiDescription Returns the status of all reports for the selected year and authenticated user.
 *
 * @apiParam (QueryString)  {Number}      year    Reference year.
 *
 * @apiParamExample {json} Request Example
 * GET action/report/status/2017
 *
 * @apiSuccess {Boolean}  success             Returns <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors              Array of <code>Error</code>.
 * @apiSuccess {Object}   [reports]           Array of <code>ReportStatus</code> objects.
 *
 * @apiSuccess (ReportStatus) {Number}   createdOn    Report file creation timestamp.
 * @apiSuccess (ReportStatus) {Number}   size         Report file size in bytes.
 * @apiSuccess (ReportStatus) {String}   url          Resource for downloading the report.
 * @apiSuccess (ReportStatus) {Number}   year         Reference year.
 * @apiSuccess (ReportStatus) {Number}   month        Reference month.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "reports": [{
 *     "createdOn": 1463051902000,
 *     "size": 243755,
 *     "url": "https://app.dev.daiad.eu/action/report/download/2017/2",
 *     "year": 2017,
 *     "month": 2
 *   }],
 *   "success": true
 * }
 *
 * @apiError {Boolean}          success Always <code>false</code>.
 * @apiError {Object[]}         errors  Array of <code>Error</code> objects.
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
function getCurrentUserYearReportStatus() { return; }
