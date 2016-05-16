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
            handler: function() {}
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
    },
    
    Favourites: {
      fields: [{
        name: 'id',
        hidden: true
      }, {
        name: 'type',
        title: 'Demographics.Favourites.Type'
      }, {
        name: 'name',
        title: 'Demographics.Favourites.Name',
        link: function(row) {
          switch(row.type) {
            case 'Account':
              return '/user/{id}';
            case 'Commons': case 'Group':
              return '/group/{id}';
          }
          return null;
        }
      }, {
        name: 'addedOn',
        title: 'Demographics.Favourites.AddedOn',
        type: 'datetime'
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
    },
    
    GroupMembers : {
      fields: [{
        name: 'id',
        hidden: true
      }, {
        name: 'name',
        title: 'Demographics.GroupMembers.Name'
      }, {
        name: 'email',
        title: 'Demographics.GroupMembers.Email'     
      }, {
        name: 'registeredOn',
        title: 'Demographics.GroupMembers.RegisteredOn',
        type: 'datetime'
      }, {
        name: 'selected',
        type: 'alterable-boolean',
        handler: null
      }],
      
      rows: [],
      
      pager: {
        index: 0,
        size: 10      
      }
    }
    
};

module.exports = DemographicsTablesSchema;