var DemographicsTablesSchema = {
    
    Groups: {
      fields: [{
        name: 'id',
        title: 'Table.Group.id',
        hidden: true
      }, {
        name: 'name',
        title: 'Table.Group.name',
        link: '/group/{id}'
      }, {
        name: 'size',
        title: 'Table.Group.size'
      }, {
        name: 'createdOn',
        title: 'Table.Group.createdOn'
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
            name: 'add-favourite',
            type:'action',
            icon: 'bookmark-o',
            handler: function() {
              console.log(this);
            }
      }, {
            name: 'chart',
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
      }],
      
      rows : [],
      
      pager: {
        index: 0,
        size: 10
       }
    }
    
};

module.exports = DemographicsTablesSchema;