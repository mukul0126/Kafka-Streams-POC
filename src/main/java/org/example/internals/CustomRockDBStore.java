//package org.example.internals;
//
//import org.apache.kafka.streams.errors.ProcessorStateException;
//import org.apache.kafka.streams.state.internals.RocksDBStore;
//import org.rocksdb.ColumnFamilyDescriptor;
//import org.rocksdb.ColumnFamilyHandle;
//import org.rocksdb.ColumnFamilyOptions;
//import org.rocksdb.DBOptions;
//import org.rocksdb.RocksDB;
//import org.rocksdb.RocksDBException;
//import org.rocksdb.TtlDB;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//public class CustomRockDBStore extends RocksDBStore {
//
//
//  CustomRockDBStore(final String name,
//      final String metricsScope) {
//    super(name, "DB_FILE_DIR");
//  }
//
//  @Override
//  void openRocksDB(final DBOptions dbOptions,
//      final ColumnFamilyOptions columnFamilyOptions) {
//    final List<ColumnFamilyDescriptor> columnFamilyDescriptors
//        = Collections.singletonList(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, columnFamilyOptions));
//    final List<ColumnFamilyHandle> columnFamilies = new ArrayList<>(columnFamilyDescriptors.size());
//
//    try {
//      db = TtlDB.open(dbOptions, dbDir.getAbsolutePath(), columnFamilyDescriptors, columnFamilies,
//          Arrays.asList(30000),false);
//      dbAccessor = new SingleColumnFamilyAccessor(columnFamilies.get(0));
//    } catch (final RocksDBException e) {
//      throw new ProcessorStateException("Error opening store " + name + " at location " + dbDir.toString(), e);
//    }
//  }
//}
