CREATE TABLE #Totals
(data_base VARCHAR(20) NULL,
 stat VARCHAR(20) NULL,
 total NUMERIC(20)
);

CREATE TABLE #Odd_Samples
(Extent_id NUMERIC(20,0) NULL,
 data_base VARCHAR(20) NULL,
 jira_id VARCHAR(20) NULL,
 status VARCHAR(20) NULL,
 reason VARCHAR(40) NULL
);

CREATE TABLE #CTM_Only_Finished
(Extent_id NUMERIC(20,0) NULL,
 data_base VARCHAR(20) NULL,
 status VARCHAR(20) NULL
);


\func CTM-Totals
INSERT INTO #Totals
SELECT "${db}" AS data_base,
       "CTM-${1}" AS stat,
       Count(c.Extent_id) AS total
FROM   
  (SELECT CONVERT(NUMERIC,value) AS Extent_id, 
         name AS status
  FROM ${db}..ctm_reference r
  JOIN ${db}..ctm_reference_status s
  ON r.ctm_reference_status_id = s.ctm_reference_status_id
  ) c
WHERE 
c.status = "${1}";
 
\done

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb
--In the CTM but not in JIRA (active samples)
INSERT INTO #Odd_Samples (Extent_id, data_base, jira_id, status, reason)
SELECT c.Extent_id          AS Extent_id, 
       "${db}"              AS data_base,
       NULL                 AS jira_id, 
       c.status             AS status, 
       "In CTM not in JIRA (active samples)" AS reason
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
WHERE a.value IS NULL AND
c.status NOT IN ("Deprecated","Unresolved","Published");

--In the CTM but not in JIRA (finished samples)
--INSERT INTO #Odd_Samples (Extent_id, data_base, jira_id, status, reason)
INSERT INTO #CTM_Only_Finished (Extent_id, data_base, status)

SELECT c.Extent_id          AS Extent_id, 
       "${db}"              AS data_base,
--       NULL                 AS jira_id, 
       c.status             AS status
--       ,"In CTM not in JIRA (finished samples)" AS reason
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
WHERE a.value IS NULL AND
c.status IN ("Deprecated","Unresolved","Published","Draft Submission");

--Exists in the CTM but not the GLK
INSERT INTO #Odd_Samples (Extent_id, data_base, jira_id, status, reason)
SELECT ctm.Extent_id AS Extent_id,
       "${db}"     AS data_base,
       NULL        AS jira_id,
       ctm.status  AS status,
       "Exists in the CTM but not the GLK" AS reason
FROM 
 (SELECT CONVERT(NUMERIC,value) AS Extent_id,
         b.name AS status
  FROM ${db}..ctm_reference a
  JOIN ${db}..ctm_reference_status b
  ON a.ctm_reference_status_id = b.ctm_reference_status_id
) ctm
LEFT JOIN ${db}..Extent s
ON s.Extent_id = ctm.Extent_id
WHERE s.Extent_id IS NULL;

--Find Samples that are deprecated or in a deprecated lot / collection
CREATE TABLE #Deprecated
(Extent_id NUMERIC(20,0));

INSERT INTO #Deprecated (Extent_id)
SELECT Extent_id FROM ${db}..ExtentAttribute WHERE ExtentAttributeType_id=1616;

CREATE TABLE #Deprecated_Samples
(Extent_id NUMERIC(20,0));

INSERT INTO #Deprecated_Samples (Extent_id)
SELECT s.Extent_id AS Extent_id
FROM 
  (SELECT Extent_id, parent_id FROM ${db}..Extent WHERE Extent_Type_id=1006
  ) s
JOIN ${db}..Extent l
ON s.parent_id = l.Extent_id
JOIN ${db}..Extent c
ON l.parent_id = c.Extent_id
WHERE s.Extent_id IN (SELECT Extent_id FROM #Deprecated)
   OR l.Extent_id IN (SELECT Extent_id FROM #Deprecated)
   OR c.Extent_id IN (SELECT Extent_id FROM #Deprecated)
   OR c.parent_id IN (SELECT Extent_id FROM #Deprecated)
;
DROP TABLE #Deprecated;

--in the GLK but not in the CTM (and not deprecated)
INSERT INTO #Odd_Samples (Extent_id, data_base, jira_id, status, reason)
SELECT s.Extent_id AS Extent_id,
       "${db}"     AS data_base,
       NULL        AS jira_id,
       NULL        AS status,
       "IN GLK not in CTM" AS reason
FROM 
  (SELECT Extent_id, parent_id FROM ${db}..Extent WHERE Extent_Type_id=1006
  ) s
LEFT JOIN (
  SELECT CONVERT(NUMERIC,value) AS Extent_id
  FROM ${db}..ctm_reference
) ctm
ON s.Extent_id = ctm.Extent_id
WHERE ctm.Extent_id is NULL 
   AND s.Extent_id NOT IN (SELECT Extent_id FROM #Deprecated_Samples);

--deprecated in the GLK, but active in the CTM
INSERT INTO #Odd_Samples (Extent_id, data_base, jira_id, status, reason)
SELECT s.Extent_id AS Extent_id,
       "${db}"     AS data_base,
       NULL        AS jira_id,
       ctm.status  AS status,
       "Deprecated in the GLK but active in the CTM" AS reason
FROM 
  #Deprecated_Samples s
JOIN (
  SELECT CONVERT(NUMERIC,value) AS Extent_id,
         b.name AS status
  FROM ${db}..ctm_reference a
  JOIN ${db}..ctm_reference_status b
  ON a.ctm_reference_status_id = b.ctm_reference_status_id
) ctm
ON s.Extent_id = ctm.Extent_id
WHERE ctm.status != "Deprecated"
;

--deprectated in the CTM, but active in the GLK
INSERT INTO #Odd_Samples (Extent_id, data_base, jira_id, status, reason)
SELECT ctm.Extent_id AS Extent_id,
       "${db}"     AS data_base,
       NULL        AS jira_id,
       ctm.status  AS status,
       "Deprecated in the CTM but active in the GLK" AS reason
FROM 
 (SELECT CONVERT(NUMERIC,value) AS Extent_id,
         b.name AS status
  FROM ${db}..ctm_reference a
  JOIN ${db}..ctm_reference_status b
  ON a.ctm_reference_status_id = b.ctm_reference_status_id
) ctm
LEFT JOIN #Deprecated_Samples s
ON s.Extent_id = ctm.Extent_id
--check that is exists at all
LEFT JOIN ${db}..Extent e
ON ctm.Extent_id = e.Extent_id
WHERE ctm.status = "Deprecated" 
      AND s.Extent_id IS NULL
      AND e.Extent_id IS NOT NULL;

----------------------------------
--stats on JIRA vs CTM vs GLK data
----------------------------------

--GLK
INSERT INTO #Totals
SELECT "${db}" AS data_base,
       "GLK-Deprecated" AS stat,
       Count(Extent_id) AS total
FROM #Deprecated_Samples;

INSERT INTO #Totals
SELECT "${db}" AS data_base,
       "GLK-Not-Deprecated" AS stat,
       Count(Extent_id) AS total
FROM ${db}..Extent
WHERE Extent_Type_id=1006 
  AND Extent_id NOT IN (SELECT Extent_id FROM #Deprecated_Samples);

INSERT INTO #Totals
SELECT "${db}" AS data_base,
       "GLK-All" AS stat,
       Count(Extent_id) AS total
FROM ${db}..Extent
WHERE Extent_Type_id=1006;

--CTM statuses
\call CTM-Totals Deprecated
\call CTM-Totals Unresolved
\call CTM-Totals Published

--CTM all/other
INSERT INTO #Totals
SELECT "${db}" AS data_base,
       "CTM-Other" AS stat,
       Count(c.Extent_id) AS total
FROM   
  (SELECT CONVERT(NUMERIC,value) AS Extent_id, 
         name AS status
  FROM ${db}..ctm_reference r
  JOIN ${db}..ctm_reference_status s
  ON r.ctm_reference_status_id = s.ctm_reference_status_id
  ) c
WHERE 
c.status NOT IN ("Deprecated","Unresolved","Published");

INSERT INTO #Totals
SELECT "${db}" AS data_base,
       "CTM-All" AS stat,
       Count(ctm_reference_id) AS total
FROM ${db}..ctm_reference r;

--JIRA
INSERT INTO #Totals
SELECT "${db}" AS data_base,
       "JIRA-Not-Deprecated" AS stat,
       Count(Extent_id) AS total
FROM   
  ${db}..ExtentAttribute
WHERE 
  ExtentAttributeType_id = 1670 
  AND Extent_id NOT IN (SELECT Extent_id FROM #Deprecated_Samples);

drop table #Deprecated_Samples;

INSERT INTO #Totals
SELECT "${db}" AS data_base,
       "JIRA-All" AS stat,
       Count(Extent_id) AS total
FROM   
  ${db}..ExtentAttribute
WHERE 
  ExtentAttributeType_id = 1670;

\done

--\echo Number of Samples in JIRA
--SELECT count(value) FROM ${db}..ExtentAttribute WHERE ExtentAttributeType_id=1670;

SELECT * from #Odd_Samples
go -m csv > /tmp/odd

SELECT * from #Totals
go -m csv > /tmp/totals


drop table #Odd_Samples;
drop table #Totals;
