package eu.daiad.web.model.favourite;

import java.util.UUID;

import eu.daiad.web.domain.application.FavouriteAccountEntity;
import eu.daiad.web.domain.application.FavouriteEntity;
import eu.daiad.web.domain.application.FavouriteGroupEntity;

public class FavouriteInfo {

    private UUID key;

    private UUID refId;

    private String name;

    private EnumFavouriteType type;

    private long additionDateMils;

    public FavouriteInfo(UUID refKey) {
        key = null;
        refId = refKey;
        name = null;
        type = null;
        additionDateMils = -1;
    }

    public FavouriteInfo(FavouriteEntity favourite) {
        key = favourite.getKey();
        name = favourite.getLabel();
        type = favourite.getType();
        additionDateMils = favourite.getCreatedOn().getMillis();

        switch (favourite.getType()) {
            case ACCOUNT:
                FavouriteAccountEntity accountFavourite = (FavouriteAccountEntity) favourite;
                refId = accountFavourite.getAccount().getKey();
                break;
            case GROUP:
                FavouriteGroupEntity groupFavourite = (FavouriteGroupEntity) favourite;
                refId = groupFavourite.getGroup().getKey();
                break;
            default:
                refId = favourite.getKey();
        }
    }

    public UUID getRefId() {
        return refId;
    }

    public String getName() {
        return name;
    }

    public EnumFavouriteType getType() {
        return type;
    }

    public long getAdditionDateMils() {
        return additionDateMils;
    }

    public UUID getKey() {
        return key;
    }

}
