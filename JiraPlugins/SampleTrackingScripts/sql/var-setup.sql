--Requires: $db, $project
--Produces: ${db} ${project}
--\read was used previously, but dropped to allow the script to 
--be run using -i

\if [ -z "$db" ]
\echo Missing Database parameter
quit
\else
--The variable needs copying into the local env before it can be referenced by ${}!
\set db="$db"
\fi
--\set db="giv2"

\if [ -z "$project" ]
\echo Missing Project parameter
quit
\else
--The variable needs copying into the local env before it can be referenced by ${}!
\set project="$project"
\fi
--\set project="Sample Tracking"

--Used to allow semi-colon to be used instead of 'go'
\set semicolon_hack=1
