package com.booking.replication.augmenter.model;

@SuppressWarnings("unused")
public interface TableAugmentedEventData extends AugmentedEventData {
    AugmentedEventTable getEventTable();
}
