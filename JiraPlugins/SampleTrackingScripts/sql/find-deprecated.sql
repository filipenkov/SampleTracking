--Find Samples that are deprecated or in a deprecated lot / collection
-------------------------------------------------------------------------------
--Requires ${dbs} a space seperated list of databases to find the deprecated samples from
--Produces #Deprecated_Samples(Extent_id)
-------------------------------------------------------------------------------

--\set dbs="giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb"

--Tables beginning with a # are dropped automatically when a session ends
CREATE TABLE #Deprecated
(Extent_id NUMERIC(20,0))
--silent
go > /dev/null


CREATE TABLE #Deprecated_Samples
(Extent_id NUMERIC(20,0))
go > /dev/null

\for db in ${dbs}

INSERT INTO #Deprecated (Extent_id)
SELECT Extent_id 
FROM ${db}..ExtentAttribute a
JOIN ${db}..ExtentAttributeType t
ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
WHERE type="deprecated"
go > /dev/null

INSERT INTO #Deprecated_Samples (Extent_id)
SELECT s.Extent_id AS Extent_id
FROM
  (SELECT Extent_id, parent_id 
   FROM ${db}..Extent e
   JOIN ${db}..Extent_Type t
   ON e.Extent_Type_id = t.Extent_Type_id
   WHERE type="SAMPLE"
  ) s
JOIN ${db}..Extent l
ON s.parent_id = l.Extent_id
JOIN ${db}..Extent c
ON l.parent_id = c.Extent_id
WHERE s.Extent_id IN (SELECT Extent_id FROM #Deprecated)
   OR l.Extent_id IN (SELECT Extent_id FROM #Deprecated)
   OR c.Extent_id IN (SELECT Extent_id FROM #Deprecated)
   OR c.parent_id IN (SELECT Extent_id FROM #Deprecated)
go > /dev/null

DELETE FROM #Deprecated
go > /dev/null

\done

DROP TABLE #Deprecated
go > /dev/null
