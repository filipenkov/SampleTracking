package org.jcvi.jira.importer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Reads a CSV File and stores the information in a Hash of Hashes mapping:
 * <code>
 *   Hash&lt;first column,Hash&lt;column name, value&gt;&gt;
 * </code>
 * <ul>
 *     <li>Lines with a blank first column are ignored</li>
 *     <li>Lines with a leading # are ignored</li>
 *     <li>Fields are separated by tabs</li>
 *     <li>The column names are gained from the first non-comment line</li>
 *     <li>Blank values are not inserted into the Hash</li>
 *     <li>Any fields beyond those named in the header are ignored</li>
 *     <li>Lines with fewer columns than in the header as assumed to have
 *         blank values for the remaining fields</li>
 * </ul>
 */
public class CSVToHash {
    private final Map<String,Map<String,String>> mappedValues
            = new HashMap<String, Map<String, String>>();

    public CSVToHash(Reader file) throws IOException {
        BufferedReader input = new BufferedReader(file);
        boolean header = true;
        String line;
        List<String> fieldNames = new ArrayList<String>();
        while((line = input.readLine()) != null) {
            if(line.startsWith("#")) {
                continue; //comment
            }
            if(line.trim().length()==0) {
                continue; //blank
            }
            //split into fields
            String[] fields = line.split("\t");
            if (header) {
                //Case 1: header
                for(String field: fields) {
                    fieldNames.add(field.trim());
                }
                header = false;
            } else {
                //Case 2: body
                if (fields.length < 1 || fields[0].trim().isEmpty()) {
                    continue;
                }
                Map<String,String>lineMap = new HashMap<String,String>();
                Iterator<String>fieldName = fieldNames.iterator();
                for(String field: fields) {
                    if (!fieldName.hasNext()) {
                        //we have read all of the named fields
                        break;
                    }
                    String value=field.trim();
                    String key = fieldName.next();
                    if (!value.isEmpty()) {
                        lineMap.put(key, value);
                    }
                }
                mappedValues.put(fields[0].trim(),lineMap);
            }
        }
        try {
            input.close();
        } catch (IOException ignored) {}
        try {
            file.close();
        } catch (IOException ignored) {}
    }

    public Map<String,String>getValuesFor(String key) {
        return mappedValues.get(key);
    }

    public Set<String>getKeys() {
        return mappedValues.keySet();
    }
}
