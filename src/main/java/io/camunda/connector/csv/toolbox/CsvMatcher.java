package io.camunda.connector.csv.toolbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvMatcher {

  List<Map<String, Object>> listMachers = new ArrayList<>();

  public static CsvMatcher getFromRecord(Map<String, Object> matcher) {
    if (matcher == null)
      return new CsvMatcher();
    return new CsvMatcher().addMatcher(matcher);
  }

  public CsvMatcher addMatcher(Map<String, Object> matcher) {
    listMachers.add(matcher);
    return this;
  }

  public boolean isMatcherActive() {
    return !listMachers.isEmpty();
  }

  /**
   * search in the list of matchers who match the record
   *
   * @param dataRecord record to search
   * @return list of matcher. null if nothing match
   */
  public List<Map<String, Object>> getMatchers(Map<String, Object> dataRecord) {
    return listMachers.stream().filter(t -> match(t, dataRecord)).toList();
  }

  private boolean match(Map<String, Object> matcher, Map<String, Object> dataRecord) {
    for (Map.Entry<String, Object> entry : matcher.entrySet()) {
      if (entry.getValue() == null && dataRecord.get(entry.getKey()) != null)
        return false;
      if (!entry.getValue().equals(dataRecord.getOrDefault(entry.getKey(), null)))
        return false;
    }
    return true;
  }

}
