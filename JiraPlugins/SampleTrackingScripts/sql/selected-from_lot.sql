--Requires: #Selected(created) 
--          ${db}
--          ${lot}
--Produces: #Selected(filled)

--\if [ -z $lot ]
--\echo Please enter lot code to use
--\read lot
--\else
--\echo Lot: $lot
--\fi
--\set lot="BUCB01"
\set lot="$lot"

INSERT INTO #Selected
select Extent_id from ${db}..Extent 
where parent_id in 
  (Select Extent_id from ${db}..Extent where ref_id="${lot}")
--silent
go > /dev/null
