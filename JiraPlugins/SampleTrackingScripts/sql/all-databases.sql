--selects a list of all of the VGD databases
select db from common..genomes where type = 'VGD' order by db
go
