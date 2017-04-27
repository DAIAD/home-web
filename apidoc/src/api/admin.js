/**
 * @apiDefine Credentials User credentials used for authentication
 * @apiParam {String} username User name
 * @apiParam {String} password User password
 */

/**
 * @api {post} /v1/admin/scheduler/launch/{jobName} Execute a job
 * @apiVersion 0.0.1
 * @apiName launch
 * @apiGroup Admin
 * @apiPermission ROLE_ADMIN
 *
 * @apiDescription Initializes and starts the execution of a job
 *
 * @apiParam                            {String}   username   User name
 * @apiParam                            {String}   password   User password
 *
 * @apiParam (Path Parameters)          {String}   jobName    Unique job name as registered at the server.
 *
 *
 * @apiParamExample {json} Request Example
 * POST /api/v1/admin/scheduler/launch/STATIC-CLUSTERS/ HTTP/1.1
 * {
 *   username: "user@daiad.eu",
 *   password: "****"
 * }
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true
 * }
 *
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
* @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "SchedulerErrorCode.SCHEDULER_JOB_LAUNCH_FAIL",
 *     description: "Failed to launch job STATIC-CLUSTERS."
 *   }],
 *   success: false
 * }
 */
function launch() { return; }

/**
 * @api {post} /v1/admin/group/query Get all groups
 * @apiVersion 0.0.1
 * @apiName getGroups
 * @apiGroup Admin
 * @apiPermission ROLE_ADMIN
 *
 * @apiDescription Load all available groups.
 *
 * @apiParam                            {Object}   credentials            User credentials used for authentication.
 * @apiParam                            {String}   credentials.username   User name
 * @apiParam                            {String}   credentials.password   User password
 *
 * @apiParamExample {json} Request Example
 * {
 *   "credentials": {
 *     username: "user@daiad.eu",
 *     password: "****"
 *   }
 * }
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Array of <code>Error</code> objects.
 * @apiSuccess {Object[]} groups				   Array of <code>Group</code> objects.
 *
 * @apiSuccess (Group) {String}   key              Group unique id (UUID).
 * @apiSuccess (Group) {String}   type             Type of group. Valid values are :<br/><br/><code>UTILITY</code>: A water utility.<br/><code>CLUSTER</code>: A cluster with several segments.<br/><code>SEGMENT</code>: A segment of a cluster.<br/><code>SET</code>: A user defined collection of users.<br/><code>COMMONS</code>: A user community.
 * @apiSuccess (Group) {String}   utilityKey       Utility unique id to which the group belongs to.
 * @apiSuccess (Group) {String}   name             User friendly name for the group.
 * @apiSuccess (Group) {Number}   createdOn        Creation time stamp.
 * @apiSuccess (Group) {Object}   [geometry]       Group geometry if available in GeoJSON format.
 * @apiSuccess (Group) {Number}   [size]           Group size i.e. number of members if available.
 *
 * @apiSuccess (Cluster extends Group) {String} segments    Array of <code>Group</code> objects.
 *
 * @apiSuccess (Community extends Group) {String}   description    Community description.
 * @apiSuccess (Community extends Group) {String}   [image]        Base64 encoded image.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "groups": [{
 *     "key": "571f90a2-632b-43b0-a810-29e3e59bed47",
 *     "utilityKey": "020ea119-61be-46ae-a9d0-8679765cc718",
 *     "name": "Income: 67,000£ - 82,000£",
 *     "createdOn": 1464107932050,
 *     "geometry": null,
 *     "size": 7,
 *     "type": "SEGMENT"
 *   }, {
 *     "key": "659372b2-b17c-491e-bd32-b7f39bc29e0d",
 *     "utilityKey": "020ea119-61be-46ae-a9d0-8679765cc718",
 *     "name": "Age",
 *     "createdOn": 1464107932050,
 *     "geometry": null,
 *     "size": null,
 *     "segments": [{
 *       "key": "309499a8-0324-41d2-ab86-1ef6b7c0a1bc",
 *       "utilityKey": "020ea119-61be-46ae-a9d0-8679765cc718",
 *       "name": "Age: 18 - 24",
 *       "createdOn": 1464107932050,
 *       "geometry": null,
 *       "size": 8,
 *       "type": "SEGMENT"
 *     }, {
 *       "key": "df55805d-8966-44c8-a1c4-3d1c8bf83129",
 *       "utilityKey": "020ea119-61be-46ae-a9d0-8679765cc718",
 *       "name": "Age: 35 - 44",
 *       "createdOn": 1464107932050,
 *       "geometry": null,
 *       "size": 13,
 *       "type": "SEGMENT"
 *     }],
 *     "type": "CLUSTER"
 *   }, {
 *     "key": "01e3eddc-ed22-48d3-9cd4-bd1823e4db0c",
 *     "utilityKey": "01e3eddc-ed22-48d3-9cd4-bd1823e4db0c",
 *     "name": "DAIAD",
 *     "createdOn": 1461110400000,
 *     "geometry": null,
 *     "size": null,
 *     "type": "UTILITY"
 *   }],
 *   "success": true
 * }
 *
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [{
 *     code: "SharedErrorCode.UNKNOWN",
 *     description: "Internal server error has occurred."
 *   }],
 *   success: false
 * }
 */
function getGroups() { return; }
