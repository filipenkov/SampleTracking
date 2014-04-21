--Outputs a list of tuples, one per published sample in the databases

--requires 
--  #Published_Samples(Extent_id,db,collection_code,bac_id) -- find-all-published.sql
--Produces:
--  A list of tuples, with no header
--  <db>,<collection_code>,<bac_id>


SELECT LTRIM(RTRIM(p.db)), 
       LTRIM(RTRIM(p.collection_code))  AS collection_code, 
       LTRIM(RTRIM(p.bac_id))  AS bac_id
FROM #Published_Samples p
go -m csv
