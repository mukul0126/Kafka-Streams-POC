package org.example.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MessageStatusState {
  private int statusState = 0 ;

  MessageStatusState(int statusState) {
    this.statusState = statusState;
  }


}
