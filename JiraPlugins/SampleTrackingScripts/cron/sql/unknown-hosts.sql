--DROP TABLE #missing
CREATE TABLE #missing
(host VARCHAR(40),
 num NUMERIC
)
go

--check for samples that still don't have a normalized host after the lookup
DELETE FROM #missing
go

\for db in giv giv2 giv3 piv swiv
  INSERT INTO #missing (host, num)
  SELECT h.value, count(h.Extent_id)
  --restrict the search to samples
  FROM (
    SELECT Extent_id 
    FROM ${db}..Extent e 
    JOIN ${db}..Extent_Type t
    ON e.Extent_Type_id = t.Extent_Type_id
    WHERE type='SAMPLE') e
  --restrict to samples that have a host
  JOIN (
    SELECT Extent_id, value 
    FROM ${db}..ExtentAttribute a 
    JOIN ${db}..ExtentAttributeType t 
    ON a.ExtentAttributeType_id=t.ExtentAttributeType_id 
    WHERE type='host') h
  ON e.Extent_id = h.Extent_id
  --check for normalized_host
  LEFT JOIN (
    SELECT Extent_id, value 
    FROM ${db}..ExtentAttribute a 
    JOIN ${db}..ExtentAttributeType t 
    ON a.ExtentAttributeType_id=t.ExtentAttributeType_id 
    WHERE type='normalized_host') n
  ON e.Extent_id = n.Extent_id
  WHERE n.value IS NULL
  GROUP BY n.value, h.value
  ORDER BY n.value, h.value
  go
\done

--A list of unknown hosts
SELECT host AS "Host", sum(num) AS "Occurrences"
FROM #missing
GROUP BY host
ORDER BY host
go -w 60 -m pretty
