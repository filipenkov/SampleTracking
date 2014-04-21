--requires: #Samples 
--          ${db}
--          ${project}
--produces: <stdout> in the format
--          <project>,Sample,<summary>,<db>,<collection code>,<bac id>,
--                <sample id>,<subtype>,<blinded #>,<lot>,<extent>,<batch>
--#Samples: Required Fields
-- bac_id          VARCHAR(36),  --e.g. 38340
-- collection_code VARCHAR(36),  --e.g. RFH3
-- lot_id          VARCHAR(36),  --e.g. RFH301 
-- sample_number   VARCHAR(128), --e.g. 00282
-- jira_id         VARCHAR(128), --e.g. ST-1599
-- blinded_number  VARCHAR(128), --e.g. NIGSP_CEIRS_CIP047_RFH3_00282 [Sample Creation Only]
-- extent_id       NUMERIC(20,0),--e.g. 1128822655440                 [Sample Creation Only]
-- sub_type        VARCHAR(128), --e.g. H3N2                          [Sample Creation Only]
-- batch_id        VARCHAR(128), --e.g.                               [Sample Creation Only]
-- deprecated      bit)          --0 or 1 (0=>ok, 1=>deprecated)

SELECT 
  "${project},"+ 
  "Sample,"+          
  "${db}_" + collection_code + "_" + bac_id +","+
  "${db},"+
  collection_code +","+
  bac_id +","+
  "${db}_" + collection_code + "_" + bac_id +","+
  sub_type +","+
  blinded_number+","+
  lot_id+","+
  CONVERT(VARCHAR, extent_id)+","+
  batch_id
FROM #Samples
WHERE jira_id is null and deprecated = 0
ORDER BY bac_id
go
\quit
