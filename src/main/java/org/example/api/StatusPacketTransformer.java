package org.example.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class StatusPacketTransformer implements
    Transformer<String, String, KeyValue<String, BroadcastStats>> {

  private KeyValueStore<String, Integer> kvStore;

  private RocksDBTTLStore<MessageStatusState> store;

  private ObjectMapper mapper = new ObjectMapper();

  private ObjectReader reader;


  @Override
  public void init(org.apache.kafka.streams.processor.ProcessorContext context) {
    this.reader = mapper.readerFor(BroadcastStatusPacket.class);
    //change it to get store from store provider
    this.store = new RocksDBTTLStore<>("status-store", 10, MessageStatusState.class);
  }

  @Override
  public KeyValue<String, BroadcastStats> transform(String key, String value) {
    try {
      BroadcastStatusPacket broadcastStatusPacket = reader.readValue(value);
      MessageStatusState state = this.store.get(broadcastStatusPacket.getMid());
      if (state == null) {
        state = new MessageStatusState(0);
      }
      BroadcastStats stats = processPacket(state, broadcastStatusPacket.getStatus());
      stats.setBroadcastId(broadcastStatusPacket.broadcastId);
      this.store.put(broadcastStatusPacket.getMid(), state);
      return new KeyValue<>(broadcastStatusPacket.broadcastId, stats);
    } catch (Exception e) {
      log.error("error while parsing", e);
    }
    return null;
  }

  @Override
  public void close() {
    this.store.close();
  }

  private BroadcastStats processPacket(MessageStatusState state, Status status) {
    BroadcastStats stats = new BroadcastStats();
    int bitState = state.getStatusState();
    if (status == Status.FAILED) {
      int statusBit = status.getBit();
      //todo if again failed packet  ---- done
      if((bitState & (1 << statusBit))!= 0) {
        return stats;
      }
      for (int i = statusBit - 1; i >= 0; i--) {
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
      stats.setFailed(1);
      state.setStatusState(1 << status.getBit());
    } else {
      if ((bitState & (1 << Status.FAILED.getBit())) != 0) {
        //todo: set failed bit off
        bitState = (bitState & ~(1 << Status.FAILED.getBit()));
        //make sure to send -1 for failed --- done
        stats.setFailed(-1);
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
