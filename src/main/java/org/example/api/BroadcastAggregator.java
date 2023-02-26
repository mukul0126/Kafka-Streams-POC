package org.example.api;

import org.apache.kafka.streams.kstream.Aggregator;

public class BroadcastAggregator implements Aggregator<String, BroadcastStats, BroadcastStats> {
  @Override
  public BroadcastStats apply(String key, BroadcastStats value, BroadcastStats aggregate) {
    if(aggregate == null) {
      return value;
    }
    aggregate.setRead(aggregate.getRead() + value.getRead());
    aggregate.setFailed(aggregate.getFailed() + value.getFailed());
    aggregate.setDelivered(aggregate.getDelivered()+ value.getDelivered());
    aggregate.setSent(aggregate.getSent() + value.getSent());
    return aggregate;
  }
}
