\loop /home/pedworth/workspace/SampleTrackingImport/sql/Find-deprecated.sql

CREATE TABLE #GLK_Only_Samples
(Extent_id NUMERIC(20,0) NULL,
 db VARCHAR(20) NULL,
 status VARCHAR(20) NULL
);



\for db in ebola eeev flumb fluutr gcv giv giv2 giv3 hadv hpiv1 hpiv3 hrv2 jev marburg mmp mpv msl norv piv rbl rsv rtv swiv vda veev vzv yfv

-- JIRA Entries in GLK but not in JIRA
INSERT INTO #GLK_Only_Samples (Extent_id, db, status)
SELECT s.Extent_id, "${db}", jira_status
FROM
    (SELECT Extent_id, value AS jira_status FROM ${db}..ExtentAttribute i WHERE ExtentAttributeType_id=1683) s
LEFT JOIN
    (SELECT Extent_id, value AS jira_id FROM ${db}..ExtentAttribute i WHERE ExtentAttributeType_id=1670) i
ON
    i.Extent_id = s.Extent_id
WHERE jira_id is NULL
      OR NOT jira_id LIKE "ST-%"
      AND s.Extent_id NOT IN (SELECT Extent_id FROM #Deprecated_Samples);

\done

--checks
--It should only have 3 values,Sample Published,Unresolved,Deprecated
SELECT Distinct(status) FROM #GLK_Only_Samples;

--This should be dropping, 11,640 as of April 24th 2013
SELECT Count(Extent_id) FROM #GLK_Only_Samples;

--Get it in a format to play with in Excel
SELECT db, status, count(Extent_id)
FROM #GLK_Only_Samples
GROUP BY db, status
ORDER By db, status
go -m csv > /tmp/samples
