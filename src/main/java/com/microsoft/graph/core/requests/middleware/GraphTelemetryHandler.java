package com.microsoft.graph.core.requests.middleware;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

import com.microsoft.graph.core.requests.options.GraphClientOption;
import jakarta.annotation.Nonnull;

import com.microsoft.graph.core.CoreConstants;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Middleware responsible for adding telemetry information on SDK usage
 * Note: the telemetry only collects anonymous information on SDK version and usage. No personal information is collected.
 */
public class GraphTelemetryHandler implements Interceptor{

    private GraphClientOption mGraphClientOption;

    /**
     * Instantiate a GraphTelemetryHandler with default GraphClientOption.
     */
    public GraphTelemetryHandler(){
        this(new GraphClientOption());
    }
    /**
     * Instantiate a GraphTelemetryHandler with specified GraphClientOption
     * @param graphClientOption the specified GraphClientOption for the GraphTelemetryHandler.
     */
    public GraphTelemetryHandler(@Nonnull final GraphClientOption graphClientOption){
        this.mGraphClientOption = Objects.requireNonNull(graphClientOption);
    }

    @Override
    @Nonnull
    public Response intercept(@Nonnull final Chain chain) throws IOException {
        final Request request = chain.request();
        final Request.Builder telemetryAddedBuilder = request.newBuilder();

        final String clientLibraryUsed = "graph-java" + (mGraphClientOption.getGraphServiceTargetVersion().equals("v1.0") ? ""  : "-"+mGraphClientOption.getGraphServiceTargetVersion()); //graph-java | graph-java-beta
        final String sdkVersion = (mGraphClientOption.getClientLibraryVersion() == null ? "" : "/"+ mGraphClientOption.getClientLibraryVersion()); //SDK version value

        final String coreVersionHeader = CoreConstants.Headers.GRAPH_VERSION_PREFIX + "/" + mGraphClientOption.getCoreLibraryVersion(); //"graph-java-core/3.0.0"

        final String featureUsage = "(featureUsage=" + mGraphClientOption.featureTracker.getSerializedFeatureUsage(); // (featureUsage=<featureFlag>

        final String jreVersion = System.getProperty("java.version");
        final String jreVersionHeader = (CoreConstants.Headers.DEFAULT_VERSION_VALUE.equals(jreVersion) ? "" : ("; runtimeEnvironment=JRE/"+jreVersion)); //runtimeEnvironment=JRE/<JRE version>

        final String androidVersion = getAndroidAPILevel(); // android/<version value>
        final String androidVersionHeader = (CoreConstants.Headers.DEFAULT_VERSION_VALUE.equals(androidVersion) ? "" : ("; " + CoreConstants.Headers.ANDROID_VERSION_PREFIX + "/" + androidVersion));

        final String sdkTelemetry = clientLibraryUsed + sdkVersion + ", " + coreVersionHeader + " " + featureUsage + jreVersionHeader + androidVersionHeader+")";

        telemetryAddedBuilder.addHeader(CoreConstants.Headers.SDK_VERSION_HEADER_NAME, sdkTelemetry);
        if(request.header(CoreConstants.Headers.CLIENT_REQUEST_ID) == null) {
            telemetryAddedBuilder.addHeader(CoreConstants.Headers.CLIENT_REQUEST_ID, mGraphClientOption.getClientRequestId());
        }

        return chain.proceed(telemetryAddedBuilder.build());
    }

    private String androidAPILevel;
    private String getAndroidAPILevel() {
        if(androidAPILevel == null) {
            androidAPILevel = getAndroidAPILevelInternal();
        }
        return androidAPILevel;
    }

    private String getAndroidAPILevelInternal() {
        try {
            final Class<?> buildClass = Class.forName("android.os.Build");
            final Class<?>[] subclasses = buildClass.getDeclaredClasses();
            Class<?> versionClass = null;
            for(final Class<?> subclass : subclasses) {
                if(subclass.getName().endsWith("VERSION")) {
                    versionClass = subclass;
                    break;
                }
            }
            if(versionClass == null)
                return CoreConstants.Headers.DEFAULT_VERSION_VALUE;
            else {
                final Field sdkVersionField = versionClass.getField("SDK_INT");
                final Object value = sdkVersionField.get(null);
                final String valueStr = String.valueOf(value);
                return valueStr == null || valueStr.equals("") ? CoreConstants.Headers.DEFAULT_VERSION_VALUE : valueStr;
            }
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchFieldException ex) {
            // we're not on android and return "0" to align with java version which returns "0" when running on android
            return CoreConstants.Headers.DEFAULT_VERSION_VALUE;
        }
    }
}
