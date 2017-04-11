/**
 * @api {get} action/spatial/group Get groups
 * @apiVersion 0.0.1
 * @apiName Groups
 * @apiGroup Spatial
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Returns all area groups.
 *
 * @apiParamExample {json} Request Example
 * GET action/spatial/group
 *
 * @apiSuccess {Boolean}      success             <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]}     errors              Empty array of error messages.
 * @apiSuccess {Object[]}     groups              Array of <code>Group</code> objects.
 *
 * @apiSuccess (Group) {Number}   key           Group unique key (UUID).
 * @apiSuccess (Group) {String}   title         Description
 * @apiSuccess (Group) {Number}   createdOn     Creation date time.
 * @apiSuccess (Group) {Object}   boundingBox   Bounding box in GeoJSON format.
 * @apiSuccess (Group) {Number}   levelCount    Number of levels.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [ ],
 *   groups: [{
 *     key: "d29f8cb8-1da6-4d57-8c39-0a155cc194a5",
 *     title: "Neighbourhoods",
 *     createdOn: 1474541445596,
 *     boundingBox: null,
 *     levelCount: 1
 *   }],
 *   success: true
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
 *     code: "SharedErrorCode.AUTHENTICATION_USERNAME",
 *     description: "Authentication has failed for user user@daiad.eu."
 *   }],
 *   success: false
 * }
 */
function getGroups() { return; }

/**
 * @api {get} action/spatial/group/{groupKey}/area Get areas
 * @apiVersion 0.0.1
 * @apiName Areas
 * @apiGroup Spatial
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Returns all areas for the selected area group.
 *
 * @apiParam (QueryString)  {String}      groupKey  Group unique key (UUID).
  *
 * @apiParamExample {json} Request Example
 * GET action/spatial/group/db1ddcc7-152f-429d-1b8e-083879f3ed51/area
 *
 * @apiSuccess {Boolean}      success             <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]}     errors              Empty array of error messages.
 * @apiSuccess {Object[]}     areas               Array of <code>Area</code> objects.
 *
 * @apiSuccess (Area) {Number}   key           Area unique key (UUID).
 * @apiSuccess (Area) {String}   groupKey      Parent group unique key (UUID).
 * @apiSuccess (Area) {String}   title         Description
 * @apiSuccess (Area) {Number}   createdOn     Creation date time.
 * @apiSuccess (Area) {Object}   geometry      Area geometry in GeoJSON format.
 * @apiSuccess (Area) {Number}   levelCount    Area level.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [ ],
 *   areas: [{
 *     key: "4bf1a884-b640-4e54-9f5e-7c7caf01c95f",
 *     groupKey: "d29f8cb8-7df6-4d57-8c99-0a155cc394c5",
 *     title: "Area 1",
 *     createdOn: 1474541446177,
 *     geometry: {...},
 *     levelIndex: 1
 *   }],
 *   success: true
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
 *     code: "SharedErrorCode.AUTHENTICATION_USERNAME",
 *     description: "Authentication has failed for user user@daiad.eu."
 *   }],
 *   success: false
 * }
 */
function getGroupAreas() { return; }

/**
 * @api {get} action/spatial/user/area Get user area
 * @apiVersion 0.0.1
 * @apiName UserArea
 * @apiGroup Spatial
 * @apiPermission ROLE_USER, ROLE_ADMIN
 *
 * @apiDescription Returns the default area for the authenticated user. The area is selected by the default area group defined in property <code>daiad.spatial.neighbourhood.group</code> in configuration file <code>application.properties</code>.
 *
 * @apiParamExample {json} Request Example
 * GET action/spatial/user/area
 *
 * @apiSuccess {Boolean}      success          <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]}     errors           Empty array of error messages.
 * @apiSuccess {Object[]}     area             <code>Area</code> object.
 *
 * @apiSuccess (Area) {Number}   key           Area unique key (UUID).
 * @apiSuccess (Area) {String}   groupKey      Parent group unique key (UUID).
 * @apiSuccess (Area) {String}   title         Description
 * @apiSuccess (Area) {Number}   createdOn     Creation date time.
 * @apiSuccess (Area) {Object}   geometry      Area geometry in GeoJSON format.
 * @apiSuccess (Area) {Number}   levelCount    Area level.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   errors: [ ],
 *   area: {
 *     key: "4bf1a884-b640-4e54-9f5e-7c7caf01c95f",
 *     groupKey: "d29f8cb8-7df6-4d57-8c99-0a155cc394c5",
 *     title: "Area 1",
 *     createdOn: 1474541446177,
 *     geometry: {...},
 *     levelIndex: 1
 *   },
 *   success: true
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
 *     code: "SharedErrorCode.AUTHENTICATION_USERNAME",
 *     description: "Authentication has failed for user user@daiad.eu."
 *   }],
 *   success: false
 * }
 */
function getCurrentUserReport() { return; }
