package org.example;

import java.util.List;


public interface MapReduceInterface {

    List<KeyValue> map(String fileName, String content);

    String reduce(String key, List<String> values);
}

