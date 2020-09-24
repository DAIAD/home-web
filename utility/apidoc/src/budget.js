/**
 * @apiDefine BudgetObject
 * @apiSuccess (Budget) {String}   key                  The unique budget key (UUID).
 * @apiSuccess (Budget) {String}   utilityKey           The utility unique key (UUID).
 * @apiSuccess (Budget) {String}   owner                The name of the user that created and budget.
 * @apiSuccess (Budget) {String}   name                 A user friendly name for the budget.
 * @apiSuccess (Budget) {Object}   parameters           An instance of <code>BudgetParametersObject</code>. For the parameters object schema see <a href="#api-Budget-BudgetCreate">Create</a>.
 * @apiSuccess (Budget) {Number}   consumptionBefore    Total consumption during the period that the budget is active with an offset of one year.
 * @apiSuccess (Budget) {Number}   consumptionAfter     Total consumption during the period that the budget is active.
 * @apiSuccess (Budget) {Number}   savingsPercent       Budget percent.
 * @apiSuccess (Budget) {Number}   expectedPercent      Expected savings percent as computed by the scenario or the goal.
 * @apiSuccess (Budget) {Number}   createdOn            Timestamp of budget creation.
 * @apiSuccess (Budget) {Number}   updatedOn            Timestamp of budget most recent update.
 * @apiSuccess (Budget) {Number}   nextUpdateOn         Timestamp of budget next update.
 * @apiSuccess (Budget) {Boolean}  active               <code>true</code> if budget is active.
 * @apiSuccess (Budget) {Number}   activatedOn          Timestamp of setting budget as active.
 * @apiSuccess (Budget) {Number}   numberOfConsumers    Total number of selected consumers.
 * @apiSuccess (Budget) {Boolean}  initialized          <code>true</code> if budget has been initialized and consumers list has been computed.
 */

/**
 * @apiDefine ErrorResponse
 *
 * @apiError {Boolean}    success           Always <code>false</code>.
 * @apiError {Object[]}   errors            Array of <code>Error</code> objects.
 *
 * @apiError (Error) {String} code          Unique error code.
 * @apiError (Error) {String} description   Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 */

 /**
 * @apiDefine ErrorExample
 *
 * @apiErrorExample Error Response Example
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "BudgetPotentialErrorCode.BUDGET_NOT_FOUND",
 *         description: "Budget was not found."
 *       }],
 *       success: false
 *     }
 */

/**
 * @api {put} action/budget Create
 * @apiVersion 0.0.1
 * @apiName BudgetCreate
 * @apiGroup Budget
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Creates a new budget.
 *
 * @apiParam {String}   title                         Budget user friendly name
 * @apiParam {Object}   parameters                    Consumer selection parameters.
 * @apiParam {Number}   [parameters.goal]             Budget goal percent. The value must be in the interval 1 to 100. If both a goal and a scenario are set, the scenario always overrides the goal value.
 * @apiParam {Object}   [parameters.scenario]         Selected scenario. <code>null</code> if not a savings potential scenario is selected.
 * @apiParam {String}   parameters.scenario.key       Scenario key.
 * @apiParam {Number}   parameters.scenario.percent   Computed scenario savings percent value. The value must be in the interval 1 to 100.
 * @apiParam {String}   parameters.distribution       Budget distribution. Valid values are:
 * <br/><code>EQUAL</code>
 * <br/><code>FAIR</code>
 * @apiParam {Object}   parameters.include            An instance of <code>ConsumerSelectionFilter</code>. Sets the users to be included in the budget. Selected users may be removed due to the <code>exclude</code> parameter.
 * @apiParam {Object}   [parameters.exclude]          An instance of <code>ConsumerSelectionFilter</code>. Sets the users to be excluded from the budget.
 *
 * @apiParam (ConsumerSelectionFilter) {Object[]} population    A collection of <code>BudgetPopulationFilter</code> objects used for filtering consumers. Selected users must belong to every declared group of users.
 * @apiParam (ConsumerSelectionFilter) {Object[]} spatial       Spatial constraints used for filtering consumers. Spatial constraint classes derive from <code>SpatialFilter</code>. Concrete implementations are <code>AreaSpatialFilter</code>, <code>GroupSpatialFilter</code> and <code>ConstraintSpatialFilter</code>.<br/></br>For additional details see specific types below. A consumer should belong to at least one of the declared <code>AreaSpatialFilter</code> filters.
 *
 * @apiParam (BudgetPopulationFilter) {String} type  Filter type. Valid values are <code>GROUP</code> and <code>UTILITY</code>.
 * <br/><br/><code>GROUP</code> selects all the members of a single group.
 * <br/><br/><code>UTILITY</code> selects all the members of a utility.
 * @apiParam (BudgetPopulationFilter) {String} key   The group or utility unique key (UUID).
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
 * PUT /action/budget
 *
 * {
 *   "title": "Budget 1",
 *   "parameters": {
 *     "scenario": null,
 *     "goal": 5,
 *     "distribution": "EQUAL",
 *     "include": {
 *       "population": [{
 *       "type": "GROUP",
 *       "key" : "0c95025f-d7ac-42aa-8f2e-5205fa228b3b"
 *     }]
 *   },
 *   "exclude": {
 *     "spatial": [{
 *       "type": "GROUP",
 *         "group": "d29f8cb8-7df6-4d57-8c99-0a155cc394c5"
 *       }],
 *       "population": [{
 *         "type": "GROUP",
 *         "key" : "42b7eefe-6e7e-4b8a-9123-5602110b765f"
 *       }]
 *     }
 *   }
 * }
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {String}   [key]             The unique key (UUID) of the new budget.
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
 *         code: "SharedErrorCode.AUTHENTICATION_USERNAME",
 *         description: "Authentication has failed for user user@daiad.eu."
 *       }],
 *       success: false
 *     }
 */
function createBudget() { return; }

/**
 * @api {put} action/budget/compute/{budgetKey}/{year}/{month} Schedule
 * @apiVersion 0.0.1
 * @apiName BudgetSchedule
 * @apiGroup Budget
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Schedules a job for computing the results for an existing budget. The reference date must be prior to the current month.
 *
 * @apiParam  (Query String Parameters)     {String}     budgetKey   The budget unique key (UUID).
 * * @apiParam  (Query String Parameters)   {Number}     year        Reference date year.
 * * @apiParam  (Query String Parameters)   {Number}     month       Reference date month.
 *
 * @apiParamExample {json} Request Example
 * PUT /action/budget/compute/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc/2017/2
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
 *         code: "BudgetPotentialErrorCode.BUDGET_NOT_FOUND",
 *         description: "Budget was not found."
 *       }],
 *       success: false
 *     }
 */
function scheduleBudget { return; }

/**
 * @api {get} action/budget/{budgetKey} Find
 * @apiVersion 0.0.1
 * @apiName BudgetFind
 * @apiGroup Budget
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Loads data for an existing budget.
 *
 * @apiParam  (Query String Parameters)     {String}     budgetKey   The budget unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * GET /action/budget/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {Object}   [budget]          The selected budget
 *
 * @apiUse BudgetObject
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *    "errors": [],
 *    "budget":    {
 *       "key": "fa7e7d87-de68-4c6c-a69d-44c18fadfa3c",
 *       "utilityKey": "941be15c-a8ea-40c9-8502-9b790d2a99f3",
 *       "owner": "admin@alicante.daiad.eu",
 *       "name": "Budget 2",
 *       "parameters":       {
 *          "scenario":          {
 *             "key": "53dcdfc9-2ed1-4522-be15-1d5fb20ba08b",
 *             "percent": 80
 *          },
 *          "goal": null,
 *          "distribution": "EQUAL",
 *          "include":          {
 *             "population": [            {
 *                "type": "GROUP",
 *                "key": "0c95025f-d7ac-42aa-8f2e-5205fa228b3b"
 *             }],
 *             "spatial": []
 *          },
 *          "exclude":          {
 *             "population": [            {
 *                "type": "GROUP",
 *                "key": "42b7eefe-6e7e-4b8a-9123-5602110b765f"
 *             }],
 *             "spatial": [            {
 *                "group": "d29f8cb8-7df6-4d57-8c99-0a155cc394c5",
 *                "type": "GROUP"
 *             }]
 *          }
 *       },
 *       "consumptionBefore": 794410,
 *       "consumptionAfter": 738989,
 *       "savingsPercent": 0.06976372402160094,
 *       "expectedPercent": 0.27163182210353065,
 *       "createdOn": 1492785883605,
 *       "updatedOn": 1492840879684,
 *       "nextUpdateOn": 1493765999999,
 *       "active": true,
 *       "activatedOn": 1492788310388,
 *       "numberOfConsumers": 59,
 *       "initialized": true
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
 *         code: "BudgetPotentialErrorCode.BUDGET_NOT_FOUND",
 *         description: "Budget was not found."
 *       }],
 *       success: false
 *     }
 */
function findBudget { return; }

/**
 * @api {post} action/budget/query Query
 * @apiVersion 0.0.1
 * @apiName BudgetQuery
 * @apiGroup Budget
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Searches budgets.
 *
 * @apiParam {Object}   query                   The query.
 * @apiParam {Number}   query.pageIndex         Data page index. Default value is <code>0</code>.
 * @apiParam {Number}   query.pageSize          Data page size. Default value is <code>10</code>.
 * @apiParam {String}   [query.name]            Filters data by name. The operation is implemented using the <code>LIKE</code> operator.
 * @apiParam {Boolean}  [query.active]          Filters active budgets.
 * @apiParam {String}   [query.sortBy]          Sorting property. Valid values are:
 * <br/><code>NAME</code>
 * <br/><code>CREATED_ON</code>
 * <br/><code>ACTIVE</code>
 * @apiParam {Boolean}  [query.sortAscending]   Sorts results in ascending or descending order.
 *
 * @apiParamExample {json} Request Example
 * {
 *  "query": {
 *    "pageIndex": 0,
 *    "pageSize": 20,
 *    "name": "Budget",
 *    "sortBy": "CREATED_ON",
 *    "sortAscending": true
 *  }
 * }
 *
 * @apiSuccess {Boolean}  success         <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors          Empty array of error messages.
 * @apiSuccess {Number}   pageIndex       The result page index.
 * @apiSuccess {Number}   pageSize        The result page size.
 * @apiSuccess {Number}   total           The total number of records found.
 * @apiSuccess {Object[]} budgets         An array of <code>Budget</code> objects. For the schema of a budget object see <a href="#api-Budget-BudgetFind">Find</a>.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *    "errors": [],
 *    "total": 4,
 *    "pageIndex": 0,
 *    "pageSize": 10,
 *    "budgets": [ ... ],
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
 *         code: "BudgetPotentialErrorCode.PARSE_ERROR",
 *         description: "Failed to parse JSON serialized parameters."
 *       }],
 *       success: false
 *     }
 */
function queryBudget { return; }

/**
 * @api {put} action/budget/{budgetKey}/activate Activate
 * @apiVersion 0.0.1
 * @apiName BudgetActivate
 * @apiGroup Budget
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Sets a budget as active.
 *
 * @apiParam  (Query String Parameters)     {String}     budgetKey   The budget unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * PUT /action/budget/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc/activate
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
 * @apiUse ErrorResponse
 *
 * @apiUse ErrorExample
 */
function activateBudget() { return; }

/**
 * @api {put} action/budget/{budgetKey}/deactivate Deactivate
 * @apiVersion 0.0.1
 * @apiName BudgetDeactivate
 * @apiGroup Budget
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Sets a budget as inactive
 *
 * @apiParam  (Query String Parameters)     {String}     budgetKey   The budget unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * PUT /action/budget/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc/deactivate
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
 * @apiUse ErrorResponse
 *
 * @apiUse ErrorExample
 */
function deactivateBudget() { return; }

/**
 * @api {get} action/budget/explore/cluster/{budgetKey}/{clusterKey} Explore Cluster
 * @apiVersion 0.0.1
 * @apiName BudgetExploreCluster
 * @apiGroup Budget
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Explores data for an existing budget using clusters.
 *
 * @apiParam  (Query String Parameters)     {String}     budgetKey   The budget unique key (UUID).
 * @apiParam  (Query String Parameters)     {String}     clusterKey  The unique key for a cluster.
 *
 * @apiParamExample {json} Request Example
 * GET /action/budget/explore/cluster/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc/d29f8cb8-7df6-4d57-8c99-0a155cc394c5
 *
 * @apiSuccess {Boolean}  success         <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors          Empty array of error messages.
 * @apiSuccess {String}   budgetKey       The budget unique key (UUID).
 * @apiSuccess {String}   budgetName      The budget name.
 * @apiSuccess {String}   clusterKey      The cluster unique key (UUID).
 * @apiSuccess {String}   clusterName     The cluster name.
 * @apiSuccess {Object[]} segments        An array of <code>Segment</code> objects.
 *
 * @apiSuccess (Segment)  {String}   key                  The unique group key (UUID).
 * @apiSuccess (Segment)  {String}   name                 The group name.
 * @apiSuccess (Segment)  {Number}   consumptionBefore    Total consumption during the period that the budget is active with an offset of one year.
 * @apiSuccess (Segment)  {Number}   consumptionAfter     Total consumption during the period that the budget is active.
 * @apiSuccess (Segment)  {Number}   percent              Savings percent value.
  *
 * @apiSuccessExample {json} Response Example
 * GET /action/budget/explore/cluster/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc/d29f8cb8-7df6-4d57-8c99-0a155cc394c5
 *
 * HTTP/1.1 200 OK
 * {
 *    "errors": [],
 *    "budgetKey": "fa7e7d87-de68-4c6c-a69d-44c18fadfa3c",
 *    "budgetName": "Budget 2",
 *    "clusterKey": "41dc3c07-9938-4ca5-b70b-58ee90cf7a6a",
 *    "clusterName": "Age",
 *    "segments": [{
 *      "key": "8fd79b7a-243d-4c0c-9fe8-6170762d6e2e",
 *      "name": "18 - 24",
 *      "consumptionBefore": 25059,
 *      "consumptionAfter": 22582,
 *      "percent": 9.88
 *    },  {
 *      "key": "e8973061-8a27-410c-9a8a-5e5e76c773f2",
 *      "name": "25 - 34",
 *      "consumptionBefore": 170607,
 *      "consumptionAfter": 144878,
 *      "percent": 15.08
 *    }, {
 *      "key": "0b7fe646-97f4-499f-9f7e-6b4a982754f7",
 *      "name": "45 - 54",
 *      "consumptionBefore": 349956,
 *      "consumptionAfter": 347972,
 *      "percent": 0.57
 *    }, {
 *      "key": "f03274c1-5a2c-444f-b80d-0e8c2e0541d7",
 *      "name": "55 - 64",
 *      "consumptionBefore": 227322,
 *      "consumptionAfter": 199520,
 *      "percent": 12.23
 *    }, {
 *      "key": "db841d38-0df2-47cb-9c77-abf2a2886f61",
 *      "name": "65 - 74",
 *      "consumptionBefore": 21466,
 *      "consumptionAfter": 24037,
 *      "percent": -11.98
 *    }],
 *    "success": true
 * }
 *
 * @apiUse ErrorResponse
 *
 * @apiUse ErrorExample
 */
function exploreBudgetCluster { return; }

/**
 * @api {get} action/budget/explore/consumer/{budgetKey}/{consumerKey} Explore Consumer
 * @apiVersion 0.0.1
 * @apiName BudgetExploreConsumer
 * @apiGroup Budget
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Explores data for a consumer of an existing budget.
 *
 * @apiParam  (Query String Parameters)     {String}     budgetKey   The budget unique key (UUID).
 * @apiParam  (Query String Parameters)     {String}     consumerKey The unique user key.
 *
 * @apiParamExample {json} Request Example
 * GET /action/budget/explore/consumer/fa7e7d87-de68-4c6c-a69d-44c18fadfa3c/dad6a786-faee-4f1b-9fc5-734569cfb54d
 *
 * @apiSuccess {Boolean}  success           <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors            Empty array of error messages.
 * @apiSuccess {String}   userKey           The user key (UUID).
 * @apiSuccess {String}   userName          The user name.
 * @apiSuccess {Object[]} months            An array of <code>MonthlyConsumerData</code> objects.
 *
 * @apiSuccess (MonthlyConsumerData)  {Number}   year               Year.
 * @apiSuccess (MonthlyConsumerData)  {Number}   month              Month of year.
 * @apiSuccess (MonthlyConsumerData)  {Number}   consumptionBefore  Total consumption during the period that the budget is active with an offset of one year.
 * @apiSuccess (MonthlyConsumerData)  {Number}   consumptionAfter   Total consumption during the period that the budget is active.
 * @apiSuccess (MonthlyConsumerData)  {Number}   percent            Savings percent value.
 *
 * @apiSuccessExample {json} Response Example
 * GET /action/budget/explore/consumer/fa7e7d87-de68-4c6c-a69d-44c18fadfa3c/dad6a786-faee-4f1b-9fc5-734569cfb54d
 *
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   "userKey": "dad6a786-faee-4f1b-9fc5-734569cfb54d",
 *   "userName": "ajmpcr@gmail.com",
 *   "months": [ {
 *     "year": 2017,
 *     "month": 1,
 *     "consumptionBefore": 6385,
 *     "consumptionAfter": 3943,
 *     "percent": 38.25
 *   }, {
 *     "year": 2017,
 *     "month": 3,
 *     "consumptionBefore": 6857,
 *     "consumptionAfter": 3666,
 *     "percent": 46.54
 *   }],
 *   "success": true
 * }
 *
 * @apiUse ErrorResponse
 *
 * @apiUse ErrorExample
 */
function exploreBudgetConsumer { return; }

/**
 * @api {delete} action/budget/{budgetKey} Delete
 * @apiVersion 0.0.1
 * @apiName BudgetDelete
 * @apiGroup Budget
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Deletes an existing budget. The user must be the owner of the budget.
 *
 * @apiParam  (Query String Parameters)     {String}     budgetKey   The budget unique key (UUID).
 *
 * @apiParamExample {json} Request Example
 * DELETE /action/budget/bd3a2b45-cdcb-4381-be4f-6c4c1327dfadc
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
 * @apiUse ErrorResponse
 *
 * @apiUse ErrorExample
 */
function deleteBudget() { return; }
