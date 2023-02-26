package org.example.api;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.example.api.customserde.CustomSerdes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;

@Component
@EnableKafkaStreams
public class BroadcastAggregationDSL {

  @Autowired
  private KafkaStreamsConfiguration kafkaStreamsConfiguration;

  @Autowired
  private StreamsBuilder builder;


  StreamsBuilderFactoryBean streamsBuilderFactoryBean;

  @PostConstruct
  private void processIncomingMessagesKStream() {
    Map<String, Object> props = new HashMap<>();
    props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(COMMIT_INTERVAL_MS_CONFIG, 2000);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    kafkaStreamsConfiguration.asProperties().putAll(props);
    KStream<String, String> packets =
        builder.stream("test.topic", Consumed.with(Serdes.String(), Serdes.String()));
    packets.transform(StatusPacketTransformer::new)
        .groupByKey(Grouped.valueSerde(CustomSerdes.broadcastStatsSerde())).windowedBy(
            TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(2)))
        .aggregate(() -> null, new BroadcastAggregator(), Materialized.with(Serdes.String(), CustomSerdes.broadcastStatsSerde()))
        .suppress(Suppressed.untilWindowCloses(unbounded())).toStream()
        .map((windowKey, value) -> KeyValue.pair(windowKey.key(), value))
        .map((key, val) -> {
              System.out.println("#######################" + key + "  " + val);
              return KeyValue.pair(key, val);
            }
        )
        .to("sink-topic", Produced.with(Serdes.String(), CustomSerdes.broadcastStatsSerde()));
  }
}
