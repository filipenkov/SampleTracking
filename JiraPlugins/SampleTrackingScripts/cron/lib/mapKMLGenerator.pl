#!/usr/local/bin/perl -w
use strict;
use LWP::Simple; # required package
use JSON;
use Text::CSV_XS; # used for reading the locations cache
use FindBin '$Bin';

my ($cacheFile) = "$FindBin::Bin/../etc/locations.cache";
#standard illegible perl for reading a file into a variable
my $header = do { local( @ARGV, $/ ) = "$FindBin::Bin/../etc/header.kml" ; <> };
my $footer = do { local( @ARGV, $/ ) = "$FindBin::Bin/../etc/footer.kml" ; <> };

my (%countries, @newLocations);
#These are locations that the google maps lookup gets wrong
my (%locationCache) = (
    "USSR"           => ["OK","USSR","102.245","59.275","3"],
    "Korea"          => ["OK","Korea","128.2","38.2","3"],
    "Czechoslovakia" => ["OK","Czechoslovakia","18.1","49.125","3"],

    "Georgia"        => ["OK","Georgia","43.5","42.5","1"],
    "Total,USA"      => ["OK","United States","-95.712891","37.09024","1"],

    "Unknown,USA"    => ["IGNORE"],
    "Unknown"        => ["IGNORE"],

    #The state abbreviations don't work for these
    "CO,USA" => ["OK","Colorado, USA","-105.782067","39.550050","2"],
    "DC,USA" => ["OK","Washington, DC, USA","-77.0364641","38.9072309","2"],
    "LA,USA" => ["OK","Louisiana, USA","-92.1450244","31.2448234","2"],
    "RI,USA" => ["OK","Rhode Island, USA","-71.477429","41.580094","2"],
    "NY,USA" => ["OK","New York, USA","-75.485","42.7","2"],

    #This should be merged with Puerto Rico's results but this is a quick fix
    "PR,USA" => ["OK","Puerto Rico","-66.590149","18.220833",1],
  );

sub loadCache {
  my $csv = Text::CSV_XS->new();

  #cache file is in the format lookup, status, country name, lng, lat
  open (CACHEFILE, $cacheFile);
  my ($line) = 0;
  while(<CACHEFILE>) {
    $line++;    
    if (!($_ =~ "^#")) { #ignore comment lines
      if ($csv->parse($_)) {
        my @allFields = $csv->fields();    
        @{$locationCache{$allFields[0]}} = @allFields[ 1 .. $#allFields ];
      } else {
        my ($info1, $str, $info2) = $csv->error_diag ();
        print "Failed loading locations cache\n";    
        print "Could not parse line $line: $str\n";
        exit 1;
      }
    }
  }
}

sub askGoogle {
  my $country = $_[0];
  #try the cache first
  if ($locationCache{$country}) {
    return @{$locationCache{$country}};
  }

  my $url ="http://maps.googleapis.com/maps/api/geocode/json?&address=$country&sensor=true";
  my $result=get ($url);
  #don't flood google
  sleep 1;

  my $decode_result  = decode_json $result;
  if ( $$decode_result{status} ne "OK" ) {
    return "FAIL";
  }
  my $formatted_address = $$decode_result{results}[0]{formatted_address};
  my $type = $$decode_result{results}[0]{types}[0];
  my $lat = $$decode_result{results}[0]{geometry}{location}{lat};
  my $lng = $$decode_result{results}[0]{geometry}{location}{lng};
  my $style = 1;
  #States use type 2
  if ($formatted_address =~ m/USA/ && $type =~ m/administrative_area_level_1/) {
    $style = 2;
  }
  my @data = ["OK", $formatted_address, $lng, $lat, $style];
  #add it to the cache to save doing this multiple times
  @{$locationCache{$country}} = @data;
  #add it to newLocations so that the cacheFile can be updated
  push (@newLocations, [$country, "OK", $formatted_address, $lng, $lat, $style]);
  return @data;
}

sub processFile {
  my $csv = Text::CSV_XS->new();

  my $filename = $_[0]; 
  open (INPUT_FILE, $filename);
  my $header = <INPUT_FILE>;
  my $line = 0;
  while(<INPUT_FILE>) {
    $line++;
    chomp;
    if (!$csv->parse($_)) {
        my ($info1, $str, $info2) = $csv->error_diag ();
        print STDERR "ERROR Failed to read location from line $line: $str\n";
    } else { #line parsed ok
      my ($country, $state, $host, $number) = $csv->fields();
      my ($searchName);
      if ($country ne "USA") {
        $searchName = $country;
      } else {
        $searchName = $state . "," . $country;
      }
      my ($status, $formatted_address, $lng, $lat, $style) = askGoogle $searchName;
      if ($status eq "FAIL") {
        print STDERR "ERROR Failed to find: $_\n";
      } 
      if ($status eq "OK" && 
               !$countries{$formatted_address}) {
        print <<EOF;
  <Placemark>
    <name>$formatted_address</name>
    <description><\![CDATA[]]></description>
    <styleUrl>#style$style</styleUrl>
    <Point>
      <coordinates>$lng,$lat,0.000000</coordinates>
    </Point>
  </Placemark>
EOF
      }
      if ($formatted_address) {
        $countries{$formatted_address} = 1;
      }
    }
  }
  close (INPUT_FILE);
}

sub cacheUpdateMessage {
  for(@newLocations) {
    my $dataString = join ('","', @{$_});
    print "<!-- add to cache: \"$dataString\" -->\n";
    print STDERR "WARNING add to cache: \"$dataString\"\n";
  }
}

if ( scalar @ARGV > 0 ) {
  my $filename = $ARGV[0];  
  loadCache ();
  print $header;
  processFile $filename;
  cacheUpdateMessage ();
  print $footer;
} else {
    print "A file to read from is required as a parameter\n";
    exit 0;
}
