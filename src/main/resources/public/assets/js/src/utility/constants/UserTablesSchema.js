var UserTablesSchema = {
  Groups : {
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
      }],
      
      rows : [],
        
      pager: {
        index: 0,
        size: 10
      }
    }
};

module.exports = UserTablesSchema;