var AnnouncementsTableSchema = {
        fields: [{
          name: 'id',
          title: 'id',
          hidden: true
        }, {
          name: 'key',
          title: 'key',
          hidden: true
        }, {
          name: 'username',
          title: 'Username'
        }, {
          name: 'accountRegisteredOn',
          title: 'Registered On',
          type: 'datetime'
        }, {
          name: 'lastLoginSuccess',
          title: 'Last login on',
          type: 'datetime'
        }, {
          name: 'selected',
          type:'alterable-boolean',
          handler: null        
        }],
        rows: [],
        pager: {
          index: 0,
          size: 10
        }
    };    

module.exports = AnnouncementsTableSchema;