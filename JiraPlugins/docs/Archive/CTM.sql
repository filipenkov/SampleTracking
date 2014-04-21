--store the extent_id's we are using to save repreatedly using a sub-query
CREATE TABLE #CTM_Deprecated
(Extent_id NUMERIC(20,0));

--The Extent_ids of ctm_references that have a current state of 'Deprecated'
insert into #CTM_Deprecated 
select convert(NUMERIC,value) Extent_id from 
  ctm_reference_status join ctm_reference 
on 
  ctm_reference_status.ctm_reference_status_id = ctm_reference.ctm_reference_status_id 
where name='Deprecated';

\echo CTM_Deprecated total
select count(Extent_id) from #CTM_Deprecated;

\echo CTM_Deprecated -> null GLK Extent
Select count(ctm.Extent_id) 
from #CTM_Deprecated ctm join Extent
on ctm.Extent_id = Extent.Extent_id;

\echo CTM_Deprecated -> Extent with attribute of 'deprecated'
Select count(ctm.Extent_id)
from #CTM_Deprecated ctm join ExtentAttribute
on ctm.Extent_id = ExtentAttribute.Extent_id
where ExtentAttributeType_id in (
  select ExtentAttributeType_id 
  from ExtentAttributeType
  where type like '%eprecate%');

\echo GLK Extents with attribute 'deprecated' and their types
Select LEFT(Extent_Type.type,20), count(Extent.Extent_id)
from Extent join ExtentAttribute
on Extent.Extent_id = ExtentAttribute.Extent_id
join Extent_Type 
on Extent.Extent_Type_id = Extent_Type.Extent_Type_id
where ExtentAttributeType_id in (
  select ExtentAttributeType_id 
  from ExtentAttributeType
  where type like '%eprecate%')
group by Extent_Type.type;

--working the other way
CREATE TABLE #GLK_Deprecated
(Extent_id NUMERIC(20,0));
insert into #GLK_Deprecated
select Extent.Extent_id from
Extent join ExtentAttribute
on Extent.Extent_id = ExtentAttribute.Extent_id
join Extent_Type 
on Extent.Extent_Type_id = Extent_Type.Extent_Type_id
where ExtentAttributeType_id in (
  select ExtentAttributeType_id 
  from ExtentAttributeType
  where type like '%eprecate%')
and Extent_Type.type = 'SAMPLE';

\echo # of SAMPLES with the deprecated attribute in the GLK
select count(Extent_id) from #GLK_Deprecated;

\echo # of SAMPLES with the deprecated attribute that are also in the CTM

\echo # of SAMPLES with the deprecated attribute that are also in the CTM and have a status of deprecated

-----------------------------------------------------------------------------------------------
Select ctm.Extent_id, Left(extent.value,20) deprecated, extent_type, att_type from 
  (select convert(NUMERIC,value) Extent_id from 
    ctm_reference_status join ctm_reference 
  on 
    ctm_reference_status.ctm_reference_status_id = ctm_reference.ctm_reference_status_id 
  where 
    name='Deprecated') ctm 
left join 
  (select Extent.Extent_id Extent_id, Extent_Type.type extent_type, ExtentAttributeType.type att_type, value 
   from ExtentAttribute 
   join ExtentAttributeType 
   on ExtentAttribute.ExtentAttributeType_id = ExtentAttributeType.ExtentAttributeType_id
   join Extent
   on ExtentAttribute.Extent_id = Extent.Extent_id
   join Extent_Type
   on Extent.Extent_Type_id = Extent_Type.Extent_Type_id
   where ExtentAttributeType.type like "%eprecated"
  ) extent
on ctm.Extent_id = extent.Extent_id;

select Extent_id, value 
    from ExtentAttribute join ExtentAttributeType 
  on ExtentAttribute.ExtentAttributeType_id = ExtentAttributeType.ExtentAttributeType_id
  where type = 'deprecated') ExtentAttribute
on ctm.Extent_id=ExtentAttribute.Extent_id;

Select count(ExtentAttribute.Extent_id), Left(ctm.status,20) from 
  (select Extent.Extent_id, Extent_Type.type, ExtentAttributeType.type, value 
   from ExtentAttribute 
   join ExtentAttributeType 
   on ExtentAttribute.ExtentAttributeType_id = ExtentAttributeType.ExtentAttributeType_id
   join Extent
   on ExtentAttribute.Extent_id = Extent.Extent_id
   join Extent_Type
   on Extent.Extent_Type_id = Extent_Type.Extent_Type_id
   where ExtentAttributeType.type = 'deprecated') ExtentAttribute
left join 
  (select convert(NUMERIC,value) Extent_id, ctm_reference_status.name status from 
    ctm_reference_status join ctm_reference 
  on 
    ctm_reference_status.ctm_reference_status_id = ctm_reference.ctm_reference_status_id 
  ) ctm 
on ExtentAttribute.Extent_id = type.Extent_id 
where ctm.status is null
group by ctm.status;
