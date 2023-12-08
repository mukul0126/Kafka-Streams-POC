package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootApplication
public class Main {
    public Main() throws IOException {
    }

    public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
  FileInputStream inputStream = new FileInputStream("test.txt");
   FileInputStream c = null;
}