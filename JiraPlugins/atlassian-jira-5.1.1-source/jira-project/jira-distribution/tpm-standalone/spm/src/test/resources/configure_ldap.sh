#!/bin/bash

# This shell script is run on the Open LDAP Lab Manager VM to configure the Open LDAP server ready for use by TPM tests.
# See http://www.redhat.com/docs/manuals/linux/RHL-9-Manual/ref-guide/s1-ldap-quickstart.html

# CentOS 5.5 on Labmanager or test env
function labManager() {
        echo Configuring OpenLDAP for use in JIRA TPM test

        # make a copy of the original slapd.conf - could be useful for debugging
        cp /etc/openldap/slapd.conf /etc/openldap/slapd.conf.orig

        # Update the rootdn and suffix
        sed 's/dc=my-domain,dc=com/o=tpm/' </etc/openldap/slapd.conf >slapd.conf.temp
        # Update the root password
        sed 's/# rootpw         secret/rootpw           secret/' <slapd.conf.temp >/etc/openldap/slapd.conf

        # Cleanup the temp file
        rm slapd.conf.temp

        # Now restart the LDAP server to pick up the changes
        echo Restarting LDAP with new values
        service ldap restart

        # Add the root entry for our tree:
        ldapadd -h localhost -p 389 -D "cn=Manager,o=tpm" -w secret -f target/classes/addroot.ldif -x
}

# Fedora 15 Openldap on Amazon EC2
function ec2build() {
        # agent user has automatic sudo privs.  slapd.conf is already correct and installed via puppet
        # clean stock install of ldap directories
        sudo service slapd stop
        if [ $? != 0 ];then
                echo ">>> FAILURE: slapd service was unable to stop!"
        fi

        echo "Showing slapd status after stopping..."
        sudo service slapd status


        sudo ls -l /etc/openldap/slapd.d/
        echo "Cleaning stock slapd.d directory and making new one otherwise you can't make new ldap dirs..."
        if [ -d /tmp/slapd.d ];then
                sudo rm -rf /tmp/slapd.d
        fi
        if [ -d /tmp/ldap ];then
                sudo rm -rf /tmp/ldap
        fi
        # sudo isnt able to rm the dirs so need to mv
        sudo mv /etc/openldap/slapd.d /tmp
        sudo mv /var/lib/ldap /tmp
        sudo mkdir /etc/openldap/slapd.d /var/lib/ldap
        sudo chown ldap:ldap /etc/openldap/slapd.d /var/lib/ldap
        sudo chmod 700 /etc/openldap/slapd.d /var/lib/ldap

        echo "---------------------------"
        sudo ls -l /etc/openldap/slapd.d/
        sudo ls -l /var/lib/ldap
        echo "INFO: Starting slapd..."
        sudo service slapd start
        if [ $? != 0 ];then
                echo ">>> FAILURE: slapd service was unable to start!"
        fi
	# make sure slapd has enough time to properly startup
	sleep 30
        echo "INFO: Displaying slapd status..."
        sudo service slapd status
        # add test directory, the ldif is installed via puppet on ec2
        sudo ldapadd -h localhost -p 389 -D "cn=Manager,o=tpm" -w secret -f target/classes/addroot.ldif
}

if [ `hostname |grep ^ec2` ];then
        echo "INFO: Executing ec2 based build..."
        ec2build
else
        echo "INFO: Executing Labmanager based build..."
        labManager
fi

