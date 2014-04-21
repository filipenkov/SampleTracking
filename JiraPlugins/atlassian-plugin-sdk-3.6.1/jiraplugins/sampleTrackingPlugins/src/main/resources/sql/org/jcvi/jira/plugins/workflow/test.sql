/*the UPSERT  is done by creating a table containing the row to
//be inserted/updated and then merging that table with the
//table the row is to be inserted into*/
    MERGE
    /*
      MERGE is a relatively recent addition to SQL. It was added in the
      SQL 2003 spec and extended in the SQL 2008 spec.
         * <table>
         *     <hr><td>Database</td><td>Compatibility</td></hr>
         *     <tr><td>Sybase</td><td>SQL 2003</td></ts></tr>
         *     <tr><td>Oracle</td><td>SQL 2003<br/>
         *                            (originally it had its own incompatible syntax
                                       which should be avoided)</td></tr>
         *     <tr><td>DB2</td><td>SQL 2008</td></tr>
         *     <tr><td>SQL Server</td><td>SQL 2008</td></tr>
         *     <tr><td>MySQL</td><td>NONE<br/>
         *                           REPLACE could for this single row insert
         *     </br></td></tr>
         *     <tr><td>Postgress</td><td>NONE</td></tr>
         * </table>
     */
        INTO ExtentAttribute AS e -- e is the target table
        USING
            /*This is a derived table consisting of a single
            //entry containing the row to be UPSERTED
            //The Extent_id and value are constants but the
            //ExtentAttributeType_id is looked up to avoid
            //hard coding the number*/
            (SELECT CONVERT(NUMERIC,${customfield_10000}) Extent_id,
              ExtentAttributeType_id,
              ${new-status} value
             /*This table is included purely for the
             //ExtentAttributeType_id lookup*/
             FROM ExtentAttributeType
             WHERE type = 'jira_id') AS i -- i is the source table
    --The join criteria
        ON e.Extent_id = i.Extent_id AND
           e.ExtentAttributeType_id = i.ExtentAttributeType_id
    ---If a row from the source table matches a row from the target table
        WHEN MATCHED THEN
            /*The join keys of the row can't be changed, this
            leaves just the value to update*/
            UPDATE SET value=i.value
    --If a row from the source table has no equivalent row in the target table
        WHEN NOT MATCHED THEN
            /*As the row doesn't exist all fields must be set*/
            INSERT (  Extent_id,   ExtentAttributeType_id,   value)
            VALUES (i.Extent_id, i.ExtentAttributeType_id, i.value)
