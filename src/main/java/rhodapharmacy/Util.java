package rhodapharmacy;

import java.util.LinkedHashMap;
import java.util.Map;

public class Util {

    public static Map<String, Object> mapOf(Object...kv) {
        Map<String, Object> ans = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; ) ans.put((String)kv[i++], kv[i++]);
        return ans;
    }

}
