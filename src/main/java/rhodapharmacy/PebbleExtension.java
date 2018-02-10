package rhodapharmacy;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PebbleExtension extends AbstractExtension {
    @Override
    public Map<String, Filter> getFilters() {
        Filter currencyFilter = new Filter() {
            @Override
            public Object apply(Object input, Map<String, Object> args) {
                if(input == null) {
                    return null;
                }
                Long value = ((Number)input).longValue();
                Double doubleValue = value.doubleValue() / 100d;
                return String.format("%.2f", doubleValue);
            }

            @Override
            public List<String> getArgumentNames() {
                return Collections.emptyList();
            }
        };
        Map<String, Filter> filters = new HashMap<>();
        filters.put("currencyformat", currencyFilter);
        return filters;
    }
}
