package org.example.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.TtlDB;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class RocksDBTTLStore<V> {

  //  @Value("${rocks.db.dir:/Users/mukulgupta/Desktop}")
  private String rocksDbTTlStorePath = "/Users/mukulgupta/Desktop/rocksdb_ttl_store";

  private static final long BLOCK_CACHE_SIZE = 50 * 1024 * 1024L;
  private static final long BLOCK_SIZE = 4096L;

  File dbDir;

  private TtlDB db;

  private Cache cache;

  private BloomFilter filter;

  @Getter
  private String name;

  @Getter
  private Integer ttl;

  private ObjectMapper mapper = new ObjectMapper();

  private ObjectWriter writer;
  private ObjectReader reader;

  private Class<V> clazz;

//  WriteOptions writeOptions = new WriteOptions();
//  ReadOptions readOptions = new ReadOptions();
  public RocksDBTTLStore(String name, Integer ttl, Class clazz) {
    this.name = name;
    this.ttl = ttl;
    this.clazz = clazz;
    this.reader = mapper.readerFor(this.clazz);
    this.writer = mapper.writerFor(this.clazz);
    openDB(name, ttl);
  }

  private void openDB(String name, Integer ttl) {

    final Options options = new Options();

//    final BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
//    cache = new LRUCache(BLOCK_CACHE_SIZE);
//    tableConfig.setBlockCache(cache);
//    tableConfig.setBlockSize(BLOCK_SIZE);
//    filter = new BloomFilter();
//    tableConfig.setFilterPolicy(filter);
//
//    options.setTableFormatConfig(tableConfig);
//    options.optimizeFiltersForHits();
//    writeOptions.setDisableWAL(false);

//    readOptions.setVerifyChecksums(false);
    options.setCreateIfMissing(true);
    options.setErrorIfExists(false);
    options.setInfoLogLevel(InfoLogLevel.ERROR_LEVEL);

//    options.setIncreaseParallelism(Math.max(Runtime.getRuntime().availableProcessors(), 2));

    dbDir = new File(rocksDbTTlStorePath, name);

    try {
      Files.createDirectories(dbDir.getParentFile().toPath());
      Files.createDirectories(dbDir.getAbsoluteFile().toPath());
    } catch (IOException fatal) {
      throw new RuntimeException(fatal);
    }

    try {
      db = TtlDB.open(options, dbDir.getAbsolutePath(), ttl, false);
    } catch (final RocksDBException e) {
      throw new RuntimeException(
          "Error opening store " + name + " at location " + dbDir.toString(), e);
    }
  }


  public V get(String key) throws RocksDBException, IOException {
    if(key == null) {
      throw new IllegalArgumentException();
    }
    byte[] res = db.get(key.getBytes(StandardCharsets.UTF_8));
    return res == null ? null : (V) reader.readValue(res);
  }

  public void put(final String key, final V value) throws JsonProcessingException {
    if(key == null) {
      throw new IllegalArgumentException();
    }
    if (value == null) {
      try {
        db.delete(key.getBytes(StandardCharsets.UTF_8));
      } catch (final RocksDBException e) {
        throw new RuntimeException("Error while removing key from store " + name, e);
      }
    } else {
      try {
        db.put(key.getBytes(StandardCharsets.UTF_8), writer.writeValueAsBytes(value));
      } catch (final RocksDBException e) {
        throw new RuntimeException("Error while putting key/value into store " + name, e);
      }
    }

  }

  public void close() {
    db.close();
  }

}

