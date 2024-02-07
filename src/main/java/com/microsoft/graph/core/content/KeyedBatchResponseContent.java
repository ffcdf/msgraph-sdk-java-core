package com.microsoft.graph.core.content;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * A model to map id Keys to requests within a BatchResponseContent object.
 */
public class KeyedBatchResponseContent {
    /**
     * The ids of the requests that were batched together.
     */
    @Nonnull
    protected HashSet<String> keys;
    /**
     * The BatchResponseContent object paired to the keys.
     */
    @Nonnull
    protected BatchResponseContent response;
    /**
     * Instantiates a new Keyed batch response content.
     * @param keys the ids of the requests that were batched together.
     * @param response the BatchResponseContent object to add to the collection.
     */
    public KeyedBatchResponseContent(@Nonnull Set<String> keys, @Nonnull BatchResponseContent response) {
        this.keys = new HashSet<>(keys);
        this.response = response;
    }
}
