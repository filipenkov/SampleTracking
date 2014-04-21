--requres ${DIR} to locate the other scripts
--requires ${dbs} a space seperated list of databases
--\let dbs=giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb
\loop ${DIR}/sql/find-deprecated.sql

\for db in ${dbs}

SELECT 
  LEFT (c.ref_id,10), 
  LEFT (value   ,20),
  COUNT(e.Extent_id)
FROM 
  ${db}..ExtentAttribute a
JOIN 
  ${db}..ExtentAttributeType t 
  ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
JOIN 
  ${db}..Extent e 
  ON a.Extent_id = e.Extent_id
JOIN 
  ${db}..Extent l 
  ON e.parent_id = l.Extent_id
JOIN 
  ${db}..Extent c 
  ON l.parent_id = c.Extent_id
LEFT JOIN
  #Deprecated_Samples d
  ON e.Extent_id = d.Extent_id
WHERE 
    t.type='jira_status' 
AND c.ref_id IN (${collections})
AND d.Extent_id IS NULL
GROUP BY c.ref_id,value
ORDER BY c.ref_id,value

go

\done
