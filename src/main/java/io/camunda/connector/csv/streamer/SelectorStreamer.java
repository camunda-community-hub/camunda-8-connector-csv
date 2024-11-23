package io.camunda.connector.csv.streamer;

import java.util.*;

public class SelectorStreamer extends DataRecordStreamer {

    List<Map<String, Object>> listMatchers = new ArrayList<>();
    Set<String> keyFields = Collections.emptySet();


    public SelectorStreamer addKeyFields(Set<String> keyFields) {
        this.keyFields = keyFields;
        return this;
    }

    /**
     * The matchers will work as a OR.
     *
     * @param matcherData add a matcher
     * @return the Streamer
     */
    public SelectorStreamer addMatcher(Map<String, Object> matcherData) {
        if (matcherData != null && !matcherData.isEmpty())
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
        return !searchMatch(dataRecord).isEmpty();
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
    public List<Map<String, Object>> searchMatch(Map<String, Object> dataRecord) {
        return listMatchers.stream().filter(t -> match(t, dataRecord)).toList();
    }

    /**
     * Search if the matcher match the record. The matcher can contain data not in the record: these data are not take into account
     *
     * @param matcher    data to search if they match
     * @param dataRecord record to check
     * @return true if the matcher match the data
     */
    private boolean match(Map<String, Object> matcher, Map<String, Object> dataRecord) {
        Set<String> listKeys = keyFields.isEmpty() ? matcher.keySet() : keyFields;

        for (String key : listKeys) {
            // two value can be null OR must be egals
            if (matcher.get(key) == null && dataRecord.get(key) == null)
                continue;
            if (matcher.get(key) == null)
                return false;
            if (!matcher.get(key).equals(dataRecord.getOrDefault(key, null)))
                return false;
        }
        return true;
    }

}
