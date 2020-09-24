package eu.daiad.common.model.group;

import java.util.UUID;

public class Set extends Group {

    private UUID ownerKey;

    public Set(UUID ownerKey) {
        this.ownerKey = ownerKey;
    }

    public UUID getOwnerKey() {
        return ownerKey;
    }

    @Override
    public EnumGroupType getType() {
        return EnumGroupType.SET;
    }

}
