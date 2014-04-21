\loop ${DIR}/sql/find-deprecated.sql

\for db in ${dbs}

SELECT j.value AS ' ', b.value AS '  '
FROM
( SELECT Extent_id, value
  FROM ${db}..ExtentAttribute
  WHERE ExtentAttributeType_id=
  ( SELECT ExtentAttributeType_id 
    FROM ${db}..ExtentAttributeType
    WHERE type="${field}")
  ) AS b
JOIN
  ( SELECT Extent_id, value
  FROM ${db}..ExtentAttribute
  WHERE ExtentAttributeType_id=
  ( SELECT ExtentAttributeType_id 
    FROM ${db}..ExtentAttributeType
    WHERE type='jira_id')
  ) AS j
ON b.Extent_id = j.Extent_id

go -m csv
\done
