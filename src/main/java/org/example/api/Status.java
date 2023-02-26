package org.example.api;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum Status {
  SENT(0),
  DELIVERED(1),
  READ(2),
  FAILED(3);

  Integer bit;

  Status(int bit) {
    this.bit = bit;
  }

  private static final Map<Integer, Status> MAP =
      Arrays.stream(values())
          .collect(Collectors.toMap(Status::getBit, Function.identity()));

  public static Status getStatus(int bit) {
    return MAP.computeIfAbsent(
        bit,
        (key) -> {
          throw new IllegalArgumentException("Unknown state: " + bit);
        });
  }

}
