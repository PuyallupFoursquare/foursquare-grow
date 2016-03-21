package com.p4square.f1oauth;

import com.codahale.metrics.MetricRegistry;
import com.p4square.grow.config.Config;
import com.p4square.grow.frontend.IntegrationDriver;
import org.restlet.Context;
import org.restlet.security.Verifier;

/**
 * The FellowshipOneIntegrationDriver creates implementations of various
 * objects to support integration with Fellowship One.
 */
public class FellowshipOneIntegrationDriver implements IntegrationDriver {

    private final Context mContext;
    private final MetricRegistry mMetricRegistry;
    private final Config mConfig;
    private final F1Access mAPI;

    public FellowshipOneIntegrationDriver(final Context context) {
        mContext = context;
        mConfig = (Config) context.getAttributes().get("com.p4square.grow.config");
        mMetricRegistry = (MetricRegistry) context.getAttributes().get("com.p4square.grow.metrics");

        mAPI = new F1Access(context,
                            mConfig.getString("f1ConsumerKey", ""),
                            mConfig.getString("f1ConsumerSecret", ""),
                            mConfig.getString("f1BaseUrl", "staging.fellowshiponeapi.com"),
                            mConfig.getString("f1ChurchCode", "pfseawa"),
                            F1Access.UserType.WEBLINK);
        mAPI.setMetricRegistry(mMetricRegistry);
    }

    /**
     * @return An F1Access instance.
     */
    public F1Access getF1Access() {
        return mAPI;
    }

    @Override
    public Verifier newUserAuthenticationVerifier() {
        return new SecondPartyVerifier(mContext, mAPI);
    }
}
