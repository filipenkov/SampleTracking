tree grammar CqlEval;

options {
	tokenVocab=Cql;
	ASTLabelType=CommonTree;
}

@header {
package com.atlassian.crowd.cql.parser.antlr;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.builder.Combine;
import com.atlassian.crowd.search.query.entity.PropertyTypeService;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;

import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
}

@members {

private enum SimpleMatchMode { EQUALITY, GREATER_THAN, LESS_THAN };

private PropertyTypeService propertyTypeService;

public void setPropertyTypeService(final PropertyTypeService propertyTypeService)
{
    this.propertyTypeService = propertyTypeService;
}

private PropertyRestriction createPropertyRestriction(final String propertyName, final SimpleMatchMode simpleMatchMode, final String value)
{
    final Class clazz = propertyTypeService.getType(propertyName);
    if (clazz == null)
    {
        throw new IllegalArgumentException("Unknown type for property: " + propertyName);
    }
    final MatchMode mode = getMatchMode(simpleMatchMode, value);
    try
    {
        if (clazz.equals(String.class))
        {
            Property<String> property = new PropertyImpl<String>(propertyName, String.class);
            final String val = org.apache.commons.lang.StringUtils.strip(value, "*");
            return new TermRestriction<String>(property, mode, val);
        }
        else if (clazz.equals(Integer.class))
        {
            Property<Integer> property = new PropertyImpl<Integer>(propertyName, Integer.class);
            return new TermRestriction<Integer>(property, mode, Integer.valueOf(value));
        }
        else if (clazz.equals(Float.class))
        {
            Property<Float> property = new PropertyImpl<Float>(propertyName, Float.class);
            return new TermRestriction<Float>(property, mode, Float.valueOf(value));
        }
        else if (clazz.equals(Date.class))
        {
            Property<Date> property = new PropertyImpl<Date>(propertyName, Date.class);
            org.joda.time.format.DateTimeFormatter formatter = ISODateTimeFormat.dateOptionalTimeParser();
            return new TermRestriction<Date>(property, mode, formatter.parseDateTime(value).toDate());
        }
        else if (clazz.equals(Boolean.class))
        {
            Property<Boolean> property = new PropertyImpl<Boolean>(propertyName, Boolean.class);
            return new TermRestriction<Boolean>(property, mode, Boolean.valueOf(value));
        }
    }
    catch (ClassCastException e)
    {
        throw new IllegalArgumentException("Unexpected type: " + e.getMessage());
    }
    throw new IllegalArgumentException("Unknown value type: " + clazz.getName());
}

private static MatchMode getMatchMode(final SimpleMatchMode mode, final Object value)
{
    if (value == null)
    {
        throw new IllegalArgumentException("value cannot be null");
    }
    
    switch (mode)
    {
    case EQUALITY:
    	if (value.getClass().equals(String.class))
    	{
    	    String str = (String)value;
    	    if (org.apache.commons.lang.StringUtils.isNotBlank(str))
    	    {
    	        char firstChar = str.charAt(0);
    	        char lastChar = str.charAt(str.length() - 1);
    	        if (firstChar == '*' && lastChar == '*')
    	        {
                    return MatchMode.CONTAINS;
                }
                else if (lastChar == '*')
                {
                    return MatchMode.STARTS_WITH;
                }
            }
        }
        return MatchMode.EXACTLY_MATCHES;
    case GREATER_THAN:
        return MatchMode.GREATER_THAN;
    case LESS_THAN:
        return MatchMode.LESS_THAN;
    default:
        throw new IllegalArgumentException("Unknown mode: " + mode);
    }
}

private static String stripQuotes(final String str)
{
    return org.apache.commons.lang.StringUtils.strip(str, "\"\'");
}

} // close @members

getRestriction returns [SearchRestriction value]
	: a=restriction {$value = a;}
	;

restriction returns [SearchRestriction value]
@init {final List<SearchRestriction> reslist = new ArrayList<SearchRestriction>();}
	:	^(OR (res=restriction { reslist.add(res); })+) {$value = Combine.anyOf(reslist);}
	|	^(AND (res=restriction { reslist.add(res); })+) {$value = Combine.allOf(reslist);}
	|	res=propertyExpression {$value = res;}
	;

propertyExpression returns [SearchRestriction value]
	:	^(op=comparison_op key=termKey val=termValue) {$value = createPropertyRestriction(key, op, val);}
	;
	
termKey returns [String value]
	:	STRING {$value = $STRING.text;}
	|	QUOTE_STRING {$value = stripQuotes($QUOTE_STRING.text);}
	|	SQUOTE_STRING {$value = stripQuotes($SQUOTE_STRING.text);}
	;
	
termValue returns [String value]
	:	STRING {$value = $STRING.text;}
	|	QUOTE_STRING {$value = stripQuotes($QUOTE_STRING.text);}
	|	SQUOTE_STRING {$value = stripQuotes($SQUOTE_STRING.text);}
	;

comparison_op returns [SimpleMatchMode value]
	:	EQUALS {$value = SimpleMatchMode.EQUALITY;}
	|	LT {$value = SimpleMatchMode.LESS_THAN;}
	|	GT {$value = SimpleMatchMode.GREATER_THAN;}
	;
