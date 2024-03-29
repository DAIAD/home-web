package eu.daiad.common.model.billing;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.model.RestResponse;

public class PriceBracketCollectionResult extends RestResponse {

    List<PriceBracket> brackets = new ArrayList<PriceBracket>();

    public PriceBracketCollectionResult(List<PriceBracket> brackets) {
        this.brackets.addAll(brackets);
    }

    public List<PriceBracket> getBrackets() {
        return brackets;
    }

}
