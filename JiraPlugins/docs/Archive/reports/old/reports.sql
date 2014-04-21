'CLOSURE_Q'
select 
  e.ref_id, 
  b.lib_name, 
  b.lib_desc 
from 
  Extent e, 
  ctm_reference r,
  ctm_reference_status s, 
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and 
  convert(numeric,e.ref_id) = b.id and 
  convert(numeric,r.value)  = e.Extent_id and
  s.name like 'Received'   
order by b.lib_name

'CLOSURE_Q2'
select 
  e.ref_id 
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s,
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and
  convert(numeric,e.ref_id) = b.id and 
  convert(numeric,r.value)  = e.Extent_id and
  s.name like 'Edit NF' 
order by b.lib_name

'CLOSURE_Q_COLLECTION'
select 
  e.ref_id 
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s,
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and
  convert (numeric,e.ref_id)=b.id and 
  convert(numeric,r.value)=e.Extent_id and 
  s.name like 'Pending' and
  b.lib_name like "RSV%"

'CLOSURE_Q_DATE'
select 
  collection.ref_id as collection, 
  sample.ref_id as sample, 
  h.date_in 
from 
  Extent collection, 
  Extent sample, 
  ctm_reference r, 
  ctm_reference_history h,
  ctm_reference_status s 
where 
  h.ctm_reference_status_id=s.ctm_reference_status_id and 
  convert(numeric,r.value)=sample.Extent_id and 
  r.ctm_reference_id=h.ctm_reference_id and 
  sample.parent_id=collection.Extent_id and 

  s.name="Published" and 
  collection.ref_id NOT IN ('XX') 
order by h.date_in

'CLOSURE_STAT_SPECIFIC'
select 
  b.lib_desc,
  b.lib_name, 
  e.ref_id, 
  s.name 
from 
  Extent e, 
  ctm_reference r,
  ctm_reference_status s, 
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and 
  convert (numeric,e.ref_id)=b.id and
  convert(numeric,r.value)=e.Extent_id and 
  b.lib_name like "DW09%63" 
order by b.lib_name

'CLOSURE_STATUS'
select 
  e.ref_id, 
  b.lib_desc,
  b.lib_name, 
  s.name 
from 
  Extent e, 
  ctm_reference r,
  ctm_reference_status s, 
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and 
  convert (numeric,e.ref_id)=b.id and
  convert(numeric,r.value)=e.Extent_id and 
  b.lib_name like "GN%" 
order by s.name

'CLOSURE_STATUS2'
select 
  e.ref_id 
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s,
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and
  convert (numeric,e.ref_id)=b.id and 
  convert(numeric,r.value)=e.Extent_id and
  b.lib_name like "JPA%" 
order by s.name

'CLOSURE_STATUS_COUNT'
select 
  s.name, 
  count(*) 
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s,
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and
  convert (numeric,e.ref_id)=b.id and 
  convert(numeric,r.value)=e.Extent_id and
  b.lib_name like "JHC%" 
group by s.name 
order by s.name


'COMMENT'
select 
  distinct e.ref_id, 
  rh.comment, 
  rh.date_in
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s, 
  ctm_reference_history rh
where 
  r.ctm_reference_status_id=s.ctm_reference_status_id and 
  convert(numeric,r.value)=e.Extent_id and 
  rh.ctm_reference_id=r.ctm_reference_id and 
  e.ref_id between '38491' and '38493'


'FL_STATUS_SQL'
select 
  b.lib_name,
  e.ref_id,
  s.name 
from 
  Extent e,
  ctm_reference r,
  ctm_reference_status s,
  track..bac b, 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and
  convert (numeric,e.ref_id)=b.id and
  convert(numeric,r.value)=e.Extent_id and
  b.lib_name in("FL00001","FL00002","FL00003","FL00005","FL00006","FL00007","FL00009","FL00010","FL00013","FL00020","FL00021","FL00022","FL00023","FL00029","FL00030","FL00031","FL00032","FL00033","FL00035","FL00036","FL00037","FL00038","FL00039","FL00043","FL00047","FL00050","FL00053","FL00054","FL00055","FL00060","FL00063","FL00065","FL00066","FL00069","FL00071","FL00072","FL00073","FL00074","FL00075","FL00076","FL00077","FL00080","FL00081","FL00082","FL00087","FL00089","FL00090","FL00091","FL00092","FL00102","FL00103","FL00104","FL00106","FL00107","FL00111","FL00113","FL00114","FL00115","FL00117","FL00120","FL00121","FL00122","FL00123","FL00130","FL00131","FL00136","FL00139","FL00140","FL00142","FL00146","FL00148","FL00149","FL00152","FL00153","FL00156","FL00158","FL00160","FL00162","FL00168","FL00173","FL00174","FL00178","FL00179","FL00180","FL00181","FL00183","FL00184","FL00187","FL00188","FL00190","FL00192","FL00193","FL00194","FL00197","FL00198","FL00199","FL00200","FL00201","FL00202","FL00203")
order by s.name

'SDI0708_STATUS_SQL'
select 
  b.lib_name,
  e.ref_id,
  s.name 
from 
  Extent e,
  ctm_reference r,
  ctm_reference_status s,
  track..bac b, 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and
  convert (numeric,e.ref_id)=b.id and
  convert(numeric,r.value)=e.Extent_id and
  b.lib_name in ("SDI10629","SDI14488","SDI19820","SDI18814","SDI13053","SDI19879","SDI14668","SDI15268","SDI17431","SDI16666","SDI15684","SDI13247","SDI18949","SDI13818","SDI11286","SDI15148","SDI12124","SDI16012","SDI11305","SDI16547","SDI16770","SDI19672","SDI15359","SDI10994","SDI14100","SDI19513","SDI13381","SDI16938","SDI17729","SDI11083","SDI15818","SDI16083","SDI16032","SDI13460","SDI19290","SDI19384","SDI16841","SDI16980","SDI13185","SDI19124","SDI10623","SDI15303","SDI15371","SDI16602","SDI17271","SDI11648","SDI10444","SDI19035","SDI10200","SDI10480","SDI12132","SDI18001","SDI17187","SDI19064","SDI13879","SDI16361","SDI15178","SDI18926","SDI10396","SDI11871","SDI14498","SDI19283","SDI12761","SDI17181","SDI16678","SDI12396","SDI19962","SDI16961","SDI19821","SDI18811","SDI15896","SDI15876","SDI17364","SDI19638","SDI19605","SDI16716","SDI13234","SDI16008","SDI14450","SDI18344","SDI19179","SDI17028","SDI16304","SDI17567","SDI14453","SDI13377","SDI16251","SDI13725","SDI15191","SDI13129","SDI14070","SDI12383","SDI19287","SDI17196","SDI13549","SDI18581","SDI16720","SDI11618","SDI17101","SDI17490")
order by s.name

'MULTI_CLOSURE_Q'
select 
  e.ref_id, 
  b.lib_name, 
  b.lib_desc 
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s, 
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and 
  convert (numeric,e.ref_id)=b.id and 
  convert(numeric,r.value)=e.Extent_id and
  s.name like 'Primer Design'

union all

select 
  e.ref_id, 
  b.lib_name, 
  b.lib_desc 
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s, 
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and 
  convert (numeric,e.ref_id)=b.id and 
  convert(numeric,r.value)=e.Extent_id and
  s.name like 'Hold'

union all

select 
  e.ref_id, 
  b.lib_name, 
  b.lib_desc 
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s, 
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and 
  convert (numeric,e.ref_id)=b.id and 
  convert(numeric,r.value)=e.Extent_id and
  s.name like 'Temp Hold'

union all

select 
  e.ref_id, 
  b.lib_name, 
  b.lib_desc 
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s, 
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and 
  convert (numeric,e.ref_id)=b.id and 
  convert(numeric,r.value)=e.Extent_id and
  s.name like 'Coinfection/Mixed'

'ONE_PERSON_Q'
select 
  e.ref_id, 
  b.lib_name, 
  b.lib_desc 
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s, 
  track..bac b 
where 
  r.ctm_reference_status_id = s.ctm_reference_status_id and 
  convert (numeric,e.ref_id)=b.id and 
  convert(numeric,r.value)=e.Extent_id and
  s.name like 'Closure KD'


'SINGLE_SAMPLE_STATUS'
select 
  b.lib_name, 
  e.ref_id, 
  s.name
from 
  Extent e, 
  ctm_reference r, 
  ctm_reference_status s, 
  track..bac b
where
  r.ctm_reference_status_id = s.ctm_reference_status_id and 
  convert (numeric,e.ref_id)=b.id and 
  convert(numeric,r.value)=e.Extent_id and 
  e.ref_id = "29725"

'SUBMISSION_Q'
select 
  collection.ref_id as collection, 
  sample.ref_id as sample, 
  h.date_in 
from 
  Extent collection, 
  Extent sample, 
  ctm_reference r, 
  ctm_reference_history h, 
  ctm_reference_status s 
where 
  h.ctm_reference_status_id=s.ctm_reference_status_id and 
  r.ctm_reference_id=h.ctm_reference_id and 
  sample.Extent_id=CONVERT(NUMERIC,r.value) and 
  sample.parent_id=collection.Extent_id and 
  collection.ref_id NOT IN ('XX') and 
  s.name="Published" and 
  h.date_in > 'Dec 31 2007' and 
  h.date_in < 'Jan 1 2009' 
order by h.date_in

'TASK_COUNT_YEAR'
select 
  ctm_task_type_id, 
  creation_date, 
  ctm_task_id 
from 
  ctm_task 
where 
  creation_date > 'Dec 31 2011' and 
  creation_date < 'Jun 15 2012' and 
  ctm_task_type_id = 1

'VALIDATION_Q'
select 
  distinct e.ref_id 
from 
  Extent e, 
  ctm_reference cr, 
  ctm_reference_history h
where 
  convert(numeric,cr.value)=e.Extent_id and 
  cr.ctm_reference_id=h.ctm_reference_id and 
  h.ctm_reference_status_id=66 and 
  h.date_in > "Aug 31 2009" and 
  h.date_in < "Oct 01 2009"

'VALIDATION_Q2'
select 
  collection.ref_id as collection, 
  sample.ref_id as sample, 
  h.date_in 
from 
  Extent collection, 
  Extent sample, 
  ctm_reference r, 
  ctm_reference_history h, 
  ctm_reference_status s 
where 
  h.ctm_reference_status_id=s.ctm_reference_status_id and 
  r.ctm_reference_id=h.ctm_reference_id and 
  sample.Extent_id=CONVERT(NUMERIC,r.value) and 
  sample.parent_id=collection.Extent_id and 
  collection.ref_id NOT IN ('XX') and 
  s.name="Validation" and 
  h.date_in > 'Jan 31 2009' and 
  h.date_in < 'Feb 28 2009' 
order by h.date_in

'MONTHLY' directory
select 
  distinct e.ref_id 
from 
  Extent e, 
  ctm_reference cr, 
  ctm_reference_history h
where 
  convert(numeric,cr.value)=e.Extent_id and 
  cr.ctm_reference_id=h.ctm_reference_id and 
  h.ctm_reference_status_id=68 and 
  h.date_in > "Aug 31 2009" and 
  h.date_in < "Oct 01 2009"

