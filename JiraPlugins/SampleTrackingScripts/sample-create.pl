#!/usr/local/bin/perl

=head1 NAME
    
    sample_create
    
=head1 USAGE

    sample_create [-e <env>] -f <tuple_file> -c "Comment"
    sample_create [-e <env>] -l <lot_number> -d <dbname> -c "Comment"

=head1 REQUIRED ARGUMENTS

=item [-]-c <comment>

=over

=head1 OPTIONS

=item [-]-l <lot_no>

Lot number

=for Euclid:

    lot_no.excludes: file
    lot_no.excludes.error: Either set a tuple file or lot & database


=item [-]-d <database>

GLK database name (giv)

=for Euclid:

    d.excludes: f
    d.excludes.error: Either set a tuple file or lot & database


Database name (e.g. giv)

=item [-]-f <file>

Path to CSV tuple file

=for Euclid:

    f.type:   readable
    f.excludes: d, l
    f.excludes.error: Either set a tuple file or lot & database
    
=item [-]-e <env>

Env to use (prod,dev). Default is prod

=for Euclid:

    env.type:   /env|prod/

=over

=back

=head1 DESCRIPTION

This script will bulk comment JIRA Sample tracking cases based on a tuple file for lot id & DB name

=cut

use strict;
use File::Temp qw/ tempfile tempdir /;;
use FindBin;
use lib "$FindBin::Bin/lib";
use lib "/usr/local/devel/VIRIFX/software/Elvira/perllib/TIGR";
use GLKLib;
use st_props;
use st_funcs;


use Log::Log4perl qw(:easy);
use Getopt::Euclid 0.2.4 qw(:vars);


##
## init logging
##
st_funcs::initLogging("sample-create");
 

our ($ARGV_f,$ARGV_e,$ARGV_d,$ARGV_l,$ARGV_c);

LOGDIE "Please choose tuple file or lot no & database.\n" if (!defined($ARGV_f) && !defined($ARGV_l) && !defined($ARGV_d));
LOGDIE "Please choose tuple file or lot no & database, but not both.\n" if (defined($ARGV_f) && (defined($ARGV_l) || defined($ARGV_d)));
LOGDIE "Please choose tuple file or lot no & database.\n" if (!defined($ARGV_f) && !defined($ARGV_l && !defined($ARGV_d)));
LOGDIE "Please choose both lot no & database.\n" if ((defined($ARGV_l) && !defined($ARGV_d)) || (!defined($ARGV_l) && defined($ARGV_d)));

LOGDIE "Cannot find java: $st_props::JAVA_CMD\n" unless ( -e $st_props::JAVA_CMD );

##
## Default to prod
##
my $env = "prod";

$env = $ARGV_e if (defined($ARGV_e));
my %props = %{ $st_props::props{$env} };

my %jira_fields = %st_props::jira_fields;
my %jira_fields_regex = %st_props::jira_fields_regex;
my %custom_field_ids = %st_props::custom_field_ids;

DEBUG "server = $props{jira_server}\n";


my ($csv_fh, $csv_filename) = st_funcs::makeTempfile("sample-create", "csv");
DEBUG "CSV file is $csv_filename\n";

##
## CSV Header
##
## Project,type,summary,customfield_10120,customfield_10121,customfield_10122,customfield_10100,customfield_10126,customfield_10123,customfield_10630,customfield_10731,customfield_10730,comment
##Sample Tracking,Sample,piv_COH3_42078,piv,COH3,42078,piv_COH3_42078,H1N2,NIGSP_CEIRS_UMN033_COH3_00001,COH301,1133149939901,COH3_batch1, "flooby"
##Sample Tracking,Sample,piv_SD_42445,piv,SD,42445,piv_SD_42445,H1N1,NIGSP_SD_00001,SD01,1133717043426,SD_batch1, "flooby"


print $csv_fh $jira_fields{"PROJECT"}.",";
print $csv_fh $jira_fields{"TYPE"}.",";
print $csv_fh $jira_fields{"SUMMARY"}.",";
for my $custom ("DATABASE", "COLLECTION_CODE_LOAD", "BAC_ID_LOAD", "SAMPLE_ID", "COMPUTED_SUBTYPE", "BLINDED_NUMBER", "LOT_LOAD", "EXTENT_ID_LOAD", "BATCH_ID_LOAD")
{
	#print $csv_fh "customfield_$custom_field_ids{$custom},";
	print $csv_fh "$jira_fields{$custom},";
}
print $csv_fh "$jira_fields{COMMENT}\n";

my $attribute_name = "jira_id";

my $add_count = 0;
my $glk = TIGR::GLKLib::newConnect($props{sybase_server}, "", $props{sybase_rw_user}, $props{sybase_rw_password});
$glk->setLogger(get_logger());


if (defined($ARGV_f))
{
	my %tuples;
	open(TFILE, "< $ARGV_f") || LOGDIE "Cannot open tuple file $ARGV_f.\n";
	while (<TFILE>)
	{
		chomp;
		my($db,$coll,$bac) = split(/,/);
		#DEBUG "reading $db $bac\n";
		push( @{$tuples{$db}}, $bac); 
	}
	close TFILE;



	##
	## Load & validation
	##
	foreach my $db ( keys %tuples )
	{
		$glk->changeDb($db);
		my @baclist = @{ $tuples{$db} };
		foreach my $bac ( @baclist )
		{
			my $tmpResult = st_funcs::getAttributesForSample($bac, $glk);
			my %attributes = %{$tmpResult};

			DEBUG "-----START TUPLE $db $bac ------------\n";

			foreach my $key ( st_props::GLK_BAC_ID, st_props::GLK_COLLECTION_CODE, st_props::GLK_LOT_ID, st_props::GLK_SAMPLE_NUMBER, 
						st_props::GLK_JIRA_ID, st_props::GLK_BLINDED_NUMBER, st_props::GLK_EXTENT_ID, 
						st_props::GLK_SUBTYPE, st_props::GLK_BATCH_ID, st_props::GLK_DEPRECATED )
			{
				DEBUG "$key => ".$attributes{$key}."\n";
			}
			DEBUG "-----END TUPLE $db $bac ------------\n";
			if (defined($attributes{(st_props::GLK_JIRA_ID)}) && $attributes{(st_props::GLK_JIRA_ID)} ne "")
			{
				DEBUG "Sample BAC ID $bac already exists in JIRA. Skipping...\n";
				print "Sample BAC ID $bac already exists in JIRA. Skipping...\n";
			}
			else
			{
				print $csv_fh "\"$st_props::JIRA_PROJECT\",";
				print $csv_fh "$st_props::JIRA_ISSUE_TYPE,";
				print $csv_fh $db."_".$attributes{(st_props::GLK_COLLECTION_CODE)}."_".$attributes{(st_props::GLK_BAC_ID)}.",";
				print $csv_fh "$db,";
				print $csv_fh "$attributes{(st_props::GLK_COLLECTION_CODE)},";
				print $csv_fh "$attributes{(st_props::GLK_BAC_ID)},";
				## Sample ID:
				print $csv_fh $db."_".$attributes{(st_props::GLK_COLLECTION_CODE)}."_".$attributes{(st_props::GLK_BAC_ID)}.",";
				print $csv_fh "$attributes{(st_props::GLK_SUBTYPE)},";
				print $csv_fh "$attributes{(st_props::GLK_BLINDED_NUMBER)},";
				print $csv_fh "$attributes{(st_props::GLK_LOT_ID)},";
				print $csv_fh "$attributes{(st_props::GLK_EXTENT_ID)},";
				print $csv_fh "$attributes{(st_props::GLK_BATCH_ID)},";
				print $csv_fh "\"$ARGV_c\"\n";

				$add_count++;

			}
		}
	}
}
elsif (defined($ARGV_d) && defined($ARGV_l))
{

	$glk->changeDb($ARGV_d);

    my $lot_extent_id = $glk->getExtentByTypeRef("LOT", $ARGV_l);

    LOGDIE "No Lot $ARGV_l found in DB $ARGV_d.\n" unless (defined($lot_extent_id));

#print "Lot extents = $lot_extent_id\n";

    my @extent_ids = $glk->getExtentChildrenByType($lot_extent_id, "SAMPLE");
    LOGDIE "No children found for Lot $ARGV_l in datavase $ARGV_d.\n" unless (@extent_ids && $#extent_ids > -1);

    foreach my $extent_id1 (@extent_ids)
    {
        foreach my $extent_id (@{ $extent_id1 })
        {
            DEBUG "processing child extent $extent_id\n";
			my $tmpResult = st_funcs::getAttributesForSampleExtentID($extent_id, $glk );
			my %attributes = %{$tmpResult};

			if (defined($attributes{(st_props::GLK_JIRA_ID)}) && $attributes{(st_props::GLK_JIRA_ID)} ne "")
			{
				my $id_text = "Extent $extent_id";
				$id_text = "BAC ID ($attributes{(st_props::GLK_BAC_ID)})" if (defined($attributes{(st_props::GLK_BAC_ID)}));
				DEBUG "Sample $id_text already exists in JIRA. Skipping...\n";
				print "Sample $id_text already exists in JIRA. Skipping...\n";
			}
			else
			{
				print $csv_fh "\"$st_props::JIRA_PROJECT\",";
				print $csv_fh "$st_props::JIRA_ISSUE_TYPE,";
				print $csv_fh $ARGV_d."_".$attributes{(st_props::GLK_COLLECTION_CODE)}."_".$attributes{(st_props::GLK_BAC_ID)}.",";
				print $csv_fh "$ARGV_d,";
				print $csv_fh "$attributes{(st_props::GLK_COLLECTION_CODE)},";
				print $csv_fh "$attributes{(st_props::GLK_BAC_ID)},";
				## Sample ID:
				print $csv_fh $ARGV_d."_".$attributes{(st_props::GLK_COLLECTION_CODE)}."_".$attributes{(st_props::GLK_BAC_ID)}.",";
				print $csv_fh "$attributes{(st_props::GLK_SUBTYPE)},";
				print $csv_fh "$attributes{(st_props::GLK_BLINDED_NUMBER)},";
				print $csv_fh "$attributes{(st_props::GLK_LOT_ID)},";
				print $csv_fh "$attributes{(st_props::GLK_EXTENT_ID)},";
				print $csv_fh "$attributes{(st_props::GLK_BATCH_ID)},";
				print $csv_fh "\"$ARGV_c\"\n";

				$add_count++;
			}
        }
    }


}
else
{
	exit;
}


close($csv_fh);

die "Nothing to add.\n" if ($add_count == 0);

my ($jira_user, $jira_password) = st_funcs::getJiraLogin();

my $command =  "$st_props::JAVA_CMD -jar $st_props::JIRA_CLI_JAR --action runFromCSV "
	."--file $csv_filename --common \"--action createIssue\" --continue "
	."--server $props{jira_server}  --password '$jira_password' --user '$jira_user'"
	." 2>&1"
	;

DEBUG "$command\n";

## 
## Result:
##
##Run: --action createIssue --summary "piv_COH3_42078" --project "Sample Tracking" --comment "flooby" --type "Sample" --custom "'customfield_10100:piv_COH3_42078','customfield_10730:COH3_batch1','customfield_10126:H1N2','customfield_10122:42078','customfield_10123:NIGSP_CEIRS_UMN033_COH3_00001','customfield_10120:piv','customfield_10121:COH3','customfield_10630:COH301','customfield_10731:1133149939901'"
##Issue ST-97149 created with id 681960. Comment added. URL: http://sampletracking-dev.jcvi.org:8380/browse/ST-97149
##
##Run: --action createIssue --summary "piv_SD_42445" --project "Sample Tracking" --comment "flooby" --type "Sample" --custom "'customfield_10100:piv_SD_42445','customfield_10730:SD_batch1','customfield_10126:H1N1','customfield_10122:42445','customfield_10123:NIGSP_SD_00001','customfield_10120:piv','customfield_10121:SD','customfield_10630:SD01','customfield_10731:1133717043426'"
##Issue ST-97150 created with id 681961. Comment added. URL: http://sampletracking-dev.jcvi.org:8380/browse/ST-97150

my $output = `$command`;
chomp $output;
if (${^CHILD_ERROR_NATIVE} == 0)
{
	DEBUG "$output\n";

	my $jira_updated = "";
	my $jira_count = "?";
	$jira_count = $1 if ($output =~ /(\d+) actions were successful/i);
	$jira_updated = "$jira_count records updated in JIRA.";

	## update GLK
	##
	my $glk_count = 0;
	my $extent_match = $jira_fields_regex{EXTENT_ID_LOAD}.":";
	DEBUG "EXTENT MATCH = $extent_match\n";
	my $db_match = $jira_fields_regex{DATABASE}.":";

    for my $line (split("\n\n",$output))
    {
		$line =~ s/^Run completed .*//;
		$line =~ s/\n/ /g;
        my ($extent_id, $jira_id, $database);
		$extent_id = $1 if ($line =~ /$extent_match(\w+)/i);
		$jira_id = $1 if ($line =~ /Issue\s+(\S+)\s+created/i);
		$database = $1 if ($line =~ /$db_match(\w+)/i);
		DEBUG "EXTENT = $extent_id\n";
		DEBUG "JIRA   = $jira_id\n";
		DEBUG "DB     = $database\n";
        if (defined($extent_id) && defined($jira_id) && defined($database))
		{
        	DEBUG "Set jira_id = $jira_id where extent_id = $extent_id in database $database\n";
			$glk->changeDb($database);
			$glk->setExtentAttribute($extent_id, "jira_id", $jira_id);
			++$glk_count;
		}
    }
	my $glk_updated = $glk_count." records updated in the GLK.";
	if ($glk_updated != $jira_count)
	{
		DEBUG "ERROR! Added $jira_count records, but updated $glk_updated GLK records!";
		print "ERROR! Added $jira_count records, but updated $glk_updated GLK records!";
	}
	else
	{
		DEBUG "Complete. $jira_updated $glk_updated\n";
		print "Complete. $jira_updated $glk_updated\n";
	}
}
else
{
	print "JIRA Error! Output = $output\n";
	DEBUG "JIRA Error! Output = $output\n";
}

