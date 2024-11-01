package io.camunda.connector.csv.streamer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompositeMatcherStreamer extends DataRecordStreamer {

  List<Map<String, Object>> listMatchers = new ArrayList<>();

  public static CompositeMatcherStreamer getFromRecord(Map<String, Object> matcherData) {
    if (matcherData == null)
      return new CompositeMatcherStreamer();
    return new CompositeMatcherStreamer().addMatcher(matcherData);
  }

  public CompositeMatcherStreamer addMatcher(Map<String, Object> matcherData) {
    listMatchers.add(matcherData);
    return this;
  }

  public int getNumberOfMatchers() {
    return listMatchers.size();
  }

  @Override
  public boolean needDataRecordDecode() {
    return isMatcherActive();
  }

  @Override
  public boolean keepDataRecord(int lineNumber, Map<String, Object> dataRecord) {
    // Is one matcher match the record, then return true
    return !getMatchers(dataRecord).isEmpty();
  }

  public boolean isMatcherActive() {
    return !listMatchers.isEmpty();
  }

  /**
   * search in the list of matchers who match the record
   *
   * @param dataRecord record to search
   * @return list of matcher. null if nothing match
   */
  public List<Map<String, Object>> getMatchers(Map<String, Object> dataRecord) {
    return listMatchers.stream().filter(t -> match(t, dataRecord)).toList();
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
