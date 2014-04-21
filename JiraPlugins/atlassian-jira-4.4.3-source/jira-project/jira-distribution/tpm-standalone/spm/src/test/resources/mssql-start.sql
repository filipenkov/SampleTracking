USE master
GO

-- Create the JIRA user.
CREATE LOGIN ${db.username} WITH PASSWORD=N'${db.password}', DEFAULT_DATABASE=master, CHECK_EXPIRATION=OFF, CHECK_POLICY=OFF
GO

-- Create the Database.
CREATE DATABASE ${db.instance}
GO

USE ${db.instance}
GO

-- Change the database owner.
ALTER AUTHORIZATION ON DATABASE::${db.instance} TO ${db.username}
GO

-- Create a schema for the new user in the database.
CREATE SCHEMA ${db.schema} AUTHORIZATION dbo
GO
