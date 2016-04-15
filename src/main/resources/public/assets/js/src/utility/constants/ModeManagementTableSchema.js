var ModeManagementTableSchema = {
    
    filters: [{
      id: 'groupName',
      name: 'Group',
      field: 'groupName',
      icon: 'group',
      type: 'text',
    }, {
      id: 'amphiro',
      name: 'b1',
      field: 'amphiro',
      icon: 'tachometer',
      type: 'boolean',    
    }, {
      id: 'mobile',
      name: 'Mobile',
      field: 'mobile',
      icon: 'mobile',
      type: 'boolean',
    }, {
      id: 'social',
      name: 'Social',
      field: 'social',
      icon: 'share',
      type: 'boolean',
    }], 
    
    
    fields :[{
      name: 'id',
      title: 'Table.User.id',
      hidden: true
    }, {
      name: 'active',
      title: 'Table.User.active',
      hidden: true
    }, {
      name: 'groupId',
      title: 'Table.User.group',
      hidden: true
    }, {
      name: 'name',
      title: 'Table.User.name',
      link: '/user/{id}'
    }, {
      name: 'groupName',
      title: 'Table.User.group',
      link: '/group/{groupId}'
    }, {
      name: 'amphiro',
      title: 'Table.User.viewInfoOnAmphiro',
      type:'property'
    }, {
      name: 'mobile',
      title: 'Table.User.viewInfoOnMobile',
      type:'property'
    }, {
      name: 'social',
      title: 'Table.User.allowSocial',
      type:'property'
    }, {
      name: 'deactivate',
      title: 'Table.User.deactivateUser',
      type:'action',
      icon: 'user-times',
      handler: null
    }],
    
    pager: {
      index: 1,
      size: 10
    }
    
};

module.exports = ModeManagementTableSchema;