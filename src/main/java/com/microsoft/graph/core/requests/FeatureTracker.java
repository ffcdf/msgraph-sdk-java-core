package com.microsoft.graph.core.requests;


import jakarta.annotation.Nonnull;

/**
 * Manages and tracks the flags for tasks and handlers.
 */
public class FeatureTracker {
    /**
     * Default constructor
     */
    public FeatureTracker() {
        //Default constructor
    }

    private int featureUsage = FeatureFlag.NONE_FLAG;
    /**
     * Sets a numeric representation of the SDK feature usage
     * @param flag a numeric representation of the SDK feature usage
     */
    public void setFeatureUsage(@Nonnull int flag) {

        featureUsage = featureUsage | flag;
    }
    /**
     * Gets a numeric representation of the SDK feature usage
     * @return a numeric representation of the SDK feature usage
     */
    public int getFeatureUsage() {
        return featureUsage;
    }

    /**
     * Gets a serialized representation of the SDK feature usage.
     * @return a serialized representation of the SDK feature usage
     */
    @Nonnull
    public String getSerializedFeatureUsage() {
        return Integer.toHexString(featureUsage);
    }
}
