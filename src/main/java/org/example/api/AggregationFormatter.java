package org.example.api;

import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.Change;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AggregationFormatter implements Processor<Windowed<String>, Change<BroadcastStats>, String, BroadcastStats> {
  private ProcessorContext<String, BroadcastStats> context;

  @Override
  public void init(ProcessorContext<String, BroadcastStats> context) {
    this.context = context;
    Processor.super.init(context);
  }

  @Override
  public void process(Record<Windowed<String>, Change<BroadcastStats>> record) {
    Record<String, BroadcastStats> record1 =
        new Record<>(record.key().key(), record.value().newValue, record.timestamp(),
            record.headers());
    System.out.println("AggregationFormatter : "+ record1);
    context.forward(record1);
  }
}
