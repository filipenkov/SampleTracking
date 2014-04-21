--Find Samples that are deprecated or in a deprecated lot / collection
\loop /home/pedworth/workspace/SampleTrackingImport/sql/Find-deprecated.sql

--
-- Find samples that are only in the CTM
--

DROP TABLE #CTM_Only_Other;
CREATE TABLE #CTM_Only_Other
(Extent_id NUMERIC(20,0) NULL,
 jira_status VARCHAR(20) NULL,
 ctm_status VARCHAR(20) NULL,
 num_tasks NUMERIC(5,0) NULL,
 db VARCHAR(5) NULL
);

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

--In the CTM but not in JIRA (finished samples)
--INSERT INTO #Odd_Samples (Extent_id, data_base, jira_id, status, reason)
INSERT INTO #CTM_Only_Other (Extent_id, db, jira_status, ctm_status, num_tasks)

SELECT c.Extent_id          AS Extent_id, 
       "${db}"              AS db,
       "Other"              AS jira_status,
       c.status             AS ctm_status,
       SUM(tasks_per_ref) AS num_tasks
FROM (
  SELECT CONVERT(NUMERIC,value) AS Extent_id, 
         name AS status,
         ctm_reference_id AS ctm_reference_id
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
LEFT JOIN (
  SELECT ctm_reference_id AS ctm_reference_id,
         count(ctm_reference_id) AS tasks_per_ref
  FROM ${db}..ctm_task
  GROUP BY ctm_reference_id
) t
ON c.ctm_reference_id = t.ctm_reference_id
WHERE 
  a.value IS NULL 
  AND c.status NOT IN ("Deprecated","Unresolved","Published")
  AND c.Extent_id NOT IN (SELECT Extent_id FROM #Deprecated_Samples)
GROUP BY c.Extent_id;
\done

--view
SELECT count(Extent_id), count(num_tasks), ctm_status, db, sum(num_tasks)
FROM #CTM_Only_Other
GROUP BY db, ctm_status
ORDER BY db, ctm_status;

--copy
\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

    XXMERGE
        INTO ${db}..ExtentAttribute AS e -- e is the target table
        USING
            (SELECT e.Extent_id Extent_id,
              (SELECT ExtentAttributeType_id from ${db}..ExtentAttributeType where type="jira_status") ExtentAttributeType_id,
              jira_status value
             FROM #CTM_Only_Other j
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

