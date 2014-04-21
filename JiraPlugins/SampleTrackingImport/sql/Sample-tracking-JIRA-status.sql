DROP TABLE #Jira_Data;
CREATE TABLE #Jira_Data
(jira_id VARCHAR(20) NULL,
 jira_status VARCHAR(20) NULL
);

\loop "/export/jira-status.sql"

DROP TABLE #Jira_Data_DB;
CREATE TABLE #Jira_Data_DB
(jira_id VARCHAR(20) NULL,
 jira_status VARCHAR(20) NULL,
 db VARCHAR(5)
);

DROP TABLE #Status_Map;
CREATE TABLE #Status_Map
(ctm_status VARCHAR(30),
jira_status VARCHAR(30));

INSERT INTO #Status_Map (ctm_status, jira_status) values ('Published', 'Sample Published');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Received', 'Received Sample');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Submitted to Genbank', 'Submitted');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Deprecated', 'Deprecated');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Unresolved', 'Unresolved');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Validation', 'Validate');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Edit TT','Close Sample');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Edit NF','Close Sample');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Closure TT','Close Sample');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Closure NF','Close Sample');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('Initial Manual Edit','Close Sample');
INSERT INTO #Status_Map (ctm_status, jira_status) values ('NextGen Validation','Validate');



\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb
INSERT INTO #Jira_Data_DB
SELECT jira_id, jira_status, "${db}" AS db
FROM #Jira_Data j
JOIN ${db}..ExtentAttribute a
ON j.jira_id = a.value;
\done

DROP TABLE #Results;
CREATE TABLE #Results
(db     VARCHAR(10),
 status VARCHAR(20),
 value  NUMERIC(20,0)
);

INSERT INTO #Results
SELECT db, jira_status AS status, count(jira_id) AS value 
FROM #Jira_Data_DB 
WHERE jira_status IN ('Sample Published','Deprecated','Unresolved')
GROUP BY db, jira_status;

INSERT INTO #Results
SELECT db, "Other" AS status, count(jira_id) AS value 
FROM #Jira_Data_DB
WHERE jira_status NOT IN ('Sample Published','Deprecated','Unresolved')
GROUP BY db;

DROP TABLE #Missmatch;
CREATE TABLE #Missmatch
(jira_id  VARCHAR(10),
 sample_id VARCHAR(20),
 db       VARCHAR(5),
 jira_status VARCHAR(20),
 ctm_status VARCHAR(20)
);

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

INSERT INTO #Missmatch
SELECT db.jira_id     AS jira_id, 
       "${db}_" + p.ref_id + "_" + e.ref_id AS sample_id,
       "${db}"        AS db, 
       db.jira_status AS jira_status, 
       ctm.status     AS ctm_status
FROM #Jira_Data_DB db
JOIN ${db}..ExtentAttribute a
ON a.value = db.jira_id
JOIN (
  SELECT CONVERT(NUMERIC,value) AS Extent_id,
         b.name AS status
  FROM ${db}..ctm_reference a
  JOIN ${db}..ctm_reference_status b
  ON a.ctm_reference_status_id = b.ctm_reference_status_id
) ctm
ON a.Extent_id = ctm.Extent_id
JOIN ${db}..Extent e
ON a.Extent_id = e.Extent_id
JOIN ${db}..Extent p
ON e.parent_id = p.Extent_id
--only test a sub-set
WHERE db.jira_status != ctm.status
AND db.jira_status NOT IN 
  (Select jira_status FROM #Status_Map WHERE ctm_status=ctm.status);

\done

\echo Missmatched Status Pairs
--find all missmatch pairs;
SELECT count(jira_id), jira_status, ctm_status FROM #Missmatch GROUP BY jira_status, ctm_status

go -m csv > /tmp/pairs.out

\echo Finished Samples with Missmatched statuses
--list samples with the wrong final status
SELECT jira_id, sample_id, jira_status, ctm_status 
FROM #Missmatch m
WHERE jira_status IN ('Sample Published','Deprecated','Unresolved')
OR ctm_status IN ('Published','Deprecated','Unresolved')
ORDER BY jira_id

go -m csv > /tmp/missmatched-finished.out

\echo Open Samples with Missmatched statuses
--list samples with the wrong final status
SELECT jira_id, sample_id, jira_status, ctm_status 
FROM #Missmatch m
WHERE jira_status NOT IN ('Sample Published','Deprecated','Unresolved')
AND ctm_status NOT IN ('Published','Deprecated','Unresolved')
ORDER BY jira_id

go -m csv > /tmp/missmatched-active.out

\echo JIRA Stats
SELECT db, status, value FROM #Results

go -m csv > /tmp/jira-stats.out
