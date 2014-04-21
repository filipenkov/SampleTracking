\loop /tmp/db-setup.sql

\for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu eeev flumb

UPDATE
    #SampleData
SET
    s.Extent_id=e.Extent_id
FROM
    #SampleData s
    JOIN ${db}..Extent e
    ON s.bac_id=e.ref_id
WHERE
    s.db = "${db}"
;

UPDATE
    #SampleData
SET
    s.lot=l.ref_id
FROM
    #SampleData s
    JOIN ${db}..Extent e
    ON e.Extent_id = s.Extent_id
    JOIN ${db}..Extent l
    ON e.parent_id = l.Extent_id
WHERE
    s.db = "${db}"
;

UPDATE
    #SampleData
SET
    s.batch_id=a.value
FROM
    #SampleData s
    JOIN ${db}..ExtentAttribute a
    ON a.Extent_id = s.Extent_id
WHERE
    a.ExtentAttributeType_id = 1665
  AND
    s.db = "${db}"
;

UPDATE
    #SampleData
SET
    s.blinded_number=a.value
FROM
    #SampleData s
    JOIN ${db}..ExtentAttribute a
    ON a.Extent_id = s.Extent_id
WHERE
    a.ExtentAttributeType_id = 1515
  AND
    s.db = "${db}"
;

UPDATE
    #SampleData
SET
    s.sample_id=a.value
FROM
    #SampleData s
    JOIN ${db}..ExtentAttribute a
    ON a.Extent_id = s.Extent_id
WHERE
    a.ExtentAttributeType_id = 1590
  AND
    s.db = "${db}"
;

UPDATE
    #SampleData
SET
    s.subtype=a.value
FROM
    #SampleData s
    JOIN ${db}..ExtentAttribute a
    ON a.Extent_id = s.Extent_id
WHERE
    a.ExtentAttributeType_id = 1610
  AND
    s.db = "${db}"
;

\done
