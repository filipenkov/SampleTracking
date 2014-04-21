--DROP TABLE #mappings_all
CREATE TABLE #mappings_all
(num NUMERIC(20),
 host VARCHAR(40),
 normalized VARCHAR(40)
)
go
--DROP TABLE #mappings
CREATE TABLE #mappings
(host VARCHAR(40),
 normalized VARCHAR(40)
)
go
--DROP TABLE #missing
CREATE TABLE #missing
(host VARCHAR(40),
 num NUMERIC
)
go

--capture the mappings used in each db
\for db in giv giv2 giv3 piv swiv

  INSERT INTO #mappings_all (num,host,normalized)
  SELECT count(e.Extent_id) AS num, h.value AS host, n.value AS normalized
  FROM (
    SELECT Extent_id 
    FROM ${db}..Extent e 
    JOIN ${db}..Extent_Type t
    ON e.Extent_Type_id = t.Extent_Type_id
    WHERE type='SAMPLE') e
  LEFT JOIN (
    SELECT Extent_id, value 
    FROM ${db}..ExtentAttribute a 
    JOIN ${db}..ExtentAttributeType t 
    ON a.ExtentAttributeType_id=t.ExtentAttributeType_id 
    WHERE type='normalized_host') n
  ON e.Extent_id = n.Extent_id
  LEFT JOIN (
    SELECT Extent_id, value 
    FROM ${db}..ExtentAttribute a 
    JOIN ${db}..ExtentAttributeType t 
    ON a.ExtentAttributeType_id=t.ExtentAttributeType_id 
    WHERE type='host') h
  ON e.Extent_id = h.Extent_id
  WHERE n.Extent_id IS NOT NULL 
    AND h.value IS NOT NULL
  GROUP BY h.value, n.value
  ORDER BY h.value, n.value
  go

\done

--remove duplicates from multiple dbs

INSERT INTO #mappings (host, normalized)
SELECT host, MAX(normalized) AS normalized 
FROM #mappings_all 
GROUP BY host
go

DROP TABLE #mappings_all
go

--find any samples that don't have a normalized host and use the 1 to 1 table to set it
\for db in giv giv2 giv3 piv swiv
  INSERT INTO ${db}..ExtentAttribute (Extent_id, ExtentAttributeType_id, value)
  SELECT e.Extent_id, 
         (SELECT ExtentAttributeType_id FROM ${db}..ExtentAttributeType WHERE type='normalized_host'), 
         m.normalized
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
  --carry out the mapping
  JOIN #mappings m 
  ON h.value = m.host
  --left join used to check that there is no normalized host associated
  LEFT JOIN (SELECT Extent_id, value FROM ${db}..ExtentAttribute WHERE ExtentAttributeType_id=1675) n 
  ON e.Extent_id = n.Extent_id
  WHERE n.Extent_id IS NULL
  go

\done

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
SELECT host, sum(num)
FROM #missing
GROUP BY host
ORDER BY host
go

--Add the unknown hosts to #mappings
--e.g. INSERT INTO #mappings (host, normalized) values("N/A","other")\ngo
--normalized names are: avian,human,other,swine,equine

--then re-run the '--find any samples that don't have a normalized host and use the 1 to 1 table to set it' bit
