--Requires #Selected table
--         ${db}
--Produces #Samples table
CREATE TABLE #Samples
(bac_id          VARCHAR(36)   NULL,--e.g. 38340
 lot_id          VARCHAR(36)   NULL,--e.g. RFH301 
 collection_code VARCHAR(36)   NULL,--e.g. RFH3
 sample_number   VARCHAR(128)  NULL,--e.g. 00282
 jira_id         VARCHAR(128)  NULL,--e.g. ST-1599
 blinded_number  VARCHAR(128)  NULL,--e.g. NIGSP_CEIRS_CIP047_RFH3_00282 [Sample Creation Only]
 extent_id       NUMERIC(20,0) NULL,--e.g. 1128822655440                 [Sample Creation Only]
 sub_type        VARCHAR(128)  NULL,--e.g. H3N2                          [Sample Creation Only]
 batch_id        VARCHAR(128)  NULL,--e.g.                               [Sample Creation Only]
 deprecated      bit) --0 or 1 (0=>ok, 1=>deprecated)
go > /dev/null

INSERT INTO #Samples
select 
  bac_id,
  lot.ref_id AS lot_id, 
  collection.ref_id AS collection_code,   
  sample_number,
  jira_id,
  blinded_number,
  extent_id,
  sub_type,
  batch_id,
  CASE WHEN deprecated is null THEN 0 WHEN deprecated is not null THEN 1 END
from 
  --[sample] Extent_id, parent_id, sample_number, jira_id, deprecated (Filtered by #Selected)
  (
    --Extent_id isn't used outside of this table, but needs to be selected for the group by
    select inner_sample.Extent_id AS extent_id, inner_sample.ref_id AS bac_id, inner_sample.parent_id,
      --max, case and group by combined like this are commonly used to 
      --convert a series of rows into columns
      --As there should only be one value for each ExtentAttribute per Extent
      --the MAX will be equal to that one value. If there are no values then
      --it will be null
      MAX(CASE WHEN type='sample_number' THEN value END) as sample_number,
      MAX(CASE WHEN type='jira_id' THEN value END) as jira_id,
      MAX(CASE WHEN type='subtype' THEN value ELSE NULL END) as sub_type,
      MAX(CASE WHEN type='blinded_number' THEN value END) as blinded_number,
      MAX(CASE WHEN type='batch_id'   THEN value END) as batch_id,
      MAX(CASE WHEN type='deprecated' THEN value END) as deprecated
    from
      --[attrib] Extent_id, type, value (unfiltered)
      ( --merge of ExtentAttribute and ExtentAttributeType
        --used to get the name value pairs
        select Extent_id, type, value
        from ${db}..ExtentAttribute ea join ${db}..ExtentAttributeType eat
        on ea.ExtentAttributeType_id = eat.ExtentAttributeType_id
      ) attrib 
    join
      --[inner_sample] Extent_id, parent_id (Filtered to only include #Selected samples)
      ( 
        select selected.Extent_id, extent.ref_id, extent.parent_id from 
          --selected: Extent_id (defined outside this query)
          #Selected selected
        join 
	  --[extent] Extent_id, parent_id, ... (unfiltered)
          ${db}..Extent extent
	on extent.Extent_id = selected.Extent_id
      ) inner_sample
    --the on clause limits the select to only the lot's samples
    on attrib.Extent_id = inner_sample.Extent_id
    --the group by is part of the row to column conversion
    group by inner_sample.Extent_id
  ) sample
--the next two joins walk up the tree from the sample level to first the
--lot and then the collection.
--[lot] Extent_id, parent_id (unfiltered, the join limits it to #Selected)
join ${db}..Extent lot on lot.Extent_id = sample.parent_id
--[collection] Extent_id, parent_id (unfiltered, the join limits it to #Selected)
join ${db}..Extent collection on lot.parent_id = collection.Extent_id  
--quite
go > /dev/null
