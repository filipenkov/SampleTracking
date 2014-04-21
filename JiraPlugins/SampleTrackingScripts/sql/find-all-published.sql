--Find all Samples that have been published and are not deprecated

--requires 
--  ${dbs} a space seperated list of databases to find the deprecated samples from
--  #Deprecated_Samples(Extent_id)
--Produces #Published_Samples(Extent_id,db,collection_code,bac_id)

--Tables beginning with a # are dropped automatically when a session ends

--\set dbs="giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb"

CREATE TABLE #Published_Samples
(Extent_id NUMERIC(20,0), 
 db VARCHAR(10),
 collection_code VARCHAR(20),
 bac_id VARCHAR(10)
)
go > /dev/null

\for db in ${dbs}

--Get the info that exists in a tuple
INSERT INTO #Published_Samples (Extent_id, db, collection_code, bac_id)
SELECT s.Extent_id AS Extent_id, 
       "${db}" AS db,
       c.ref_id AS collection_code,
       s.ref_id AS bac_id
FROM
--Start with all samples
  (SELECT Extent_id AS Extent_id,
          parent_id AS parent_id,
          ref_id    AS ref_id 
   FROM ${db}..Extent 
   WHERE Extent_Type_id IN (SELECT Extent_Type_id FROM ${db}..Extent_Type WHERE type="SAMPLE")
  ) s
--limit to published samples
JOIN (
  SELECT a.Extent_id 
  FROM ${db}..ExtentAttribute a
  JOIN ${db}..ExtentAttributeType t
  ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
  WHERE type="jira_status"
    AND value="Sample Published"
) p
ON s.Extent_id = p.Extent_id
--remove deprecated samples
LEFT JOIN #Deprecated_Samples d
ON s.Extent_id = d.Extent_id

--gather the extra data
JOIN ${db}..Extent l
ON s.parent_id = l.Extent_id
JOIN ${db}..Extent c
ON l.parent_id = c.Extent_id
--filter out the deprecated samples
WHERE d.Extent_id IS NULL
go > /dev/null

\done
