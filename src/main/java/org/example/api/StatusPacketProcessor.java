package org.example.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class StatusPacketProcessor implements Processor<String, String, String, BroadcastStats> {
  private ProcessorContext<String, BroadcastStats> context;

  private KeyValueStore<String, Integer> kvStore;

  private RocksDBTTLStore<MessageStatusState> store;

  private ObjectMapper mapper = new ObjectMapper();

  private ObjectReader reader;


  @Override
  public void init(ProcessorContext context) {
    this.reader = mapper.readerFor(BroadcastStatusPacket.class);
    this.context = context;
    //change it to get store from store provider
    this.store = new RocksDBTTLStore<>("/Users/mukulgupta/Desktop/rocksdb_ttl_store/state-status-store", 10, MessageStatusState.class);

  }

  @Override
  public void process(Record<String, String> record) {
    Object val = record.value();
    try {
      BroadcastStatusPacket broadcastStatusPacket = reader.readValue(val.toString());
      MessageStatusState state = this.store.get(broadcastStatusPacket.getMid());
      if (state == null) {
        state = new MessageStatusState(0);
      }
      BroadcastStats stats = processPacket(state, broadcastStatusPacket.getStatus());
      stats.setBroadcastId(broadcastStatusPacket.broadcastId);
      this.store.put(broadcastStatusPacket.getMid(), state);
      Record<String, BroadcastStats> record1 =
          new Record<>(broadcastStatusPacket.broadcastId, stats, record.timestamp(),
              record.headers());
      System.out.println("StatusPacketProcessor : " + record1);
      context.forward(record1);
    } catch (Exception e) {
      log.error("error while parsing", e);
    }

  }

  @Override
  public void close() {
    this.store.close();
  }

  private BroadcastStats processPacket(MessageStatusState state, Status status) {
    BroadcastStats stats = new BroadcastStats();
    int bitState = state.getStatusState();
    if (status == Status.FAILED) {
      //todo if again failed packet
      for (int i = status.getBit() - 1; i >= 0; i--) {
        if ((bitState & (1 << i)) != 0) {
          Status st = Status.getStatus(i);
          switch (st) {
            case SENT:
              stats.setSent(-1);
              break;
            case DELIVERED:
              stats.setDelivered(-1);
              break;
            case READ:
              stats.setRead(-1);
              break;
            default:
              break;
          }
        }
      }
      state.setStatusState(1 << status.getBit());
    } else {
      if ((bitState & (1 << Status.FAILED.getBit())) != 0) {
        //todo: set failed bit off
        //make sure to send -1 for failed
      }
      for (int i = status.getBit(); i >= 0; i--) {
        if ((bitState & (1 << i)) == 0) {
          //marking that bit on
          bitState = bitState | (1 << i);
          Status st = Status.getStatus(i);
          switch (st) {
            case SENT:
              stats.setSent(1);
              break;
            case DELIVERED:
              stats.setDelivered(1);
              break;
            case READ:
              stats.setRead(1);
              break;
          }
        }
      }
      state.setStatusState(bitState);
    }

    return stats;
  }

}
