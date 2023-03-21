package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

  @Autowired
  private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

  @GetMapping
  public Object getMetric() {
//    streamsBuilderFactoryBean.getTopology().
    streamsBuilderFactoryBean
        .getKafkaStreams()
        .metrics();
    System.out.println("FRsd");
    return null;
  }
}
