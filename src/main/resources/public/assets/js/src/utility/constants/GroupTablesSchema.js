
var GroupTablesSchema = {
    
  Members: {
    fields: [
      {
        name: 'id',
        hidden: true
      }, {
        name: 'user',
      title: 'Username',
        link: '/user/{id}'
      }, {
        name: 'registeredOn',
        title: 'Registered On',
        type: 'datetime'
      }, {
        name: 'email',
        title: 'Email'
      }, {
        name: 'map',
        type:'action',
        icon: 'map-o',
        handler: function() {
          console.log(this);
        }
      }, {
        name: 'message',
        type:'action',
        icon: 'envelope-o',
        handler: function() {
          console.log(this);
        }
      }, {
        name: 'bookmark',
        type:'action',
        icon: 'bookmark-o',
        handler: function() {
          console.log(this);
        }
      }, {
        name: 'export',
        type:'action',
        icon: 'cloud-download',
        handler: function() {
          console.log(this);
        }
      }, {
        name: 'add-chart',
        type:'action',
        icon: 'bar-chart',
        handler: function() {
          console.log(this);
        }
      }, {
        name: 'remove',
        type:'action',
        icon: 'remove',
        handler: function() {
          console.log(this);
        }
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