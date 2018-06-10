package web.util;

import java.util.HashMap;

public final class StringMap {
  private HashMap<String, String> data;

  public StringMap() {
    this(new HashMap<String, String>());
  }

  public StringMap(HashMap<String, String> data) {
    this.data = data;
  }

  public String get(String key) {
    return data.get(key);
  }

  public String get(String key, String defaultValue) {
    return data.getOrDefault(key, defaultValue);
  }

  public StringMap put(String key, String value) {
    data.put(key, value);
    return this;
  }

  public boolean contains(String key) {
    return data.containsKey(key);
  }

  public boolean isEmpty() {
    return data.isEmpty();
  }
}
