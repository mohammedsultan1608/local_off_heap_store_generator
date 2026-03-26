package com.localoffheap.snapshot;

public record SnapshotRow(String entityKey, long version, byte[] payload) {
}
