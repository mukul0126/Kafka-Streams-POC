package org.example.internals;


import org.example.api.RocksDBTTLStore;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Test {
  public static void main(String[] args)
      throws IOException, RocksDBException, InterruptedException {
    CountDownLatch c = new CountDownLatch(1);
    String s = getAlphaNumericString(1024 * 100);
    RocksDBTTLStore<String> db = new RocksDBTTLStore<>("test", 10, String.class);
    db.put("mukul", "gupta");
    System.out.println(db.get("mukul"));
    for (int i = 0; i < 100000; i++) {
      String k = "mukul" + i;
      db.put(k, s);
    }


    System.out.println(db.get("mukul"));
//    c.await();
//    db.close();
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
