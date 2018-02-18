package org.pragyan.dalal18.data;

import dalalstreet.api.datastreams.SubscriptionId;

public class Subscription {

    public enum SubscriptionType {
        TRANSACTIONS,
        STOCK_PRICES,
        MARKET_EVENTS,
        STOCK_EXCHANGE
    }

    private SubscriptionType type;
    private SubscriptionId subscriptionId;

    public Subscription(SubscriptionType type, SubscriptionId subscriptionId) {
        this.type = type;
        this.subscriptionId = subscriptionId;
    }

    public SubscriptionType getType() {
        return type;
    }

    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }
}
