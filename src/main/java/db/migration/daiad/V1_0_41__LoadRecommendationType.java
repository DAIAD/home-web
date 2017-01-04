package db.migration.daiad;

import java.util.Arrays;

import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang.StringUtils;

import org.flywaydb.core.api.migration.MigrationChecksumProvider;

import db.initialization.LoadRecommendationType;
import eu.daiad.web.model.EnumToNameTransformer;
import eu.daiad.web.model.message.EnumRecommendationType;

public class V1_0_41__LoadRecommendationType extends LoadRecommendationType
    implements MigrationChecksumProvider
{
    private static final Transformer<EnumRecommendationType, String> TRANSFORM_TO_NAME =
        new EnumToNameTransformer<>();

    @Override
    public Integer getChecksum()
    { 
        String[] names = FluentIterable.of(EnumRecommendationType.values())
            .transform(TRANSFORM_TO_NAME)
            .toArray(String.class);
        
        Arrays.sort(names);
        return StringUtils.join(names, "$").hashCode();
    }
}
