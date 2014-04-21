--Groups published samples by country, state(if USA), and virus Type
--It outputs a sub-total for each group
-------------------------------------------------------------------------------
--Requires: ${dbs}                   A space seperated list of databases to use
--          ${unknown_district_file} This will contain a list of districts that
--                                   haven't been mapped yet
--Uses:     find-deprecated.sql
--          district-to-state.sql
-------------------------------------------------------------------------------

--executes the inserts that were generated from district-to-state.csv silently
go > /dev/null

\set dbs="$dbs"
\set unknown_district_file="$unknown_district_file"

--call find deprecated samples:
-- Requires: ${dbs}
-- Produces: #Deprecated_Samples(Extent_id)
\loop ${DIR}/sql/find-deprecated.sql

--The #results table is used to group the results even though they may
--be split across multiple databases
--DROP TABLE #results
CREATE TABLE #results (
  country  VARCHAR(20) NULL, 
  district VARCHAR(80) NULL,
  host     VARCHAR(10) NULL, 
  num      NUMERIC)
go > /dev/null

\for database in ${dbs}
-- The column names are preceded by characters to ensure the ordering
-- of them when they are converted into a table inside Excel/Open Office
-- The expected order is Flu A, Flu B, Avian, H1N1pdm, Swine, Other
INSERT INTO #results(country, district, host, num)
SELECT
  LEFT(country.value,20), 
  CASE
    WHEN country.value = 'USA' THEN
      district.value
    ELSE
      'N/A'
  END,
  constructed_host = 
    CASE 
      WHEN host.value = 'human' AND project_id.value='37813' THEN 
        'D H1N1pdm'
      WHEN host.value = 'human' THEN
        LEFT(flu_type.value,1) + ' HUMAN Flu' + LEFT(flu_type.value,1)
      WHEN host.value = 'avian' THEN
        'C AVIAN Flu'
      WHEN host.value = 'swine' THEN
        'E SWINE'
      WHEN "${database}" in ('giv','giv2','giv3','piv','swiv') THEN
        'F OTHER Flu'
      ELSE 
        "${database}"
  END,
  count(published.Extent_id)
FROM
  (SELECT Extent_id 
   FROM ${database}..ExtentAttribute a
   JOIN ${database}..ExtentAttributeType t
   ON a.ExtentAttributeType_id = t.ExtentAttributeType_id
   WHERE type='jira_status' AND value='Sample Published') published 
  LEFT JOIN 
  #Deprecated_Samples deprecated
    on published.Extent_id = deprecated.Extent_id
  LEFT JOIN
    (SELECT 
      Extent_id, value 
     FROM 
       ${database}..ExtentAttribute 
     WHERE 
       ExtentAttributeType_id = 
       (SELECT ExtentAttributeType_id FROM ${database}..ExtentAttributeType WHERE type = 'country')
    ) country 
    on published.Extent_id = country.Extent_id
  LEFT JOIN
    (SELECT 
      Extent_id, value 
     FROM 
       ${database}..ExtentAttribute 
     WHERE 
       ExtentAttributeType_id = 
       (SELECT ExtentAttributeType_id FROM ${database}..ExtentAttributeType WHERE type = 'district')
    ) district 
    on published.Extent_id = district.Extent_id
  LEFT JOIN 
    (SELECT 
      Extent_id, value 
     FROM 
       ${database}..ExtentAttribute 
     WHERE 
       ExtentAttributeType_id = 
       (SELECT ExtentAttributeType_id FROM ${database}..ExtentAttributeType WHERE type = 'normalized_host')
    ) host
    on published.Extent_id = host.Extent_id 
  LEFT JOIN 
    (SELECT 
      Extent_id, value 
     FROM 
       ${database}..ExtentAttribute 
     WHERE 
       ExtentAttributeType_id = 
       (SELECT ExtentAttributeType_id FROM ${database}..ExtentAttributeType WHERE type = 'type')
    ) flu_type
    on published.Extent_id = flu_type.Extent_id 
  LEFT JOIN 
    (SELECT 
      Extent_id, value 
     FROM 
       ${database}..ExtentAttribute 
     WHERE 
       ExtentAttributeType_id = 
       (SELECT ExtentAttributeType_id FROM ${database}..ExtentAttributeType WHERE type = 'project_id')
    ) project_id
    on published.Extent_id = project_id.Extent_id
WHERE 
  deprecated.Extent_id is null 
GROUP BY
  host.value, 
  country.value,
  district.value,
  flu_type.value,
  project_id.value
go > /dev/null
\done

-------------------------------------------------------------------------------
--Map results from the USA to states based on the district field
--The district fields are free text and haven't been normalized
--We use a mapping table populated using district-to-state.sql
--to map those that we have seen before. Those that haven't been
--mapped are listed to a temp file. This needs examining by the
--calling script to ensure someone is informed that work needs 
--to be done.
--To keep the totals correct these unmapped samples are added to
--with state='Unkown'.
-------------------------------------------------------------------------------

--Output the totals, using the mapping from above
SELECT 
  CASE
    WHEN country = '' OR country IS NULL THEN
      'Unknown'
    ELSE
      country
  END AS country, 
  CASE
    WHEN country = 'USA' AND (state = '' OR state IS NULL) THEN
      'Unknown'
    WHEN country = 'USA' THEN
      state
    ELSE
      'N/A'
  END AS state,
  host AS host, 
  sum(num) AS "count"
FROM 
  #results 
LEFT JOIN
  #state_map
ON #results.district = #state_map.district
GROUP BY
  country,
  state,
  host
ORDER BY
  country,
  state,
  host
go -m csv

--Write unknown states to a temp file (as we can't have two streams returned)
SELECT #results.district
FROM #results
LEFT JOIN #state_map
ON #results.district = #state_map.district
WHERE #state_map.district IS NULL 
AND #results.district IS NOT NULL
GROUP BY #results.district
go -m csv > ${unknown_district_file}
