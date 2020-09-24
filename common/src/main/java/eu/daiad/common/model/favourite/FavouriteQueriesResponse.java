package eu.daiad.common.model.favourite;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.query.DataQuery;

import java.util.List;

public class FavouriteQueriesResponse extends RestResponse {
    
    private List<DataQuery> favourites;

    public List<DataQuery> getFavourites() {
        return favourites;
    }

    public void setFavourites(List<DataQuery> favourites) {
        this.favourites = favourites;
    }
}
