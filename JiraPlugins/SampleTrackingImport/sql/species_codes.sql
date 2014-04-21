\loop /home/pedworth/workspace/SampleTrackingImport/sql/Find-deprecated.sql

--count first incase it's a huge number
\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb
SELECT "${db}", count(a.Extent_id)
FROM ${db}..ExtentAttribute a
JOIN ${db}..ExtentAttributeType t
  ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
LEFT JOIN #Deprecated_Samples d
  ON a.Extent_id = d.Extent_id
WHERE d.Extent_id IS NULL
AND t.type="species_code";
\done

DROP TABLE #GLK_attributes_used;
CREATE TABLE #GLK_attributes_used
(dbName VARCHAR(8),
 attribute_type VARCHAR(80),
 num NUMERIC(10)
);
