--creates #Deprecated_Samples (Extent_id NUMERIC(20,0));

\loop /home/pedworth/workspace/SampleTrackingImport/sql/Find-deprecated.sql

DROP TABLE #Jira_Data_DB;
CREATE TABLE #Jira_Data_DB
(jira_id VARCHAR(20) NULL,
 jira_status VARCHAR(20) NULL,
 db VARCHAR(5),
 Extent_id NUMERIC(20,0) NULL
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
--Use JIRA ID as the primary
INSERT INTO #Jira_Data_DB
SELECT id.value AS jira_id, status.value AS jira_status, "${db}" AS db, id.Extent_id AS Extent_id
FROM
(SELECT Extent_id, value
  FROM ${db}..ExtentAttribute a
  JOIN ${db}..ExtentAttributeType t
    ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
  WHERE t.type="jira_id") id
LEFT JOIN
(SELECT Extent_id, value
  FROM ${db}..ExtentAttribute a
  JOIN ${db}..ExtentAttributeType t
    ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
  WHERE t.type="jira_status") status
ON id.Extent_id = status.Extent_id;

--Use JIRA STATUS as the primary
INSERT INTO #Jira_Data_DB
SELECT id.value AS jira_id, status.value AS jira_status, "${db}" AS db, id.Extent_id AS Extent_id
FROM
(SELECT Extent_id, value
  FROM ${db}..ExtentAttribute a
  JOIN ${db}..ExtentAttributeType t
    ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
  WHERE t.type="jira_status") status
LEFT JOIN
(SELECT Extent_id, value
  FROM ${db}..ExtentAttribute a
  JOIN ${db}..ExtentAttributeType t
    ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
  WHERE t.type="jira_id") id
ON status.Extent_id = id.Extent_id
WHERE id.Extent_id IS NULL;

\done

DROP TABLE #Missmatch;
CREATE TABLE #Missmatch
(jira_id  VARCHAR(10) NULL,
 sample_id VARCHAR(20) NULL,
 db       VARCHAR(5),
 jira_status VARCHAR(20) NULL,
 ctm_status VARCHAR(20) NULL
);

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

INSERT INTO #Missmatch
SELECT db.jira_id     AS jira_id,
       "${db}_" + p.ref_id + "_" + e.ref_id AS sample_id,
       "${db}"        AS db,
       db.jira_status AS jira_status,
       ctm.status     AS ctm_status
FROM (
  SELECT CONVERT(NUMERIC,value) AS Extent_id,
         b.name AS status
  FROM ${db}..ctm_reference a
  JOIN ${db}..ctm_reference_status b
  ON a.ctm_reference_status_id = b.ctm_reference_status_id
) ctm
LEFT JOIN
#Jira_Data_DB db
ON ctm.Extent_id = db.Extent_id
JOIN ${db}..Extent e
ON ctm.Extent_id = e.Extent_id
JOIN ${db}..Extent p
ON e.parent_id = p.Extent_id
--only test a sub-set
WHERE db.jira_status != ctm.status
AND db.jira_status NOT IN
  (Select jira_status FROM #Status_Map WHERE ctm_status=ctm.status);

--Has no JIRA_ID

INSERT INTO #Missmatch
SELECT "None"     AS jira_id,
       CONVERT(VARCHAR,ctm.Extent_id) AS sample_id,
       "${db}"        AS db,
       NULL AS jira_status,
       ctm.status     AS ctm_status
FROM (
  SELECT CONVERT(NUMERIC,value) AS Extent_id,
         b.name AS status
  FROM ${db}..ctm_reference a
  JOIN ${db}..ctm_reference_status b
  ON a.ctm_reference_status_id = b.ctm_reference_status_id
) ctm
LEFT JOIN
#Jira_Data_DB db
ON ctm.Extent_id = db.Extent_id
WHERE db.Extent_id IS NULL;

\done

\echo Missmatched Status Pairs
--find all missmatch pairs;
SELECT count(jira_id), jira_status, ctm_status FROM #Missmatch GROUP BY jira_status, ctm_status

go -m csv > /tmp/pairs.out
