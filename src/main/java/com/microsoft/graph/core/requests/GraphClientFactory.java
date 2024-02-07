package com.microsoft.graph.core.requests;

import com.microsoft.graph.core.CoreConstants;
import com.microsoft.graph.core.requests.middleware.GraphTelemetryHandler;
import com.microsoft.graph.core.requests.options.GraphClientOption;
import com.microsoft.kiota.http.KiotaClientFactory;
import com.microsoft.kiota.http.middleware.UrlReplaceHandler;
import com.microsoft.kiota.http.middleware.options.UrlReplaceHandlerOption;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

/**
 * The GraphClientFactory used to create the OkHttpClient.
 */
public class GraphClientFactory {
    private GraphClientFactory() { }
    /**
     * The default OkHttpClient Builder for Graph.
     *
     * @return an OkHttpClient Builder instance.
     */
    @Nonnull
    public static OkHttpClient.Builder create() {
        return create(new GraphClientOption());
    }
    /**
     * OkHttpClient Builder for Graph with specified Interceptors.
     *
     * @param interceptors desired interceptors for use in requests.
     * @return an OkHttpClient Builder instance.
     */
    @Nonnull
    public static OkHttpClient.Builder create(@Nonnull Interceptor... interceptors) {
        return create(new GraphClientOption(), interceptors);
    }
    /**
     * OkHttpClient Builder for Graph with specified Interceptors and GraphClientOption.
     *
     * @param interceptors desired interceptors for use in requests.
     * @param graphClientOption the GraphClientOption for use in requests.
     * @return an OkHttpClient Builder instance.
     */
    @Nonnull
    public static OkHttpClient.Builder create(@Nonnull GraphClientOption graphClientOption, @Nonnull Interceptor... interceptors) {
        final OkHttpClient.Builder builder = create(graphClientOption);
        //Skip adding interceptor if that class of interceptor already exist.
        final List<String> appliedInterceptors = new ArrayList<>();
        for(Interceptor interceptor: builder.interceptors()) {
            appliedInterceptors.add(interceptor.getClass().toString());
        }
        for (Interceptor interceptor:interceptors){
            if(appliedInterceptors.contains(interceptor.getClass().toString())) {
                continue;
            }
            builder.addInterceptor(interceptor);
        }
        return builder;
    }
    /**
     * The OkHttpClient Builder with optional GraphClientOption
     *
     * @param graphClientOption the GraphClientOption for use in requests.
     * @return an OkHttpClient Builder instance.
     */
    @Nonnull
    public static OkHttpClient.Builder create(@Nullable GraphClientOption graphClientOption) {
        GraphClientOption options = graphClientOption != null ? graphClientOption : new GraphClientOption();
        return KiotaClientFactory.create(createDefaultGraphInterceptors(options));
    }
    /**
     * Creates the default Interceptors for use with Graph.
     *
     * @param graphClientOption the GraphClientOption used to create the GraphTelemetryHandler with.
     * @return an array of interceptors.
     */
    @Nonnull
    public static Interceptor[] createDefaultGraphInterceptors(@Nonnull GraphClientOption graphClientOption) {
        List<Interceptor> handlers = new ArrayList<>();
        addDefaultFeatureUsages(graphClientOption);

        handlers.add(new UrlReplaceHandler(new UrlReplaceHandlerOption(CoreConstants.ReplacementConstants.getDefaultReplacementPairs())));
        handlers.add(new GraphTelemetryHandler(graphClientOption));
        handlers.addAll(Arrays.asList(KiotaClientFactory.createDefaultInterceptors()));
        return handlers.toArray(new Interceptor[0]);
    }
    //These are the default features used by the Graph Client
    private static void addDefaultFeatureUsages(GraphClientOption graphClientOption) {
        graphClientOption.featureTracker.setFeatureUsage(FeatureFlag.RETRY_HANDLER_FLAG);
        graphClientOption.featureTracker.setFeatureUsage(FeatureFlag.REDIRECT_HANDLER_FLAG);
        graphClientOption.featureTracker.setFeatureUsage(FeatureFlag.URL_REPLACEMENT_FLAG);
        graphClientOption.featureTracker.setFeatureUsage(FeatureFlag.BATCH_REQUEST_FLAG);
    }
}
