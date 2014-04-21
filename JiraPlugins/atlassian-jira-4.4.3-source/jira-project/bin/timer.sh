#!/bin/bash
#
# Adapted from http://www.linuxjournal.com/content/use-date-command-measure-elapsed-time
#
# Elapsed time.  Usage:
#
#   t=$(timer)
#   ... # do something
#   printf 'Elapsed time: %s\n' $(timer $t)
#      ===> Elapsed time: 0:01:12
#
#
#####################################################################
# If called with no arguments a new timer is returned.
# If called with arguments the first is used as a timer
# value and the elapsed time is returned in the form HH:MM:SS.
#
function timer()
{
    if [[ $# -eq 0 ]]; then
        echo $(date '+%s')
    else
        local  stime=$1
        etime=$(date '+%s')

        if [[ -z "$stime" ]]; then stime=$etime; fi

        dt=$((etime - stime))
        printf '%d' $dt
    fi
}

function to_time_str() {
    dt=$1
    ds=$((dt % 60))
    dm=$(((dt / 60) % 60))
    dh=$((dt / 3600))
	if [ $dh != 0 ]; then
		printf '%dh%dm%ds' $dh $dm $ds
	elif [  $dm != 0 ]; then
		printf '%dm%ds' $dm $ds
	else
		printf '%ds' $ds
	fi
}
