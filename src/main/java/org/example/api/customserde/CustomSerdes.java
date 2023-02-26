package org.example.api.customserde;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.example.api.BroadcastStats;

public final class CustomSerdes {
  private CustomSerdes() {}
  public static Serde<BroadcastStats> broadcastStatsSerde() {
    JsonSerializer<BroadcastStats> serializer = new JsonSerializer<>();
    JsonDeserializer<BroadcastStats> deserializer = new JsonDeserializer<>(BroadcastStats.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }
}