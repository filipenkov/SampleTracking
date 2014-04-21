#!/usr/local/bin/bash
grep 'nav status\|data-issuekey' $1 | sed -e 's\^.*data-issuekey="\\g' -e 's\" class.*$\\g' -e 's\^.*>\\g' -e 's\[ \t]+$\\g' | sed -r '$!N;s/\n/,/'
