package eu.daiad.web.model.group;

import eu.daiad.web.domain.application.GroupCommonsEntity;

public class CommonsInfo {

    private Commons group;

    private boolean owner = false;

    private boolean member = false;

    public CommonsInfo(GroupCommonsEntity entity) {
        group = new Commons();

        group.setKey(entity.getKey());
        group.setUtilityKey(entity.getUtility().getKey());

        group.setName(entity.getName());
        group.setDescription(entity.getDescription());
        group.setCreatedOn(entity.getCreatedOn());
        group.setUpdatedOn(entity.getUpdatedOn());

        group.setGeometry(entity.getGeometry());
        group.setSize(entity.getSize());
        group.setImage(entity.getImage());
    }

    public CommonsInfo(GroupCommonsEntity entity, boolean isOwner, boolean isMember) {
        this(entity);

        setOwner(isOwner);
        setMember(isMember);
    }

    public Commons getGroup() {
        return group;
    }

    public void setGroup(Commons group) {
        this.group = group;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isMember() {
        return member;
    }

    public void setMember(boolean member) {
        this.member = member;
    }

}
