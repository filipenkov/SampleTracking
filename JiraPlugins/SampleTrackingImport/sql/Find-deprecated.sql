--Find Samples that are deprecated or in a deprecated lot / collection
DROP TABLE #Deprecated;
CREATE TABLE #Deprecated
(Extent_id NUMERIC(20,0));

DROP TABLE #Deprecated_Samples;

CREATE TABLE #Deprecated_Samples
(Extent_id NUMERIC(20,0));

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

INSERT INTO #Deprecated (Extent_id)
SELECT Extent_id FROM ${db}..ExtentAttribute WHERE ExtentAttributeType_id=1616;

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
DELETE FROM #Deprecated;

\done
