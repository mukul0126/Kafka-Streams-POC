package org.example.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BroadcastStats {
  String broadcastId;
  String userId;
  Integer sent = 0;
  Integer read =0;
  Integer failed =0;
  Integer delivered=0;
}
