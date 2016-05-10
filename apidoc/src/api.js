/**
 * @apiDefine CreateUserError
 * @apiVersion 0.0.1
 *
 * @apiError NoAccessRight Only authenticated Admins can access the data.
 * @apiError UserNameTooShort Minimum of 5 characters required.
 *
 * @apiErrorExample  Response (example):
 *     HTTP/1.1 400 Bad Request
 *     {
 *       "error": "UserNameTooShort"
 *     }
 */



/**
 * @apiDefine ROLE_ADMIN Admin access rights needed.
 *
 * @apiVersion 0.0.1
 */


/**
 * @api {post} /data/query Execute a query
 * @apiVersion 0.0.1
 * @apiName Query
 * @apiGroup DataQuery
 * @apiPermission ROLE_ADMIN
 *
 * @apiDescription Executes a query for Smart Water Meter or/and Amphiro data. Users can apply consumer, spatial and time constraints. Currenlty spatial filters are evaluated against the selected user' smart water meter location. Population filters refer to single users, groups of users or utilities. Clusters are treated as a special case of a group filter where every cluster is replaced by a new population filter per cluster group. Time filter are either absolute or relative (sliding time window).
 *
 * @apiParam {String} [timezone] Results are converted to the specified time zone.
 * @apiParam {Object} time Time constraint.
 * @apiParam {String} time.type Time constraint type. Valid values are <code>ABSOLUTE</code> or <code>SLIDING</code>. An absolute time constraint requires the <code>start</code> and <code>end</code> parameters. A sliding time constraint requires <code>start</code>, <code>duration</code> and <code>durationTimeUnit</code> parameters.
 * @apiParam {String} time.granularity Defines the time interval over which the computation is applied. Valid values are <code>HOUR</code>, <code>DAY</code>, <code>WEEK</code>, <code>MONTH</code>, <code>YEAR</code>, <code>ALL</code>.<br/><br/>For instance, given an <code>ABSOLUTE</code> time constraint with granularity <code>DAY</code>, the computation will be applied for every day in this interval. The final result will have a single data point for every day in the time interval.
 * @apiParam {Number} time.start Start date as a UTC time stamp.
 * @apiParam {Number} time.end End date as a UTC time stamp. Required only by <code>SLIDING</code> time constraints.
 * @apiParam {Number} time.duration Time interval duration. Required only by <code>SLIDING</code> time constraints.
 * @apiParam {String} time.durationTimeUnit Time interval duration unit. Required only by <code>SLIDING</code> time constraints.
 *
 * @apiParam {Object[]} population Consumer selection filter. A data series is returned for every population filter.
 * @apiParam {String} population.type Filter type. Valid values are <code>USER</code>, <code>GROUP</code>, <code>CLUSTER</code> and <code>UTILITY</code>.
 * <br/><br/><code>USER</code> applies the computation on a single user or a list of users.
 * <br/><br/><code>GROUP</code> applies the computation on all members of a group.
 * <br/><br/><code>CLUSTER</code> applies the computation on all groups of a cluster. It is equivelant of creating a <code>GROUP</code> filter for every cluster group. When type is set to <code>CLUSTER</code>, at least one of the parameters <code>cluster</code>, <code>clusterType</code> or <code>name</code> must be also set. If more than one parameters is set, the first in the list overrides the next ones.
 * <br/><br/><code>UTILITY</code> applies the computation on all users of a utility.
 * @apiParam {String} population.label User friendly name returned for each data series.
 *
 * @apiParam {Object} population.ranking When present instead of aggregating data from all users, user sorted ranking is computed.
 * @apiParam {String} population.ranking.type Ordering. Can be <code>TOP</code> or <code>BOTTOM</code>,
 * @apiParam {String} population.ranking.field Field that the computation is applied on. Valid values are <code>VOLUME</code>, <code>ENERGY</code>, <code>DURATION</code>, <code>TEMPERATURE</code> or <code>FLOW</code>. For smart water meters, only <code>VOLUME</code> is supported.
 * @apiParam {String} population.ranking.metric Operation to apply. Valid values are <code>COUNT</code>, <code>SUM</code>, <code>MIN</code>, <code>MAX</code> or <code>AVERAGE</code>.
 * @apiParam {Number} population.ranking.limit Number of users to return.
 *
 * @apiParam {String[]} population.users Array of user unique identifiers (UUID). Required when type is <code>USER</code>.
 *
 * @apiParam {String} population.group Group unique identifier (UUID). Required when type is <code>GROUP</code>.
 *
 * @apiParam {String} population.cluster Cluster unique identifier (UUID).
 * @apiParam {String} population.clusterType Cluster type. Valid values are <code>AGE</code>, <code>INCOME</code>, <code>HOUSEHOLD_SIZE</code> and <code>APARTMENT_SIZE</code>.
 * @apiParam {String} population.name Utility unique identifier (UUID).
 *
 * @apiParam {String} population.utility Utility unique identifier (UUID). Required when type is <code>UTILITY</code>.
 * @apiParam {Object} spatial Spatial constraint used for filtering consumers.
 * @apiParam {Object} spatial.type Spatial filter operation. Valid values are <code>CONTAINS</code>, <code>INTERSECT</code> and <code>DISTANCE</code>.
 * @apiParam {Object} spatial.geometry Geometry used in spatial filter expressed in GeoJSON.
 * @apiParam {Number} spatial.distance Distance between the consumer's location and the specified geometry. Required when <code>DISTANCE</code> type is selected.
 *
 * @apiParam {String} source Data source. Valid values are <code>BOTH</code>, <code>AMPHIRO</code> and <code>METER</code>. A data series is returned for every data source type.
 *
 * @apiParam {String} metrics Operations applied. Valid values are <code>COUNT</code>, <code>SUM</code>, <code>MIN</code>, <code>MAX</code> or <code>AVERAGE</code>.
 *
 * @apiParamExample {json} Request Example
 *   {
 *      time: {
 *        type : 'SLIDING',
 *        start: moment().valueOf(),
 *        duration: -60,
 *        durationTimeUnit: 'DAY',
 *        granularity: 'DAY'
 *      },
 *      population: [{
 *        type :'USER',
 *        label: 'User 1',
 *        users: ['63078a88-f75a-4c5e-8d75-b4472ba456bb']
 *      }, {
 *        type :'CLUSTER',
 *        label: 'Income',
 *        clusterType: 'INCOME'
 *      }, {
 *        type :'UTILITY',
 *        label: 'Alicante (all)',
 *        utility: '2b48083d-6f05-488f-9f9b-99607a93c6c3'
 *      }, {
 *        type :'UTILITY',
 *        label: 'Alicante (top 2)',
 *        utility: '2b48083d-6f05-488f-9f9b-99607a93c6c3',
 *        ranking: {
 *          type: 'TOP',
 *          metric: 'AVERAGE',
 *          field: 'TEMPERATURE',
 *          limit: 2
 *        }
 *      }],
 *      spatial: {
 *        type: 'CONTAINS',
 *        geometry: {
 *          'type': 'Polygon',
 *          'coordinates': [
 *            [
 *              [
 *                  -0.525970458984375,
 *                  38.329537722849636
 *              ], [
 *                  -0.5233955383300781,
 *                  38.36386812314455
 *              ], [
 *                  -0.4821968078613281,
 *                  38.37651914591569
 *              ], [
 *                  -0.4440879821777344,
 *                  38.33963658855894
 *              ], [
 *                  -0.46966552734375,
 *                  38.31647443592999
 *              ], [
 *                  -0.5089759826660156,
 *                   38.313511301083466
 *              ], [
 *                  -0.525970458984375,
 *                  38.329537722849636
 *              ]
 *            ]
 *          ]
 *        }
 *      },
 *      source: 'METER',
 *      metrics: ['COUNT', 'SUM', 'MAX']
 *    }
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 * @apiSuccess {Object[]} devices                  Collection of Amphiro devices data series.
 * @apiSuccess {Object[]} devices.label            Result user friendly name as declared in the query.
 * @apiSuccess {Object[]} devices.population       Number of unique users found. This field is the number of users that contributed data to the final result. For instance a group may have 100 members but the <code>population</code> value may be less than 100. This may occur if a spatial field has been applied.
 * @apiSuccess {Object[]} devices.points           Data series points.
 * @apiSuccess {Object[]} meters                   Collection of smart water meter data series.
 * @apiSuccess {Object[]} meters.label             Result user friendly name as declared in the query.
 * @apiSuccess {Object[]} meters.population        Number of unique users found. This field is the number of users that contributed data to the final result. For instance a group may have 100 members but the <code>population</code> value may be less than 100. This may occur if a spatial field has been applied.
 * @apiSuccess {Object[]} meters.points            Data series points.
 *
 * @apiError {Boolean} success Always <code>false</code>.
 * @apiError {Object[]} errors Array of error messages
 * @apiError {String} errors.code Unique error code
 * @apiError {String} errors.description Error message. Application should not present error messages to the users. Instead the error <code>code</code> must be used for deciding the client message.
 *
 * @apiErrorExample Response (example):
 *     HTTP/1.1 200 OK
 *     {
 *       errors: [{
 *         code: "UserErrorCode.USERNANE_NOT_FOUND",
 *         description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *       }],
 *       success: false
 *     }
 */
function query() { return; }
