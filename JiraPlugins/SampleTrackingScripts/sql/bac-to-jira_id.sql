--requires: #Samples 

--#Samples: Required fields
-- jira_id         VARCHAR(128), --e.g. ST-1599
-- deprecated      bit)          --0 or 1 (0=>ok, 1=>deprecated)
select 
  jira_id
from #Samples where 
  deprecated = 0 AND  --ignore deprecated samples  
  jira_id is not null --and any samples that have not been added to JIRA
go
\quit
