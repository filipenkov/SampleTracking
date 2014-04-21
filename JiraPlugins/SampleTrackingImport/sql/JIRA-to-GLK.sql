DROP TABLE #Jira_Data;
CREATE TABLE #Jira_Data
(jira_id VARCHAR(20) NULL,
 jira_status VARCHAR(40) NULL
);

DROP TABLE #Jira_Missmatches;
CREATE TABLE #Jira_Missmatches
(jira_id VARCHAR(20) NULL,
 db VARCHAR(5),
 jira_status VARCHAR(40) NULL,
 glk_status VARCHAR(40)
);

\loop "/export/jira-status.sql"

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

SELECT "JIRA Entries in both glk and jira", count(jira.jira_id)
FROM
  #Jira_Data jira
JOIN 
  (SELECT jira_id, jira_status
  FROM
    (SELECT Extent_id, value AS jira_id FROM ${db}..ExtentAttribute i WHERE ExtentAttributeType_id=1670) i
  JOIN
    (SELECT Extent_id, value AS jira_status FROM ${db}..ExtentAttribute i WHERE ExtentAttributeType_id=1683) s
  ON 
    i.Extent_id = s.Extent_id) db
ON
  jira.jira_id = db.jira_id;

INSERT INTO #Jira_Missmatches (jira_id, db, jira_status, glk_status)
SELECT jira.jira_id, "${db}" AS db, jira.jira_status, db.jira_status AS glk_status
FROM
  #Jira_Data jira
JOIN 
  (SELECT jira_id, jira_status
  FROM
    (SELECT Extent_id, value AS jira_id FROM ${db}..ExtentAttribute i WHERE ExtentAttributeType_id=1670) i
  JOIN
    (SELECT Extent_id, value AS jira_status FROM ${db}..ExtentAttribute i WHERE ExtentAttributeType_id=1683) s
  ON 
    i.Extent_id = s.Extent_id) db
ON
  jira.jira_id = db.jira_id
WHERE jira.jira_status != db.jira_status;

\done

SELECT db, count(jira_id) FROM #Jira_Missmatches GROUP BY db;
SELECT jira_id FROM #Jira_Missmatches WHERE db=""; 

---------------------------------------------------------------------------------------------
DROP TABLE #Jira_Data_DB;
CREATE TABLE #Jira_Data_DB
(Extent_id NUMERIC(20),
 jira_status VARCHAR(40) NULL,
 db VARCHAR(5)
);

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb
INSERT INTO #Jira_Data_DB
SELECT Extent_id, jira_status, "${db}" AS db
FROM #Jira_Data j
JOIN ${db}..ExtentAttribute a
ON j.jira_id = a.value;
\done

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

    XXMERGE
        INTO ${db}..ExtentAttribute AS e -- e is the target table
        USING
            (SELECT Extent_id Extent_id,
              (SELECT ExtentAttributeType_id from ${db}..ExtentAttributeType where type="jira_status") ExtentAttributeType_id,
              jira_status value
             FROM #Jira_Data_DB
             WHERE db = "${db}") AS i
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
            VALUES (i.Extent_id, i.ExtentAttributeType_id, i.value);
\done

--For testing
--SELECT i.value, s.value 
--FROM (
--  SELECT Extent_id, value FROM ${db}..ExtentAttribute i WHERE ExtentAttributeType_id=1670) i
--JOIN (
--  SELECT Extent_id, value FROM ${db}..ExtentAttribute i WHERE ExtentAttributeType_id=1683) s
--ON
--i.Extent_id = s.Extent_id;


