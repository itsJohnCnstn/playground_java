package work;

import java.util.Map;

public class Null_values_quoted_in_map {

    //Original code
    public String getOrDefault(Map<String, Object> metadata, String key) {
        return String.valueOf(metadata.getOrDefault(key, null));
    }



}
