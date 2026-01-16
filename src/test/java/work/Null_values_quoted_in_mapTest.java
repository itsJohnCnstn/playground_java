package work;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class Null_values_quoted_in_mapTest {

    //Sut
    private final Null_values_quoted_in_map nullValuesQuotedInMap = new Null_values_quoted_in_map();

    @Test
    void shouldReturnStringValueAndConvertNullToStringNull() {
        //Given
        String key1 = "key1";
        String key2 = "key2";
        Map<String, Object> map = new HashMap<>();
        String rightValue = "value";
        map.put(key1, rightValue);
        map.put(key2, null);

        //When
        String stringValue = nullValuesQuotedInMap.getOrDefault(map, key1);
        String nullValue = nullValuesQuotedInMap.getOrDefault(map, key2);

        //Then
        assertThat(stringValue).isEqualTo(rightValue);

        assertThat(nullValue).isNotEqualTo(null);
        assertThat(nullValue).isEqualTo("null");
    }

}