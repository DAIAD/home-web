var ModeManagementTableSchema = {
    
    filters: [{
          id: 'utilityName',
          name: 'Utility',
          field: 'utilityName',
          icon: 'utility',
          type: 'text'
        }], 
    
    
    fields :[{
          name: 'index',
          title: 'ID'
        }, {
          name: 'title',
          title: 'Title'
        }, {
          name: 'description',
          title: 'Description'
        }, {
          name: 'createdOn',
          title: 'Created',
          type: 'datetime'
        }, {
            name: 'modifiedOn',
            title: 'Modified',
            type: 'datetime'
        }, {
          name: 'edit',
          type:'action',
          icon: 'pencil',
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
    
    pager: {
      index: 1,
      size: 10
    }
    
};

module.exports = ModeManagementTableSchema;