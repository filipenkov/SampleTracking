#!/bin/bash

# This shell script is run on the Open LDAP Lab Manager VM to configure the Open LDAP server ready for use by TPM tests.
# See http://www.redhat.com/docs/manuals/linux/RHL-9-Manual/ref-guide/s1-ldap-quickstart.html

echo Configuring OpenLDAP for use in JIRA TPM test

# make a copy of the original slapd.conf - could be useful for debugging
cp /etc/openldap/slapd.conf /etc/openldap/slapd.conf.orig

# Update the rootdn and suffix
sed 's/dc=my-domain,dc=com/o=tpm/' </etc/openldap/slapd.conf >slapd.conf.temp
# Update the root password
sed 's/# rootpw		secret/rootpw		secret/' <slapd.conf.temp >/etc/openldap/slapd.conf

# Cleanup the temp file
rm slapd.conf.temp

# Now restart the LDAP server to pick up the changes
echo Restarting LDAP with new values
service ldap restart

# Add the root entry for our tree:
ldapadd -h localhost -p 389 -D "cn=Manager,o=tpm" -w secret -f target/classes/addroot.ldif -x 