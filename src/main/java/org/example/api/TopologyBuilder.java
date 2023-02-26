package org.example.api;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windows;
import org.apache.kafka.streams.kstream.internals.KStreamWindowAggregate;
import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.apache.kafka.streams.state.Stores;
import org.example.api.customserde.CustomSerdes;
import org.example.api.customserde.JsonSerializer;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.REPLICATION_FACTOR_CONFIG;

@Component
//@EnableKafkaStreams
public class TopologyBuilder {

  //  @Autowired
  private KafkaStreamsConfiguration kafkaStreamsConfiguration;

  private KafkaStreams streams;

//  @PostConstruct
  public void init() {
    Map<String, Object> props = new HashMap<>();
    props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(COMMIT_INTERVAL_MS_CONFIG, 2000);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    props.put(BOOTSTRAP_SERVERS_CONFIG, "kafka-ha1.dev.engati.local:9092");
    props.put(APPLICATION_ID_CONFIG, "bpds");
    props.put(REPLICATION_FACTOR_CONFIG, "1");//3
    props.put(GROUP_ID_CONFIG, "bpds");
    //    kafkaStreamsConfiguration.asProperties().putAll(props);
    Windows<TimeWindow> window = TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5));
    Topology builder = new Topology();
    builder.addSource("Source-1", "test.topic")
        .addProcessor("Process", StatusPacketProcessor::new, "Source-1")

        .addProcessor("Aggregate",
            () -> new KStreamWindowAggregate<String, BroadcastStats, BroadcastStats, TimeWindow>(window,
                "broadcastAggregate", () -> null, new BroadcastAggregator()).get(), "Process")
        .addStateStore(Stores.timestampedWindowStoreBuilder(
            Stores.persistentTimestampedWindowStore("broadcastAggregate",
                Duration.ofMillis(window.size() + window.gracePeriodMs()),
                Duration.ofMillis(window.size()), false), Serdes.String(),
            CustomSerdes.broadcastStatsSerde()).withCachingEnabled(), "Aggregate")
        .addProcessor("AggregationFormatter", AggregationFormatter::new, "Aggregate")
        .addSink("Sink", "sink-topic", new StringSerializer(), new JsonSerializer<>(),
            "AggregationFormatter");

    streams = new KafkaStreams(builder, new StreamsConfig(props));
    streams.start();
  }

  @PreDestroy
  public void destroy() {
    if (Objects.nonNull(streams)) {
      streams.close();
    }
  }
}
