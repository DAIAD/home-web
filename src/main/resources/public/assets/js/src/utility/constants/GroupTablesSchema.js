
var GroupTablesSchema = {
    
  Members: {
    fields: [
      {
        name: 'id',
        hidden: true
      }, {
        name: 'user',
      title: 'User',
        link: '/user/{id}'
      }, {
        name: 'email',
        title: 'Email'
      }, {
        name: 'registeredOn',
        title: 'Registered On',
        type: 'datetime'
      }
    ],
      
    rows : [],
      
    pager: {
      index: 0,
      size: 10
    }
  }
};

module.exports = GroupTablesSchema;