#!/bin/bash

###############################################################################
#Combines the output of flu_samples_by_country_state_type.sh with the
#KML generator in lib/mapKMLGenerator.pl to create the files
###############################################################################
#The component programs should be examined to discover any dependencies
#and or requirements

#get the directory that this script is in, needed for all referenced files
DIR="$( cd "$( dirname "$0" )" && pwd )/"

if [ "$#" == "0" ]; then
  echo "Useage $0 <db>+"
  echo "A space seperated list of databases to include in the process is required"
  echo "The kml is returned to standard out and can be re-directed to generate a file"
  echo "For Influenza use: "
  echo "$0 giv giv2 giv3 piv swiv > output.kml"
  echo "For Rotavirus use: "
  echo "$0 rtv > output.kml"
  exit 1
fi
$DIR/lib/mapKMLGenerator.pl <($DIR/list-samples-by-location-and-subtype.sh "$@";)
