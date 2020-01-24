package com.example.fn;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import com.fnproject.fn.api.RuntimeContext;

public class HelloFunction {

    public String handleRequest(String input, RuntimeContext ctx) {
        Map<String, String> environmentMap = ctx.getConfiguration();
        SortedMap<String, String> sortedEnvMap = new TreeMap<>(environmentMap);
        Set<String> keySet = sortedEnvMap.keySet();
        
        String outStr  = "---\n";
        
        for (String key : keySet) {
        	String value = environmentMap.get(key);
        	outStr = outStr + ( key + ": " + value + "\n");
        }
        
        return outStr;
    }

}