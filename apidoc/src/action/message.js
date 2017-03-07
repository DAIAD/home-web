/**
 * @apiDefine Message
 * @apiSuccess (Message extends Object) {String}  type  Message type. One of <code>ALERT</code>, <code>TIP</code>, <code>RECOMMENDATION</code> and <code>ANNOUNCEMENT</code>.
 * @apiSuccess (Message extends Object) {Number}  id    Message id.
 * @apiSuccess (Message extends Object) {String}  title  Title
 * @apiSuccess (Message extends Object) {Number}  createdOn  Date created timestamp.
 * @apiSuccess (Message extends Object) {Number}  acknowledgedOn  Date acknowledged timestamp. This is the date the user has marked the message as read from the client application.
 */ 

/**
 * @apiDefine Announcement
 * @apiSuccess (Announcement extends Message) {Number} priority  Message priority.
 * @apiSuccess (Announcement extends Message) {String} content   Announcement details.
 * @apiSuccess (Announcement extends Message) {String} link      Image resource link.
 */

/**
 * @apiDefine Alert
 * @apiSuccess (Alert extends Message) {String} alertType     Alert type. One of:
 *   <br/><code>WATER_LEAK</code> 
 *   <br/><code>SHOWER_ON</code> 
 *   <br/><code>WATER_FIXTURES</code> 
 *   <br/><code>UNUSUAL_ACTIVITY</code> 
 *   <br/><code>WATER_QUALITY</code> 
 *   <br/><code>HIGH_TEMPERATURE</code> 
 *   <br/><code>NEAR_DAILY_BUDGET</code> 
 *   <br/><code>NEAR_WEEKLY_BUDGET</code> 
 *   <br/><code>REACHED_DAILY_BUDGET</code> 
 *   <br/><code>REACHED_WEEKLY_BUDGET</code> 
 *   <br/><code>CHAMPION</code> 
 *   <br/><code>TOO_MUCH_WATER</code> 
 *   <br/><code>TOO_MUCH_ENERGY</code> 
 *   <br/><code>REDUCED_WATER_USE</code> 
 *   <br/><code>WATER_EFFICIENCY_LEADER</code> 
 *   <br/><code>KEEP_UP_SAVING_WATER</code> 
 *   <br/><code>GOOD_JOB_MONTHLY</code> 
 *   <br/><code>LITERS_ALREADY_SAVED</code> 
 *   <br/><code>TOP_25_PERCENT_OF_SAVERS</code> 
 *   <br/><code>TOP_10_PERCENT_OF_SAVERS</code> 
 * @apiSuccess (Alert extends Message) {String} alertCode  The code(s) for this message as defined in DAIAD deliverable 3.2.2. If this <code>alertType</code> maps to more than one codes, they are separated by <code>|</code>.
 * @apiSuccess (Alert extends Message) {Number} priority Message priority.
 * @apiSuccess (Alert extends Message) {String} description  Long description.
 * @apiSuccess (Alert extends Message) {String} link  A resource (e.g. image) link.
 * @apiSuccess (Alert extends Message) {Number} refDate  The reference date for this message. 
 */

/**
 * @apiDefine Recommendation
 * @apiSuccess (Recommendation extends Message) {String} recommendationType  Recommendation type. One of:
 *   <br/><code>LESS_SHOWER_TIME</code> 
 *   <br/><code>LOWER_TEMPERATURE</code> 
 *   <br/><code>LOWER_FLOW</code> 
 *   <br/><code>CHANGE_SHOWERHEAD</code> 
 *   <br/><code>CHANGE_SHAMPOO</code> 
 *   <br/><code>REDUCE_FLOW_WHEN_NOT_NEEDED</code> 
 *   <br/><code>INSIGHT_A1</code> 
 *   <br/><code>INSIGHT_A2</code> 
 *   <br/><code>INSIGHT_A3</code> 
 *   <br/><code>INSIGHT_A4</code> 
 *   <br/><code>INSIGHT_B1</code> 
 *   <br/><code>INSIGHT_B2</code> 
 *   <br/><code>INSIGHT_B3</code> 
 *   <br/><code>INSIGHT_B4</code> 
 *   <br/><code>INSIGHT_B5</code> 
 * @apiSuccess (Recommendation extends Message) {String} recommendationCode      The code(s) for this message as defined in DAIAD deliverable 3.2.2. If this <code>recommendationType</code> maps to more than one codes, they are separated by <code>|</code>. 
 * @apiSuccess (Recommendation extends Message) {Number} priority        Message priority.
 * @apiSuccess (Recommendation extends Message) {String} description     Long description.
 * @apiSuccess (Recommendation extends Message) {String} imageLink       A resource link.
 * @apiSuccess (Recommendation extends Message) {Number} refDate         The reference date for this message. 
 */

/**
 * @apiDefine Tip
 * @apiSuccess (Tip extends Message) {Number}  index              Message index.
 * @apiSuccess (Tip extends Message) {String}  description        Long description.
 * @apiSuccess (Tip extends Message) {String}  categoryName       Category.
 * @apiSuccess (Tip extends Message) {String}  imageEncoded       Image encoded to base64.
 * @apiSuccess (Tip extends Message) {String}  imageLink          Image resource link.
 * @apiSuccess (Tip extends Message) {String}  imageMimeType      Image MIME type.
 * @apiSuccess (Tip extends Message) {String}  prompt             Prompt message.
 * @apiSuccess (Tip extends Message) {String}  externalLink       External link.
 * @apiSuccess (Tip extends Message) {String}  source             Message source.
 * @apiSuccess (Tip extends Message) {Number}  modifiedOn         Date modified timestamp.
 * @apiSuccess (Tip extends Message) {Boolean} active             <code>true</code> if message is active; Otherwise <code>false</code>.
 */

/**
 * @apiDefine ReceiverAccount
 * @apiSuccess (ReceiverAccount extends Object) {Number} id The account id 
 * @apiSuccess (ReceiverAccount extends Object) {String} username The account username
 */

/**
 * @api {post} action/message Fetch messages
 * @apiName MessageList
 * @apiGroup Message
 * @apiPermission ROLE_USER
 *
 * @apiDescription Fetch available messages for requesting user. All messages returned derive from <code>Message</code>.
 *
 * @apiParam                            {String}   [locale]  The target locale as a ISO-639 language code (e.g. <code>el</code>). If not supplied, the locale of current authenticated user will be used.
 * @apiParam                            {Object[]} messages  Array of <code>Options</code> objects. Each object represents a request for a specific type of messages. If more than one option objects are found for a single message type, the first overrides the others.
 *
 *
 * @apiParam (Options)   {String}   type                   Message type. Valid values are <code>ALERT</code>, <code>RECOMMENDATION</code>, <code>TIP</code> and <code>ANNOUNCEMENT</code>. This parameter is not case sensitive.
 * @apiParam (Options)   {Number}   [minMessageId]         Filter messages by a minimum message id. This option is most useful to the <code>MOBILE</code> client if only the messages after a specific message are required.
 * @apiParam (Options)   {Object}   [pagination]             Provide data pagination options. 
 * @apiParam (Options)   {Number}   [pagination.offset]      Start fetching from this index. If not set, data paging starts from the first record.
 * @apiParam (Options)   {Number}   [pagination.size]        The number of records to return. If not set, all records are returned.
 * @apiParam (Options)   {Boolean}  [pagination.ascending]   Control ordering of returned records: <code>true</code> for ordering ascending on message id, otherwise <code>false</code>. Sorting is performed on the message id field. Sorting on message id field is equivalent to sorting over creation date. Default value is <code>true</code>.
 *
 *
 * @apiParamExample {json} Request Example
 * {
 *   "locale": "el",
 *   "messages": [
 *      {
 *         "type": "ALERT",
 *         "minMessageId": 11233,
 *         "pagination": {
 *             "offset": 10,
 *             "size": 5,
 *             "ascending": false
 *         }
 *      },
 *      {
 *         "type": "RECOMMENDATION",
 *         "minMessageId": 932,
 *      }
 *   ]
 * }
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 * @apiSuccess {Object[]} alerts                   Array of <code>Alert</code> objects.
 * @apiSuccess {Object[]} recommendations          Array of <code>Recommendation</code> objects.
 * @apiSuccess {Object[]} tips                     Array of <code>Tip</code> objects.
 * @apiSuccess {Object[]} announcements            Array of <code>Announcement</code> objects.
 * @apiSuccess {Number}   totalAlerts              Total number of alerts.
 * @apiSuccess {Number}   totalRecommendations     Total number of recommendations.
 * @apiSuccess {Number}   totalTips                Total number of tips.
 * @apiSuccess {Number}   totalAnnouncements       Total number of announcements.
 *
 * @apiUse Message
 * @apiUse Announcement
 * @apiUse Alert
 * @apiUse Recommendation
 * @apiUse Tip
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 *  {
 *    "errors": [],
 *    "alerts": [
 *      {
 *        "priority": 5,
 *        "alertType": "TOO_MUCH_WATER",
 *        "alertTemplate": "TOO_MUCH_WATER_METER",
 *        "alertCode": "A17|A18",
 *        "description": "You are using twice the amount of water compared to city average. You could save up to 57,949 liters. Want to learn how?",
 *        "link": null,
 *        "body": "You are using too much water",
 *        "type": "ALERT",
 *        "id": 842,
 *        "locale": "en",
 *        "title": "You are using too much water",
 *        "refDate": 1479160800000,
 *        "acknowledgedOn": 1487756290000,
 *        "createdOn": 1487756280417
 *      }
 *    ],
 *    "totalAlerts": 1,
 *    "recommendations": [
 *      {
 *        "priority": 5,
 *        "recommendationType": "INSIGHT_A3",
 *        "recommendationTemplate": "INSIGHT_A3_MORNING_CONSUMPTION_DECR",
 *        "recommendationCode": "IA3",
 *        "description": "8.0lt vs. the average 22.2lt",
 *        "link": null,
 *        "body": "63% decrease in morning consumption",
 *        "id": 5018,
 *        "locale": "en",
 *        "title": "63% decrease in morning consumption",
 *        "refDate": 1479160800000,
 *        "acknowledgedOn": 1487756290000,
 *        "createdOn": 1487756527611,
 *        "type": "RECOMMENDATION"
 *      },
 *      {
 *        "priority": 5,
 *        "recommendationType": "INSIGHT_B1",
 *        "recommendationTemplate": "INSIGHT_B1_MONTHLY_CONSUMPTION_INCR",
 *        "recommendationCode": "IB1",
 *        "description": "8.0lt vs. the average 22.2lt",
 *        "description": "6598.0lt vs. the average 2652.5lt",
 *        "link": null,
 *        "body": "148% more than your monthly average",
 *        "id": 5029,
 *        "locale": "en",
 *        "title": "148% more than your monthly average",
 *        "refDate": 1479160800000,
 *        "acknowledgedOn": null,
 *        "createdOn": 1487756580059,
 *        "type": "RECOMMENDATION"
 *      },
 *      {
 *        "priority": 5,
 *        "recommendationType": "INSIGHT_B2",
 *        "recommendationTemplate": "INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR",
 *        "recommendationCode": "IB2",
 *        "description": "8.0lt vs. the average 22.2lt",
 *        "description": "6598.0lt vs. 2535.0lt",
 *        "link": null,
 *        "body": "160% more than previous month",
 *        "id": 5034,
 *        "locale": "en",
 *        "title": "160% more than previous month",
 *        "refDate": 1479160800000,
 *        "acknowledgedOn": null,
 *        "createdOn": 1487756630246,
 *        "type": "RECOMMENDATION"
 *      },
 *    ],
 *    "totalRecommendations": 8,
 *    "tips": [],
 *    "totalTips": 0,
 *    "announcements": [],
 *    "totalAnnouncements": 0,
 *    "success": true
 *  }
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
 *     code: "UserErrorCode.USERNAME_NOT_FOUND",
 *     description: "Account a9509da9-edf5-4838-acf4-8f1b73485d7a was not found."
 *   }],
 *   success: false
 * }
 *
 *
 */
function getMessages() { return; }
 
 
/**
 * @api {post} action/message/acknowledge Acknowledge messages
 * @apiName MessageAcknowledge
 * @apiGroup Message
 * @apiPermission ROLE_USER
 *
 * @apiDescription Marks one or more messages as acknowledged.
 *
 * @apiParam                            {Object[]} messages               Array of <code>MessageAcknowledgement</code>.
 *
 * @apiParam (MessageAcknowledgement)   {String}   type                   Message type. Valid values are <code>ALERT</code>, <code>TIP</code>, <code>RECOMMENDATION</code> and <code>ANNOUNCEMENT</code>. This parameter is not case sensitive.
 * @apiParam (MessageAcknowledgement)   {Number}   id                     Message id. This id is unique per message type.
 * @apiParam (MessageAcknowledgement)   {Number}   timestamp              A timestamp the message was read by the user i.e. the timestamp at the mobile device.
 *
 * @apiParamExample {json} Request Example
 * {
 *   "messages": [{
 *     "id": 1625,
 *     "timestamp": 1463667895000,
 *     "type": "ALERT"
 *   }, {
 *     "id": 1797,
 *     "timestamp": 1463667895000,
 *     "type": "ALERT"
 *   }]
 * }
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   Empty array of error messages.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   success: true
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
 *     description: "Unknown message acknowledgement type."
 *   }],
 *   success: false
 * }
 */
function acknowledgeMessages() { return; }


/**
 * @api {get} action/tip/localized/:locale Fetch all tips
 * @apiName TipList
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Fetch all tips for a specified locale
 * @apiParam   {String}  locale  The target locale as a ISO-639 language code (e.g. <code>el</code>). 
 * 
 * @apiSuccess {Boolean}  success    <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors     Empty array of error messages.
 * @apiSuccess {String} type    The message type (always <code>TIP</code>)
 * @apiSuccess {Object[]} messages  An list of messages as an array of <code>Tip</code> objects.
 * @apiUse Message
 * @apiUse Tip
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "success": true
 *   "errors": [],
 *   "type": "TIP",
 *   "messages": [
 *     {
 *       "index": 2,
 *       "description": "Some tips advise you to brush your teeth in the shower - DON’T. If you brush for three minutes that’s about 20  liters wasted. Brush in the sink with the tap turned off",
 *        "categoryName": null,
 *        "imageMimeType": "jpg",
 *        "imageLink": null,
 *        "imageEncoded": "...", # base64-encoded binary data
 *        "prompt": null,
 *        "externalLink": null,
 *        "source": null,
 *        "active": true,
 *        "body": "Some tips advise you to brush your teeth in the shower - DON’T. If you brush for three minutes that’s about 20 liters wasted. Brush in the sink with the tap turned off",
 *        "id": 242,
 *        "locale": "en",
 *        "title": "Don’t multi-task!",
 *        "acknowledgedOn": null,
 *        "createdOn": 1465289444685,
 *        "modifiedOn": null,
 *        "type": "TIP"
 *     },
 *     ...
 *   ]
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
 *     code: "SharedErrorCode.NO_SUCH_LOCALE",
 *     description: "No such locale"
 *   }],
 *   success: false
 * }

 */
function getLocalizedTips() { return; }


/**
 * @api {post} action/tip/status/save Enable tips
 * @apiName TipEnable
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 *
 * @apiDescription Enable or disable certain tips
 *
 * @apiParam   {TipStatus[]}  ROOT  List of <code>TipStatus</code>.
 * @apiParam  (TipStatus)  {Number} id  The tip Id
 * @apiParam  (TipStatus)  {Boolean} status The tip status
 * 
 * @apiParamExample {json} Request Example
 * [
 *   {
 *     "id": 1625,
 *     "status": true
 *   }, 
 *   {
 *     "id": 1797,
 *     "status": false
 *   }
 * ]
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   An array of error messages, if any.
 *
 * @apiSuccessExample {json} Response Example
 * HTTP/1.1 200 OK
 * {
 *   "errors": [],
 *   success: true
 * }
 */
function enableTips() { return; }

/**
 * @api {post} action/tip/save Save tip
 * @apiName TipSave
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 * @apiDescription Save (create or update) a tip
 * 
 * @apiParam {Number}  id                Message id. If supplied, an update will be performed, otherwise a new tip will be created.
 * @apiParam {String}  title             Title
 * @apiParam {String}  description       Long description.
 * @apiParam {String}  locale            The language code. If updating a tip, can be omitted
 * @apiParam {String}  [categoryName]    Category Name.
 * @apiParam {String}  [imageEncoded]    Image encoded to base64.
 * @apiParam {String}  [imageLink]       Image resource link.
 * @apiParam {String}  [imageMimeType]   Image MIME type.
 * @apiParam {String}  [prompt]          Prompt message.
 * @apiParam {String}  [externalLink]    External link.
 * @apiParam {Boolean} [active]          <code>true</code> if message is active; Otherwise <code>false</code>.
 * 
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   An array of error messages, if any.
 */
function saveTip() { return; }


/**
 * @api {post} action/tip/delete/:id Delete tip
 * @apiName TipDelete
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 * @apiDescription Delete a tip
 *
 * @apiParam {Number}  id  Message id
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   An array of error messages, if any.
 */
function deleteTip() { return; }

/**
 * @api {post} action/announcement/broadcast Broadcast announcement
 * @apiName AnnouncementBroadcast
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 * @apiDescription Broadcast an announcement to a group of users.
 *
 * @apiParam {Object[]} receivers A list of receivers as an array of <code>ReceiverAccount</code> objects
 * @apiParam {Object}  announcement The new announcement
 * @apiParam {Object}  announcement.title A title for the announcement.
 * @apiParam {Number}  [announcement.priority]  Message priority.
 * @apiParam {String}  announcement.content   Announcement details.
 * @apiParam {String}  [announcement.link]      Image resource link.
 
 * @apiParam (ReceiverAccount) {Number} id The account id 
 * @apiParam (ReceiverAccount) {String} username The username. Can be used to identify an account by username (if id is missing).
 *
 * @apiSuccess {Boolean}  success                  <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors                   An array of error messages, if any.
 */
function broadcastAnnouncement() { return; }

/**
 * @api {get} action/announcement/history Fetch all announcements
 * @apiName AnnouncementHistory
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 * @apiDescription Fetch all announcements for current locale
 *
 * @apiSuccess {Boolean}  success   <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors    An array of error messages, if any.
 * @apiSuccess {String}  type       The type of messages (always <code>ANNOUNCEMENT</code>)
 * @apiSuccess {Object}  messages   A list of messages as an array of <code>Announcement</code> objects
 * @apiUse Message
 * @apiUse Announcement
 */
function getAnnouncementsHistory() { return; }

/**
 * @api {post} action/announcement/delete/:id Delete announcement
 * @apiName AnnouncementDelete
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 * @apiDescription Delete announcement by id
 *
 * @apiParam {Number} id The message id
 * 
 * @apiSuccess {Boolean}  success   <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors    An array of error messages, if any.
 */
function deleteAnnouncement() { return; }

/**
 * @api {get} action/announcement/details/:id Fetch announcement details
 * @apiName AnnouncementDetails
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 * @apiDescription Fetch announcement details (content and receiver accounts)
 * 
 * @apiParam {Number} id The message id
 * 
 * @apiSuccess {Boolean}  success   <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors    An array of error messages, if any.
 * @apiSuccess {Object[]} receivers A list of receivers as an array of <code>ReceiverAccount</code>
 * @apiSuccess {Object}   announcement The announcement as an <code>Announcement</code> object
 * @apiUse ReceiverAccount
 * @apiUse Message
 * @apiUse Announcement
 *
 */
function getAnnouncementDetails() { return; }


/**
 * @api {post} action/alert/receivers Get alert receivers
 * @apiName AlertReceivers
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 * @apiDescription Fetch receivers for alerts of a given type
 * 
 * @apiParam {String} type The alert type. See <code>Alert#alertType</code> for a list of valid names. 
 * @apiParam {Object} [query] A query that further describes a subset of generated messages.
 * @apiParam {Object} [query.time] A time filter
 * @apiParam {Object} query.time.start A timestamp to start from
 * @apiParam {Object} query.time.end A timestamp to end to
 *
 * @apiSuccess {Boolean}  success   <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors    An array of error messages, if any.
 * @apiSuccess {String} type The alert type (as requested)
 * @apiSuccess {Object[]} receivers A list of receivers as an array of <code>ReceiverAccount</code>
 * @apiUse ReceiverAccount
 */
function getAlertReceivers() { return; }


/**
 * @api {post} action/recommendation/receivers Get recommendation receivers
 * @apiName RecommendationReceivers
 * @apiGroup Message
 * @apiPermission ROLE_UTILITY_ADMIN
 * @apiDescription Fetch receivers for recommendations of a given type
 *
 * @apiParam {String} type The recommendation type. See <code>Recommendation#recommendationType</code> for a list of valid names. 
 * @apiParam {Object} [query] A query that further describes a subset of generated messages.
 * @apiParam {Object} query.time.start A timestamp to start from
 * @apiParam {Object} query.time.end A timestamp to end to
 *
 * @apiSuccess {Boolean}  success   <code>true</code> or <code>false</code> indicating success of the operation.
 * @apiSuccess {Object[]} errors    An array of error messages, if any.
 * @apiSuccess {String} type The recommendation type (as requested)
 * @apiSuccess {Object[]} receivers A list of receivers as an array of <code>ReceiverAccount</code>
 * @apiUse ReceiverAccount
 */
function getRecommendationReceivers() { return; }

