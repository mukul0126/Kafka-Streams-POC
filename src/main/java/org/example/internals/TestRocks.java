package org.example.internals;


import org.example.api.MessageStatusState;
import org.example.api.RocksDBTTLStore;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class TestRocks {
  public static void main(String[] args)
      throws IOException, RocksDBException, InterruptedException {
    CountDownLatch c = new CountDownLatch(1);
    String s = getAlphaNumericString(1024 * 100);
//    RocksDBTTLStore<String> db = new RocksDBTTLStore<>("test", 1, String.class);

    RocksDBTTLStore<MessageStatusState> db = new RocksDBTTLStore<>("state-status-store", 172800, MessageStatusState.class);


    MessageStatusState se = new MessageStatusState();
    se.setStatusState(2);
    db.put("muk1", se);
    System.out.println(db.get("state_51_user20007"));
    db.close();
  }


  static String getAlphaNumericString(int n) {
    String AlphaNumericString =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";
    StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      int index = (int) (AlphaNumericString.length() * Math.random());
      sb.append(AlphaNumericString.charAt(index));
    }
    return sb.toString();
  }
}
