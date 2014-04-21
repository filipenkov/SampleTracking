--Find Samples that are deprecated or in a deprecated lot / collection
\loop /home/pedworth/workspace/SampleTrackingImport/sql/Find-deprecated.sql

--
-- Find samples that are only in the CTM
--

DROP TABLE #CTM_Only_Finished
CREATE TABLE #CTM_Only_Finished
(Extent_id NUMERIC(20,0) NULL,
 ctm_status VARCHAR(20) NULL,
 db VARCHAR(5) NULL
);

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

--In the CTM but not in JIRA (finished samples)
--INSERT INTO #Odd_Samples (Extent_id, data_base, jira_id, status, reason)
INSERT INTO #CTM_Only_Finished (Extent_id, db, ctm_status)

SELECT c.Extent_id          AS Extent_id, 
       "${db}"              AS db,
       c.status             AS ctm_status
FROM (
  SELECT CONVERT(NUMERIC,value) AS Extent_id, 
         name AS status
  FROM ${db}..ctm_reference r
  JOIN ${db}..ctm_reference_status s
  ON r.ctm_reference_status_id = s.ctm_reference_status_id
  ) c
LEFT JOIN (
  SELECT Extent_id, value 
  FROM ${db}..ExtentAttribute
  WHERE ExtentAttributeType_id=1670
  ) a
ON c.Extent_id=a.Extent_id
WHERE 
  a.value IS NULL 
  AND c.status IN ("Deprecated","Unresolved","Published")
  AND c.Extent_id NOT IN (SELECT Extent_id FROM #Deprecated_Samples);

\done

--
-- Convert the CTM status names to JIRA status names
--

DROP TABLE #Status_Map;
CREATE TABLE #Status_Map
(ctm_status VARCHAR(30),
jira_status VARCHAR(30));

INSERT INTO #Status_Map (ctm_status, jira_status) values ('Published', 'Sample Published');
--INSERT INTO #Status_Map (ctm_status, jira_status) values ('Received', 'Received Sample');
--INSERT INTO #Status_Map (ctm_status, jira_status) values ('Submitted to Genbank', 'Submitted');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Deprecated', 'Deprecated');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Unresolved', 'Unresolved');
--INSERT INTO #Status_Map (ctm_status, jira_status) values ('Validation', 'Validate');
--INSERT INTO #Status_Map (ctm_status, jira_status) values ('Edit TT','Close Sample');
--INSERT INTO #Status_Map (ctm_status, jira_status) values ('Edit NF','Close Sample');
--INSERT INTO #Status_Map (ctm_status, jira_status) values ('Closure TT','Close Sample');
--INSERT INTO #Status_Map (ctm_status, jira_status) values ('Closure NF','Close Sample');
--INSERT INTO #Status_Map (ctm_status, jira_status) values ('Initial Manual Edit','Close Sample');
--INSERT INTO #Status_Map (ctm_status, jira_status) values ('NextGen Validation','Validate');

DROP TABLE #Jira_Data_DB;
CREATE TABLE #Jira_Data_DB
(Extent_id NUMERIC(20),
 jira_status VARCHAR(40) NULL,
 db VARCHAR(5)
);

INSERT INTO #Jira_Data_DB (Extent_id, jira_status, db)
SELECT Extent_id, jira_status, db 
FROM #CTM_Only_Finished c
JOIN #Status_Map s
ON c.ctm_status = s.ctm_status;

--Extra check, this should be the empty set for all the databases.
\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb
SELECT value 
FROM (
  SELECT Extent_id, value from ${db}..ExtentAttribute WHERE ExtentAttributeType_id=1670) a
JOIN #Jira_Data_DB j
ON a.Extent_id = j.Extent_id;
\done


\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

    XXMERGE
        INTO ${db}..ExtentAttribute AS e -- e is the target table
        USING
            (SELECT e.Extent_id Extent_id,
              (SELECT ExtentAttributeType_id from ${db}..ExtentAttributeType where type="jira_status") ExtentAttributeType_id,
              jira_status value
             FROM #Jira_Data_DB j
             JOIN ${db}..Extent e
             ON e.Extent_id = j.Extent_id
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

