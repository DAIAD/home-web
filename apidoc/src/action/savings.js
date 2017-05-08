/**
 * @apiDefine ScenarioObject
 * @apiSuccess (Scenario) {String}   key                  The unique scenario key (UUID).
 * @apiSuccess (Scenario) {String}   utilityKey           The utility unique key (UUID).
 * @apiSuccess (Scenario) {String}   owner                The name of the user that created and scenario.
 * @apiSuccess (Scenario) {String}   name                 A user friendly name for the scenario.
 * @apiSuccess (Scenario) {Object}   parameters           Scenario creation parameters.
 * @apiSuccess (Scenario) {Number}   potential            Total volume of savings potential.
 * @apiSuccess (Scenario) {Number}   percent              Savings percent.
 * @apiSuccess (Scenario) {Number}   consumption          Total consumption for all users in the selected time interval.
 * @apiSuccess (Scenario) {Number}   createdOn            Timestamp of scenario creation.
 * @apiSuccess (Scenario) {Number}   processingBeginOn    Processing start timestamp.
 * @apiSuccess (Scenario) {Number}   processingEndOn      Processing end timestamp.
 * @apiSuccess (Scenario) {Number}   numberOfConsumers    Total number of selected consumers.
 * @apiSuccess (Scenario) {String}   status               Current scenario status. Valid values are:
 * <br/><br/><code>PENDING</code>   Scenario has been saved but not yet computed.
 * <br/><br/><code>RUNNING</code>   The Flink job that computes savings is running.
 * <br/><br/><code>COMPLETED</code> The Flink job has been successfully completed and savings data per consumer is available for querying.
 * <br/><br/><code>FAILED</code>    The Flink job execution has failed.
 */

/**
 * @api {put} action/savings Create
 * @apiVersion 0.0.1
 * @apiName SavingsCreate
 * @apiGroup Savings
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Creates a new savings potential scenario.
 *
 * @apiParam {String}   title                         Scenario user friendly name
 * @apiParam {Object}   parameters                    Consumer selection parameters.
 * @apiParam {Object}   parameters.time               Time interval.
 * @apiParam {Number}   parameters.time.start         Start date as a UTC time stamp.
 * @apiParam {Number}   parameters.time.end           End date as a UTC time stamp.
 *
 * @apiParam {Object[]} parameters.population         A collection of <code>SavingsPopulationFilter</code> objects used for filtering consumers. Selected users must belong to every declared group of users.
 *
 * @apiParam {Object[]} parameters.spatial            Spatial constraints used for filtering consumers. Spatial constraint classes derive from <code>SpatialFilter</code>. Concrete implementations are <code>AreaSpatialFilter</code>, <code>GroupSpatialFilter</code> and <code>ConstraintSpatialFilter</code>.<br/></br>For additional details see specific types below. A consumer should belong to at least one of the declared <code>AreaSpatialFilter</code> filters.
 *
 * @apiParam (SavingsPopulationFilter) {String} type  Filter type. Valid values are <code>GROUP</code> and <code>UTILITY</code>.
 * <br/><br/><code>GROUP</code> selects all the members of a single group.
 * <br/><br/><code>UTILITY</code> selects all the members of a utility.
 * @apiParam (SavingsPopulationFilter) {String} key   The group or utility unique key (UUID).
 *
 * @apiParam (SpatialFilter)           {String} type  Filter type. Valid values are <code>AREA</code>, <code>GROUP</code> and <code>CONSTRAINT</code>.
 * <br/><br/><code>AREA</code> selects all users whose locations are contained by the area geometry.
 * <br/><br/><code>GROUP</code> applies a filter to all areas of the group. It is equivelant of creating an <code>AREA</code> filter for every area in the group.
 * <br/><br/><code>CONSTRAINT</code> applies a spatial constraint to all declared <code>AreaSpatialFilter</code> filters.
 *
 * @apiParam (AreaSpatialFilter extends SpatialFilter)        {String[]}  areas       Array of <code>UUID</code> strings that identify specific areas.
 *
 * @apiParam (GroupSpatialFilter extends SpatialFilter)       {String}    group       Area group unique identifier (UUID)
 *
 * @apiParam (ConstraintSpatialFilter extends SpatialFilter)  {String}    operation   Spatial operation. Valid values are <code>CONTAINS</code>, <code>INTERSECT</code> and <code>DISTANCE</code>.
 * @apiParam (ConstraintSpatialFilter extends SpatialFilter)  {Object}    geometry    Geometry expressed in GeoJSON format.
 * @apiParam (ConstraintSpatialFilter extends SpatialFilter)  {Number}    [distance]  Optional distance parameter required only when <code>DISTANCE</code> is set for <code>operation</code> property.
 *
 * @apiParamExample {json} Request Example
 * PUT /action/savings
 *
 * {
 *  "title": "Scenario 1",
 *  "parameters": {
 *    "time": {
 *      "start": 1483221619000,
 *      "end": 1490907619000
 *    },
 *    "spatial": [{
 *      "type": "GROUP",
 *      "group": "d29f8cb8-7df6-4d57-8c99-0a155cc394c5"
 *    }],
 *    "population": [{
 *      "type": "GROUP",
 *      "key" : "0c95025f-d7ac-42aa-8f2e-5205fa228b3b"
 *    }, {
 *      "type": "GROUP",
 *      "key" : "42b7eefe-6e7e-4b8a-9123-5602110b765f"
 *    }]
 *  }
 * }
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {String}   [key]             The unique key (UUID) of the new scenario.
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
 *         code: "SchedulerErrorCode.SCHEDULER_JOB_LAUNCH_FAILED",
 *         description: "Failed to launch job SAVINGS-POTENTIAL."
 *       }],
 *       success: false
 *     }
 */
function createSavings() { return; }

/**
 * @api {get} action/savings/refresh/{scenarioKey} Refresh
 * @apiVersion 0.0.1
 * @apiName SavingsRefresh
 * @apiGroup Savings
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Refreshes the results for an existing savings potential scenario. This method invokes the scheduler and starts a new execution of the savings potential Flink job.
 *
 * @apiParam  (Query String Parameters)     {String}     scenarioKey   The savings potential scenario unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * GET /action/savings/refresh/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc
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
 *         code: "SavingsPotentialErrorCode.SCENARIO_NOT_FOUND",
 *         description: "Savings potential scenario was not found."
 *       }],
 *       success: false
 *     }
 */
function refreshSavings { return; }

/**
 * @api {get} action/savings/{scenarioKey} Find
 * @apiVersion 0.0.1
 * @apiName SavingsFind
 * @apiGroup Savings
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Loads data for an existing scenario.
 *
 * @apiParam  (Query String Parameters)     {String}     scenarioKey   The savings potential scenario unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * GET /action/savings/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {Object}   [scenario]        The selected scenario
 *
 * @apiUse ScenarioObject
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *    "errors": [],
 *    "scenario":    {
 *       "key": "4ffca702-2d8f-4f3c-a21f-df12f53956bc",
 *       "utilityKey": "941be15c-a8ea-40c9-8502-9b790d2a99f3",
 *       "owner": "admin@alicante.daiad.eu",
 *       "name": "Scenario 1",
 *       "parameters":       {
 *          "time":          {
 *             "start": 1483221619000,
 *             "end": 1490907619000
 *          },
 *          "population": [         {
 *             "type": "GROUP",
 *             "key": "0c95025f-d7ac-42aa-8f2e-5205fa228b3b"
 *          }],
 *          "spatial": [         {
 *             "group": "d29f8cb8-7df6-4d57-8c99-0a155cc394c5",
 *             "type": "GROUP"
 *          }]
 *       },
 *       "potential": 51775.25,
 *       "percent": 0.21563407147646674,
 *       "consumption": 240107,
 *       "createdOn": 1491408156080,
 *       "processingBeginOn": 1491411647308,
 *       "processingEndOn": 1491411647429,
 *       "status": "COMPLETED",
 *       "numberOfConsumers": 100
 *    },
 *    "success": true
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
 *         code: "SavingsPotentialErrorCode.SCENARIO_NOT_FOUND",
 *         description: "Savings potential scenario was not found."
 *       }],
 *       success: false
 *     }
 */
function findSavings { return; }

/**
 * @api {get} action/savings/explore/{scenarioKey}/{clusterKey} Explore
 * @apiVersion 0.0.1
 * @apiName SavingsExplore
 * @apiGroup Savings
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Explores data for an existing scenario using clusters.
 *
 * @apiParam  (Query String Parameters)     {String}     scenarioKey   The savings potential scenario unique key (UUID).
 * @apiParam  (Query String Parameters)     {String}     clusterKey    The unique key for a cluster.
 *
 * @apiParamExample {json} Request Example
 * GET /action/savings/explore/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc/d29f8cb8-7df6-4d57-8c99-0a155cc394c5
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {String}   scenarioKey       The scenario unique key (UUID).
 * @apiSuccess {String}   scenarioName      The scenario name.
 * @apiSuccess {String}   clusterKey        The cluster unique key (UUID).
 * @apiSuccess {String}   clusterName       The cluster name.
 * @apiSuccess {Object[]} segments          An array of <code>Segment</code> objects.
 *
 * @apiSuccess (Segment)  {String}   key            The unique group key (UUID).
 * @apiSuccess (Segment)  {String}   name           The group name.
 * @apiSuccess (Segment)  {Number}   potential      Total volume of savings potential for all users in the group.
 * @apiSuccess (Segment)  {Number}   percent        Savings percent.
 * @apiSuccess (Segment)  {Number}   consumption    Total consumption for all users in the group.
 *
 * @apiSuccessExample {json} Response Example
 * GET /action/savings/explore/4ffca702-2d8f-4f3c-a21f-df12f53956bc/41dc3c07-9938-4ca5-b70b-58ee90cf7a6a
 *
 * HTTP/1.1 200 OK
 * {
 *    "errors": [],
 *    "scenarioKey": "4ffca702-2d8f-4f3c-a21f-df12f53956bc",
 *    "scenarioName": "Scenario 1",
 *    "clusterKey": "41dc3c07-9938-4ca5-b70b-58ee90cf7a6a",
 *    "clusterName": "Age",
 *    "segments":    [
 *             {
 *          "key": "e8973061-8a27-410c-9a8a-5e5e76c773f2",
 *          "name": "25 - 34",
 *          "consumption": 31929,
 *          "potential": 15247.6875,
 *          "percent": 47.75
 *       }, {
 *          "key": "42b7eefe-6e7e-4b8a-9123-5602110b765f",
 *          "name": "35 - 44",
 *          "consumption": 97252,
 *          "potential": 22351.5625,
 *          "percent": 22.98
 *       }, {
 *          "key": "0b7fe646-97f4-499f-9f7e-6b4a982754f7",
 *          "name": "45 - 54",
 *          "consumption": 60244,
 *          "potential": 7831,
 *          "percent": 13
 *       }, {
 *          "key": "f03274c1-5a2c-444f-b80d-0e8c2e0541d7",
 *          "name": "55 - 64",
 *          "consumption": 26955,
 *          "potential": 4406.0625,
 *          "percent": 16.35
 *       }, {
 *          "key": "db841d38-0df2-47cb-9c77-abf2a2886f61",
 *          "name": "65 - 74",
 *          "consumption": 23727,
 *          "potential": 1938.9375,
 *          "percent": 8.17
 *       }
 *    ],
 *    "success": true
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
 *         code: "SavingsPotentialErrorCode.SCENARIO_NOT_FOUND",
 *         description: "Savings potential scenario was not found."
 *       }],
 *       success: false
 *     }
 */
function exploreSavings { return; }

/**
 * @api {post} action/savings/query Query
 * @apiVersion 0.0.1
 * @apiName SavingsQuery
 * @apiGroup Savings
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Searches scenarios.
 *
 * @apiParam {Object}   query                   The query.
 * @apiParam {Number}   query.pageIndex         Data page index. Default value is <code>0</code>.
 * @apiParam {Number}   query.pageSize          Data page size. Default value is <code>10</code>.
 * @apiParam {String}   [query.status]          Filters data by status.
 * @apiParam {String}   [query.name]            Filters data by name. The operation is implemented using the <code>LIKE</code> operator.
 * @apiParam {String}   [query.sortBy]          Sorting property. Valid values are:
 * <br/><code>NAME</code>
 * <br/><code>CREATED_ON</code>
 * <br/><code>STATUS</code>
 * @apiParam {Boolean}  [query.sortAscending]   Sorts results in ascending or descending order.
 *
 * @apiParamExample {json} Request Example
 * {
 *  "query": {
 *    "pageIndex": 0,
 *    "pageSize": 20,
 *    "status": "COMPLETED",
 *    "name": "Scenario",
 *    "sortBy": "STATUS",
 *    "sortAscending": false
 *  }
 * }
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {Number}   pageIndex         The result page index.
 * @apiSuccess {Number}   pageSize          The result page size.
 * @apiSuccess {Number}   total             The total number of records found.
 * @apiSuccess {Object[]} scenarios         An array of <code>Scenario</code> objects.
 *
 * @apiUse ScenarioObject
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *    "errors": [],
 *    "total": 1,
 *    "pageIndex": 0,
 *    "pageSize": 10,
 *    "scenarios": [{
 *       "key": "4ffca702-2d8f-4f3c-a21f-df12f53956bc",
 *       "utilityKey": "941be15c-a8ea-40c9-8502-9b790d2a99f3",
 *       "owner": "admin@alicante.daiad.eu",
 *       "name": "Scenario 1",
 *       "parameters": {
 *          "time": {
 *             "start": 1483221619000,
 *             "end": 1490907619000
 *          },
 *          "population": [{
 *             "type": "GROUP",
 *             "key": "0c95025f-d7ac-42aa-8f2e-5205fa228b3b"
 *          }],
 *          "spatial": [{
 *             "group": "d29f8cb8-7df6-4d57-8c99-0a155cc394c5",
 *             "type": "GROUP"
 *          }]
 *       },
 *       "potential": 51775.25,
 *       "percent": 0.21563407147646674,
 *       "consumption": 240107,
 *       "createdOn": 1491408156080,
 *       "processingBeginOn": 1491411647308,
 *       "processingEndOn": 1491411647429,
 *       "status": "COMPLETED",
 *       "numberOfConsumers": 100
 *    }],
 *    "success": true
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
 *         code: "SavingsPotentialErrorCode.PARSE_ERROR",
 *         description: "Failed to parse JSON serialized parameters."
 *       }],
 *       success: false
 *     }
 */
function querySavings { return; }

/**
 * @api {delete} action/savings/{scenarioKey} Delete
 * @apiVersion 0.0.1
 * @apiName SavingsDelete
 * @apiGroup Savings
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Deletes an existing savings potential scenario. The user must be the owner of the scenario.
 *
 * @apiParam  (Query String Parameters)     {String}     scenarioKey   The savings potential scenario unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * DELETE /action/savings/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc
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
 *         code: "SavingsPotentialErrorCode.SCENARIO_NOT_FOUND",
 *         description: "Savings potential scenario was not found."
 *       }],
 *       success: false
 *     }
 */
function deleteSavings() { return; }
