package com.atlassian.gadgets.spec;

/**
 * Represents the data type of a user preference. The data type to use for a particular user preference is defined in
 * the gadget spec.
 */
public enum DataType 
{
    STRING, HIDDEN, BOOL, ENUM, LIST, NUMBER;

    /**
     * Parses a data type from the input string.
     *
     * @param value
     * @return The data type of the given value.
     */
    public static DataType parse(String value) {
      for (DataType type : DataType.values()) {
        if (type.toString().compareToIgnoreCase(value) == 0) {
          return type;
        }
      }
      return STRING;
    }
}
