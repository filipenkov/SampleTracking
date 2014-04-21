#!/usr/local/bin/perl

=head1 NAME
    
    sample_comment
    
=head1 USAGE

    sample_comment [-e <env>] -f <tuple_file> -c "Comment"
    sample_comment [-e <env>] -l <lot_number> -d <dbname> -c "Comment"

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
use lib "/usr/local/devel/VIRIFX/software/Elvira/perllib/TIGR";
use GLKLib;
use FindBin;
use lib "$FindBin::Bin/lib";
use File::Temp qw/ tempfile tempdir /;;
use st_props;
use st_funcs;
use Log::Log4perl qw(:easy);
use Getopt::Euclid 0.2.4 qw(:vars);

our ($ARGV_f,$ARGV_e,$ARGV_l,$ARGV_c);

##
## init logging
##
st_funcs::initLogging("sample-comment");

LOGDIE "Usage:\n\tsample_comment -t <tuple-file> -c \"Comment to add\" [-e env]\n\tsample_comment -l <lot-number> -c \"Comment to add\" [-e env]\n" if (!defined($ARGV_f) && !defined($ARGV_l)) ;

LOGDIE "Cannot find java: $st_props::JAVA_CMD\n" unless ( -e $st_props::JAVA_CMD );
##
## Change to prod later:
##
my $env = "prod";

$env = $ARGV_e if (defined($ARGV_e));
my %props = %{ $st_props::props{$env} };

DEBUG "server = $props{jira_server}\n";


my ($csv_fh, $csv_filename) = st_funcs::makeTempfile("sample-assign", "csv");
DEBUG "CSV file is $csv_filename\n";

##
## CSV Header
##
print $csv_fh "issue,comment\n";

my $attribute_name = "jira_id";

LOGDIE "Please choose tuple file or lot no & database, but not both.\n" if (defined($ARGV_f) && (defined($ARGV_l) || defined($ARGV_d)));
LOGDIE "Please choose tuple file or lot no & database\n" if (!defined($ARGV_f) && (!defined($ARGV_l) && !defined($ARGV_d)));

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



	foreach my $db ( keys %tuples )
	{
		my $glk = TIGR::GLKLib::newConnect($props{sybase_server}, $db, $props{sybase_ro_user}, $props{sybase_ro_password});
		$glk->changeDb($db);

		my @baclist = @{ $tuples{$db} };
		foreach my $bac ( @baclist )
		{
			#print "processing $db $bac\n";
			my $extent_id = $glk->getExtentByTypeRef( "SAMPLE", $bac ) ;
			if (defined($extent_id))
			{
				my $attribute = $glk->getExtentAttribute($extent_id, $attribute_name);
				#print "($db,$bac) = $attribute\n";
				print $csv_fh "$attribute,\"$ARGV_c\"\n";
			}
			else
			{
				LOGDIE "ERROR: Cannot find $attribute_name for BAC ID $bac in database $db\!\n";
			}
		}
	}
}
elsif (defined($ARGV_l))
{
	LOGDIE "-l Lot number also requires -d DB name parameter.\n" unless (defined($ARGV_d));
	my $glk = TIGR::GLKLib::newConnect($props{sybase_server}, $ARGV_d, $props{sybase_ro_user}, $props{sybase_ro_password});

	my $lot_extent_id = $glk->getExtentByTypeRef("LOT", $ARGV_l);

	LOGDIE "No Lot $ARGV_l found in DB $ARGV_d.\n" unless (defined($lot_extent_id));

#print "Lot extents = $lot_extent_id\n";

	my @extent_ids = $glk->getExtentChildrenByType($lot_extent_id, "SAMPLE");
	LOGDIE "No children found for Lot $ARGV_l in datavase $ARGV_d.\n" unless (@extent_ids && $#extent_ids > -1);

	foreach my $extent_id1 (@extent_ids)
	{
		foreach my $extent_id (@{ $extent_id1 })
		{	
			DEBUG "processing child $extent_id\n";

			my $jira_id = $glk->getExtentAttribute($extent_id, $attribute_name);
			if (defined($jira_id))
			{
				print $csv_fh "$jira_id,\"$ARGV_c\"\n";
			}
			else
			{
				LOGDIE "ERROR: Cannot find jira_id for extent $extent_id for lot $ARGV_l in database $ARGV_d\!\n";
			}
		}
	}
}

close($csv_fh);

my $command =  "$st_props::JAVA_CMD -jar $st_props::JIRA_CLI_JAR --quiet --action runFromCSV "
	."--file $csv_filename --common \"--action addComment\" --continue "
	."--server $props{jira_server}  --password $props{jira_password} --user $props{jira_user}"
	." 2>&1"
	;

DEBUG "$command\n";

my $output = `$command`;
chomp $output;
if (${^CHILD_ERROR_NATIVE} == 0)
{
	my $updated = "";
	$updated = " $1 records updated." if ($output =~ /(\d+) actions were successful/i);
	print "Complete.$updated\n";
	DEBUG "$output\n";
	DEBUG "Complete.$updated\n";
}
else
{
	print "Error! Output = $output\n";
	DEBUG "Error! Output = $output\n";
}

