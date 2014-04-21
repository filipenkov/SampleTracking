package org.jcvi.jira.plugins.utils.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pre-parses an SQL Query to simulate named parameters instead of using
 * just JDBCs parameter indexes. Parameters are marked with ${parameter name}
 * instead of '?'.
 * e.g.
 * Standard JDBC SQL<code>"SELECT column1, column2 FROM table1 where column3 = ? and column4 <= ?"</code>
 * NamedParameterSQL<code>"SELECT column1, column2 FROM table1 where column3 = ${column3} and column4 <= ${column4}"</code>
 *
 * A lookup is also provided to map the parameter names into indexes for use with
 * the prepared statement.
 * Standard JDBC code
 * <code>
 * PreparedStatement ps = new PreparedStatement(
 *  "SELECT column1, column2 FROM table1 where column3 = ? and column4 <= ?");
 * ps.setString(1,column3Value);
 * ps.setLong(2,column4Value);
 * </code>
 * NamedParameterSQL code<code>
 * NamedParameterSQL namedParameterSQL = new NamedParameterSQL(
 *  "SELECT column1, column2 FROM table1 where column3 = ${column3} and column4 <= ${column4}"
 * PreparedStatement ps = new PreparedStatement(namedParameterSQL.getPreparedStatementSQL());
 * ps.setString(namedParameterSQL.getParameterPosition("column3"),column3Value);
 * ps.setLong(namedParameterSQL.getParameterPosition("column4"),column4Value);
 * </code>
 *
 * Notes:
 * <p>All comments and new-line characters are stripped before the parameters
 * are replaced. ${xxx} in a comment should be ignored.</p>
 * <p>The parameters are replaced without checking if they are in a quoted or
 * littoral section (',",[ ])</p>
 * <p>Currently there is no way to specify the type that should be used with
 * a parameter. The code setting the parameters must select the correct
 * setXXXX method.</p>
 * <p>Currently each parameter should have a unique name. If two share a name
 * then only the index of the second will be returned by getParameterPosition, however
 * getFromIndex will function correctly.</p>
 */
public class NamedParameterSQL {
    //find the named parameters ${XXX}
    private static final Pattern locateParameters  = Pattern.compile("[$][{]([^}]+)[}]");
    private static final Pattern endOfLineComments = Pattern.compile("--.*(\r?\n|\r)");
    // *? in java reg-ex is a reluctant match, this is important when there
    // are multiple comment blocks
    // /* comment */ sql /* more comments */
    // .* would match from the first /* to the last */ removing the sql in-between
    private static final Pattern commentBlock = Pattern.compile("/[*].*?[*]/");
    //todo: ignore pattern inside quotes, comments and '[' ']'
    //inside quotes a single quote is represented by using two single quotes in
    //a row '' (mysql uses /', but as it can also use '' it's not worth supporting)
    //There is no escape sequence for double quotes. If they are used it should
    //be within a single quote section.
    //Handling these cases makes things more complex as the SQL must be parsed
    //using multiple states.

    //todo: strip comments
    //sql -- comment
    // /* multi-line comment */

    private final String originalSQL;
    private final String preparedStatementSQL;
    private final Map<String,Integer> parameterToIndex;
    private final List<String> indexToParameter;

    public NamedParameterSQL(String sql) {
        originalSQL = sql;

        //strip comments
        String noLineEndComments = endOfLineComments.matcher(sql).replaceAll("");
        String noNewLines = noLineEndComments.replace('\n',' ').replace('\r',' ');
        String noComments = commentBlock.matcher(noNewLines).replaceAll("");

        parameterToIndex = new HashMap<String,Integer>();
        indexToParameter = new ArrayList<String>();
        StringBuffer psSQL = new StringBuffer();
        Matcher parameters = locateParameters.matcher(noComments);
        while (parameters.find()) {
            indexToParameter.add(parameters.group(1));
            parameterToIndex.put(parameters.group(1),indexToParameter.size());
            parameters.appendReplacement(psSQL, "?");
        }
        parameters.appendTail(psSQL);

        preparedStatementSQL = psSQL.toString();
    }

    /**
     * This is the SQL converted into the format used by the normal
     * JDBC PreparedStatement.
     * @return The SQL with all of the named parameters replaced by '?'s
     */
    public String getPreparedStatementSQL() {
        return preparedStatementSQL;
    }

    /**
     *
     * @param parameterName The name that was used in the SQL passed when
     *                      this object was created
     * @return  The index to use for the named parameter, counting from 1.
     * @throws SQLException is the parameter was not found.
     */
    public int getParameterPosition(String parameterName) throws SQLException {
        Integer position = parameterToIndex.get(parameterName);
        if (position != null) {
            return position;
        }
        throw new SQLException("Could not find parameter '"+parameterName+
                "' in '"+originalSQL);
    }

    /**
     * Looks up the name of the parameter from its index in the
     * PreparedStatement.
     * @param index The position of the parameter, counting from 1.
     * @return The name of the parameter in the SQL used to create this
     * NamedParameterSQL object
     * @throws SQLException If the index is &lt;= 0 or &gt; the number of
     * parameters in the SQL
     */
    public String getParameterAt(int index) throws SQLException {
        if (index <= 0 || indexToParameter.size() < index) {
            throw new SQLException("Invalid index '"+index+
                    "' the index must be in the range 1 to "+
                    indexToParameter.size()+
                    "(number of parameters in the SQL)");
        }
        return indexToParameter.get(index-1);
    }

    //todo: local type conversion
    //extend the format to ${variable:type} see java.sql.Types for a list of the
    //types jdbc can use. We will probably have to use just a sub-set, NUMBER,
    //DATE, VARCHAR the type of value that we get from the customfield will have
    //to be checked and possibly converted.
//    public int getParameterType(String parameterName) throws SQLException {
//        return getParameterType(getParameterPosition(parameterName));
//    }
//
//    public int getParameterType(int parameterPosition) throws SQLException {
//        return 0;
//    }
//
//    public Object convertParameterValue(String parameterName, Object value)
//            throws SQLException {
//        return convertParameterValue(getParameterPosition(parameterName),value);
//    }
//
//    public Object convertParameterValue(int parameterPosition, Object value)
//            throws SQLException {
//        return null;
//    }

    public int getNumberOfParameters() {
        return indexToParameter.size();
    }
}
