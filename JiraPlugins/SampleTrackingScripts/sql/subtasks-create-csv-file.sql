--requires: #Samples 
--          ${project}
--produces: <stdout> in the format
--          <jira id>,<project>,<collection code>,<bac id>
--
--#Samples: Required fields
-- jira_id         VARCHAR(128), --e.g. ST-1599
-- collection_code VARCHAR(36),  --e.g. RFH3
-- bac_id          VARCHAR(36),  --e.g. 38340
-- deprecated      bit           --0 or 1 (0=>ok, 1=>deprecated)
select 
  jira_id+
  ",${project},"+
  collection_code+"_"+
  bac_id+"_"
from #Samples where 
  deprecated = 0 AND  --ignore deprecated samples  
  jira_id is not null --and any samples that have not been added to JIRA
go
\quit
