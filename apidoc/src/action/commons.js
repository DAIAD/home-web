/**
 * @api {put} action/commons Create
 * @apiVersion 0.0.1
 * @apiName CommonsCreate
 * @apiGroup Commons
 * @apiPermission ROLE_USER
 *
 * @apiDescription Creates a new commons group. The user who creates the commons group is assigned as both the owner and a member of the group.
 *
 * @apiParam {String}     name              Unique name.
 * @apiParam {String}     description       A short description of the commons group.
 * @apiParam {Object}     [geometry]        The spatial representation of the commons group in GeoJSON format.
 * @apiParam {String}     [image]           A base64 encoded image for the group.
 *
 * @apiParamExample {json} Request Example
 *  {
 *    "name": "Alicante #1",
 *    "description": "Alicante Commons Group #1",
 *    "geometry": {
 *      'type': 'Polygon',
 *      'coordinates': [
 *        [
 *          [
 *            -0.525970458984375,
 *            38.329537722849636
 *          ], [
 *            -0.5233955383300781,
 *            38.36386812314455
 *          ], [
 *            -0.4821968078613281,
 *            38.37651914591569
 *          ], [
 *            -0.4440879821777344,
 *            38.33963658855894
 *          ], [
 *            -0.46966552734375,
 *            38.31647443592999
 *          ], [
 *            -0.5089759826660156,
 *            38.313511301083466
 *          ], [
 *            -0.525970458984375,
 *            38.329537722849636
 *          ]
 *        ]
 *      ]
 *    }
 *    "image": null
 *  }
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {String}   key               Key of the new commons group (UUID).
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "success": true,
 *   "errors": [],
 *   "key": "bfcb2b45-fcdb-4485-be4f-6c4c1327dfcc"
 * }
 *
 * @apiError {Boolean}    success           Always <code>false</code>.
 * @apiError {Object[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "CommonsErrorCode.NAME_EXISTS",
 *         description: "A commons group with the name Alicante #1 already exists."
 *       }],
 *       success: false
 *     }
 */
function createCommons() { return; }

/**
 * @api {post} action/commons/{commonsKey} Update
 * @apiVersion 0.0.1
 * @apiName CommonsUpdate
 * @apiGroup Commons
 * @apiPermission ROLE_USER
 *
 * @apiDescription Updates an existing commons group. The user must be the owner of the group.
 *
 *
 * @apiParam  (Query String Parameters)     {String}     commonsKey   The commons unique key (UUID).
 *
 * @apiParam {String}     name              Unique name.
 * @apiParam {String}     description       A short description of the commons group.
 * @apiParam {Object}     [geometry]        The spatial representation of the commons group in GeoJSON format.
 * @apiParam {String}     [image]           An image for the group.
 *
 * @apiParamExample {json} Request Example
 *  POST /action/commons/bfcb2b45-fcdb-4485-be4f-6c4c1327dfcc
 *  {
 *    "name": "Alicante #2",
 *    "description": "Alicante Commons Group #2",
 *    "geometry": null,
 *    "image": null
 *  }
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "success": true,
 *   "errors": []
 * }
 *
 * @apiError {Boolean}    success           Always <code>false</code>.
 * @apiError {Object[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "CommonsErrorCode.NOT_FOUND",
 *         description: "The commons group was not found."
 *       }],
 *       success: false
 *     }
 */
function updateCommons() { return; }

/**
 * @api {delete} action/commons/{commonsKey} Delete
 * @apiVersion 0.0.1
 * @apiName CommonsDelete
 * @apiGroup Commons
 * @apiPermission ROLE_USER
 *
 * @apiDescription Deletes an existing commons group. The user must be the owner of the group.
 *
 *
 * @apiParam  (Query String Parameters)     {String}     commonsKey   The commons unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * DELETE /action/commons/bfcb2b45-fcdb-4485-be4f-6c4c1327dfcc
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "success": true,
 *   "errors": []
 * }
 *
 * @apiError {Boolean}    success           Always <code>false</code>.
 * @apiError {Object[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "CommonsErrorCode.NOT_FOUND",
 *         description: "The commons group was not found."
 *       }],
 *       success: false
 *     }
 */
function deleteCommons() { return; }

/**
 * @api {put} action/commons/{commonsKey}/join Join
 * @apiVersion 0.0.1
 * @apiName CommonsJoin
 * @apiGroup Commons
 * @apiPermission ROLE_USER
 *
 * @apiDescription Joins an existing commons group.
 *
 *
 * @apiParam  (Query String Parameters)     {String}     commonsKey   The commons unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * PUT /action/commons/bfcb2b45-fcdb-4485-be4f-6c4c1327dfcc/join
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "success": true,
 *   "errors": []
 * }
 *
 * @apiError {Boolean}    success           Always <code>false</code>.
 * @apiError {Object[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "CommonsErrorCode.NOT_FOUND",
 *         description: "The commons group was not found."
 *       }],
 *       success: false
 *     }
 */
function joinCommons() { return; }

/**
 * @api {delete} action/commons/{commonsKey}/leave Leave
 * @apiVersion 0.0.1
 * @apiName CommonsLeave
 * @apiGroup Commons
 * @apiPermission ROLE_USER
 *
 * @apiDescription Leaves an existing commons group. The owner of a commons group cannot leave the group.
 *
 *
 * @apiParam  (Query String Parameters)     {String}     commonsKey   The commons unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * DELETE /action/commons/bfcb2b45-fcdb-4485-be4f-6c4c1327dfcc/leave
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "success": true,
 *   "errors": []
 * }
 *
 * @apiError {Boolean}    success           Always <code>false</code>.
 * @apiError {Object[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "CommonsErrorCode.NOT_FOUND",
 *         description: "The commons group was not found."
 *       }],
 *       success: false
 *     }
 */
function leaveCommons() { return; }

/**
 * @api {get} action/commons/{commonsKey} Get
 * @apiVersion 0.0.1
 * @apiName CommonsGet
 * @apiGroup Commons
 * @apiPermission ROLE_USER
 *
 * @apiDescription Loads data for a single commons group.
 *
 * @apiParam  (Query String Parameters)     {String}     commonsKey   The commons unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * GET /action/commons/bfcb2b45-fcdb-4485-be4f-6c4c1327dfcc
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {Number}   pageIndex         Always returns <code>0</code>.
 * @apiSuccess {Number}   pageSize          Always returns <code>1</code>
 * @apiSuccess {Number}   count             Returns <code>1</code> if commons group exists. Otherwise <code>0</code> is returned.
 * @apiSuccess {Object[]} groups            An array of <code>CommonsInfo</code> objects with one or zero items.
 *
 * @apiSuccess (CommonsInfo)  {Object}  group     An <code>Commons</code> object.
 * @apiSuccess (CommonsInfo)  {Boolean} owner     If the current user is the owner of the group.
 * @apiSuccess (CommonsInfo)  {Boolean} member    If the current user is a member of the group.
 *
 * @apiSuccess (Commons)      {String}  key         Group unique key (UUID).
 * @apiSuccess (Commons)      {String}  utilityKey  Utility unique key (UUID).
 * @apiSuccess (Commons)      {Number}  createdOn   Creation timestamp.
 * @apiSuccess (Commons)      {Number}  updatedOn   Most recent update timestamp.
 * @apiSuccess (Commons)      {String}  name        Name.
 * @apiSuccess (Commons)      {String}  description Description.
 * @apiSuccess (Commons)      {Number}  size        Number of members.
 * @apiSuccess (Commons)      {Object}  geometry    Spatial representation of the commons group in GeoJSON format.
 * @apiSuccess (Commons)      {String}  image       A base64 encoded image for the group.
 * @apiSuccess (Commons)      {Boolean} favorite    Always <code>false</code>.
 * @apiSuccess (Commons)      {String}  type        Always <code>COMMONS</code>.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true
 *   "groups": [ {
 *     "group": {
 *       "key": "bfcb2b45-fcdb-4485-be4f-6c4c1327dfcc",
 *       "utilityKey": "80de55eb-9bde-4477-a97a-b6048a1fcc9a",
 *       "name": "Alicante #1",
 *       "createdOn": 1486641821596,
 *       "updatedOn": 1486641866807,
 *       "geometry": null,
 *       "size": 12,
 *       "favorite": false,
 *       "description": "Alicante Commons Group #1",
 *       "image": null,
 *       "type": "COMMONS"
 *     },
 *     "owner": true,
 *     "member": true
 *   }],
 *   "pageIndex": 0,
 *   "pageSize": 1,
 *   "count": 1,
 * }
 *
 * @apiError {Boolean}    success           Always <code>false</code>.
 * @apiError {Object[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "CommonsErrorCode.NOT_FOUND",
 *         description: "The commons group was not found."
 *       }],
 *       success: false
 *     }
 */
function getCommons() { return; }

/**
 * @api {post} action/commons Search
 * @apiVersion 0.0.1
 * @apiName CommonsSearch
 * @apiGroup Commons
 * @apiPermission ROLE_USER
 *
 * @apiDescription Searchs commons groups.
 *
 * @apiParam {Object}     query               Commons query.
 * @apiParam {Number}     query.pageIndex     Current page index. Invalid page indexes i.e. negative numbers are set to <code>0</code>.
 * @apiParam {Number}     query.pageSize      Current page size. Invalid page size i.e. negative or zero values are set to <code>10</code>.
 * @apiParam {String}     query.sortBy        Sorting property. Valid values are:
 * <br/><code>NAME</code>
 * <br/><code>SIZE</code>
 * <br/><code>AREA</code>
 * @apiParam {Boolean}    query.sortAscending Sorts results in ascending or descending order.
 * @apiParam {String}     [query.name]        Selects groups whose name starts with the given string.
 * @apiParam {Number}     [query.size]        Selects groups whose size is greater or equal than the given number.
 * @apiParam {Object}     [query.geometry]    Selects groups whose geometry intersects with the given geometry. The parameter is in GeoJSON format.
 *
 * @apiParamExample {json} Request Example
 *  {
 *    "query": {
 *      "pageIndex": 0,
 *      "pageSize": 10,
 *      "sortBy": "NAME",
 *      "sortAscending": true,
 *      "name": "Alicante",
 *      "size": 10,
 *      "geometry": null
 *    }
 *  }
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {Number}   pageIndex         The result page index.
 * @apiSuccess {Number}   pageSize          The result page size.
 * @apiSuccess {Number}   count             The total number of records found.
 * @apiSuccess {Object[]} groups            An array of <code>CommonsInfo</code> objects.
 *
 * @apiSuccess (CommonsInfo)  {Object}  group     An <code>Commons</code> object.
 * @apiSuccess (CommonsInfo)  {Boolean} owner     If the current user is the owner of the group.
 * @apiSuccess (CommonsInfo)  {Boolean} member    If the current user is a member of the group.
 *
 * @apiSuccess (Commons)      {String}  key         Group unique key (UUID).
 * @apiSuccess (Commons)      {String}  utilityKey  Utility unique key (UUID).
 * @apiSuccess (Commons)      {Number}  createdOn   Creation timestamp.
 * @apiSuccess (Commons)      {Number}  updatedOn   Most recent update timestamp.
 * @apiSuccess (Commons)      {String}  name        Name.
 * @apiSuccess (Commons)      {String}  description Description.
 * @apiSuccess (Commons)      {Number}  size        Number of members.
 * @apiSuccess (Commons)      {Object}  geometry    Spatial representation of the commons group in GeoJSON format.
 * @apiSuccess (Commons)      {String}  image       A base64 encoded image for the group.
 * @apiSuccess (Commons)      {Boolean} favorite    Always <code>false</code>.
 * @apiSuccess (Commons)      {String}  type        Always <code>COMMONS</code>.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true
 *   "groups": [ {
 *     "group": {
 *       "key": "bfcb2b45-fcdb-4485-be4f-6c4c1327dfcc",
 *       "utilityKey": "80de55eb-9bde-4477-a97a-b6048a1fcc9a",
 *       "name": "Alicante #1",
 *       "createdOn": 1486641821596,
 *       "updatedOn": 1486641866807,
 *       "geometry": null,
 *       "size": 12,
 *       "favorite": false,
 *       "description": "Alicante Commons Group #1",
 *       "image": null,
 *       "type": "COMMONS"
 *     },
 *     "owner": true,
 *     "member": true
 *   }],
 *   "pageIndex": 0,
 *   "pageSize": 10,
 *   "count": 1,
 * }
 *
 * @apiError {Boolean}    success           Always <code>false</code>.
 * @apiError {Object[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "SharedErrorCode.UNKNOWN",
 *         description: "Internal server error has occurred."
 *       }],
 *       success: false
 *     }
 */
function searchCommons() { return; }

/**
 * @api {post} action/commons/{commonsKey}/members Members
 * @apiVersion 0.0.1
 * @apiName CommonsMembers
 * @apiGroup Commons
 * @apiPermission ROLE_USER
 *
 * @apiDescription Enumerates the members of a commons group.
 *
 * @apiParam  (Query String Parameters)     {String}     commonsKey   The commons unique key (UUID).
 *
 * @apiParam {Object}     query               Members query.
 * @apiParam {Number}     query.pageIndex     Current page index. Invalid page indexes i.e. negative numbers are set to <code>0</code>.
 * @apiParam {Number}     query.pageSize      Current page size. Invalid page size i.e. negative or zero values are set to <code>10</code>.
 * @apiParam {String}     query.sortBy        Sorting property. Valid values are:
 * <br/><code>FIRSTNAME</code>
 * <br/><code>LASTNAME</code>
 * <br/><code>DATE_JOINED</code>
 * @apiParam {Boolean}    query.sortAscending Sorts results in ascending or descending order.
 * @apiParam {String}     [query.name]        Selects members whose firstname or lastname starts with the given string.
 * @apiParam {Number}     [query.joinedOn]    Selects members who joined the group after the given timestamp.
 *
 * @apiParamExample {json} Request Example
 *  {
 *    "query": {
 *      "pageIndex": 0,
 *      "pageSize": 10,
 *      "sortBy": "LASTNAME",
 *      "sortAscending": true,
 *      "name": "Alex"
 *    }
 *  }
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {Number}   pageIndex         The result page index.
 * @apiSuccess {Number}   pageSize          The result page size.
 * @apiSuccess {Number}   count             The total number of records found.
 * @apiSuccess {Object[]} members           An array of <code>CommonsMemberInfo</code> objects.
 *
 * @apiSuccess (CommonsMemberInfo)      {String}  key         User unique key (UUID).
 * @apiSuccess (CommonsMemberInfo)      {String}  firstname   Firstname.
 * @apiSuccess (CommonsMemberInfo)      {String}  lastname    Lastname.
 * @apiSuccess (CommonsMemberInfo)      {Number}  joinedOn    Date joined the commons group.
 * @apiSuccess (CommonsMemberInfo)      {String}  ranking     User ranking computed using the last month's data.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "success": true
 *   "members": [ {
 *     "key": "bfcb4b45-fcdb-4485-be1f-6c4c1327dfcc",
 *     "firstname": "User",
 *     "lastname": null,
 *     "joinedOn": 1486641866807,
 *     "ranking": "B"
 *   }],
 *   "pageIndex": 0,
 *   "pageSize": 10,
 *   "count": 1,
 * }
 *
 * @apiError {Boolean}    success           Always <code>false</code>.
 * @apiError {Object[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Error Response Example
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "SharedErrorCode.UNKNOWN",
 *         description: "Internal server error has occurred."
 *       }],
 *       success: false
 *     }
 */
function getMembers() { return; }
