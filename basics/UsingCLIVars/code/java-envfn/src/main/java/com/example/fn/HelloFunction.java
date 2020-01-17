package com.example.fn;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class HelloFunction {

    public String handleRequest(String input) {
        Map<String, String> environmentMap = System.getenv();
        SortedMap<String, String> sortedEnvMap = new TreeMap<>(environmentMap);
        Set<String> keySet = sortedEnvMap.keySet();
        
        String outStr  = "---";
        
        for (String key : keySet) {
        	String value = environmentMap.get(key);
        	outStr = outStr + ("[" + key + "] " + value + "\n");
        }
        
        return outStr;
    }

}