\loop /home/pedworth/workspace/SampleTrackingImport/sql/Find-deprecated.sql

\for db in giv giv2 giv3 piv swiv

SELECT c.Extent_id, c.last_update FROM 
--selects the date of the last update of everything in the CTM that is 'published'
(
  SELECT CONVERT(NUMERIC,r.value) AS Extent_id,
         r.ctm_reference_id,
         MAX(h.date_in) AS last_update
  FROM ${db}..ctm_reference r
  JOIN ${db}..ctm_reference_status s
  ON r.ctm_reference_status_id = s.ctm_reference_status_id
  JOIN ${db}..ctm_reference_history h
  ON h.ctm_reference_id=r.ctm_reference_id
  WHERE name="Published"
  GROUP BY r.ctm_reference_id
) c
LEFT JOIN 
(
  SELECT Extent_id FROM ${db}..ExtentAttribute WHERE ExtentAttributeType_id=1609
) a
ON c.Extent_id = a.Extent_id
WHERE a.Extent_id IS NULL;

\done
