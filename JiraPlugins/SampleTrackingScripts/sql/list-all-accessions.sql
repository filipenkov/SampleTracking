--Creates files listing the segments and accession codes of published samples

--requires 
--  ${dbs} a space seperated list of databases to find the deprecated samples from
--  #Published_Samples(Extent_id,db,collection_code,bac_id) -- find-all-published.sql
--Produces:
--  A a file per database, [/tmp/accessions_${db}]. Each is in the format:
--  <ncbi_accession>,<db>,<collection_code>,<bac_id>,<blinded_number>,<segment_name>

--\set dbs="giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb"


\for db in ${dbs}


SELECT LEFT(ac.value,8)  AS ncbi_accession,
       "${db}", 
       LEFT(p.collection_code,7)  AS collection_code, 
       LEFT(p.bac_id,5)  AS bac_id, 
       LEFT(bn.value,29) AS blinded_number, 
       LEFT(sn.value,4)  AS segment_name
--check the segments in this database
FROM (
  SELECT Extent_id, parent_id 
  FROM ${db}..Extent 
  WHERE Extent_Type_id IN (SELECT Extent_Type_id FROM ${db}..Extent_Type WHERE type="SEGMENT")
  ) ss
JOIN #Published_Samples p
ON ss.parent_id = p.Extent_id
LEFT JOIN (
  SELECT Extent_id, value 
  FROM ${db}..ExtentAttribute a
  JOIN ${db}..ExtentAttributeType t
  ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
  WHERE type="deprecated") sd
ON ss.Extent_id = sd.Extent_id
--find matching accession id, if any
LEFT JOIN 
  (SELECT Extent_id, value 
   FROM ${db}..ExtentAttribute a
   JOIN ${db}..ExtentAttributeType t
   ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
   WHERE type="ncbi_accession") ac
ON ac.Extent_id=ss.Extent_id
--get the segment name
LEFT JOIN 
  (SELECT Extent_id, value 
   FROM ${db}..ExtentAttribute a
   JOIN ${db}..ExtentAttributeType t
   ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
   WHERE type="segment_name") sn
ON sn.Extent_id=ss.Extent_id
--join with the Extent tree
JOIN ${db}..Extent sse
ON sse.Extent_id = ss.Extent_id
--join with the parent sample (to get bac_id, blinded_number etc)
JOIN ${db}..Extent s
ON s.Extent_id = sse.parent_id
--get the blinded_number, don't drop the row if there isn't a blinded number
LEFT JOIN
  (SELECT Extent_id, value 
   FROM ${db}..ExtentAttribute a
   JOIN ${db}..ExtentAttributeType t
   ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
   WHERE type="blinded_number") bn
ON bn.Extent_id=s.Extent_id
WHERE sd.Extent_id IS NULL
ORDER BY collection_code, bac_id, segment_name

go -m csv > /tmp/accessions_${db}

\done
