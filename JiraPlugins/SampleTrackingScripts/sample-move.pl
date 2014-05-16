#!/usr/local/bin/perl

##
##  Move a sample to another JIRA state
##

=head1 NAME
    
    sample_move
    
=head1 USAGE

    sample_move [-e <env>] -f <tuple_file> -c "Comment"
    sample_move [-e <env>] -l <lot_number> -d <dbname> -c "Comment"

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
use Term::ReadKey;

our ($ARGV_f,$ARGV_e,$ARGV_l,$ARGV_c,$ARGV_d);

##
## init logging
##
st_funcs::initLogging("sample-move");

LOGDIE "Usage:\n\tsample_move -t <tuple-file> -c \"Comment to add\" [-e env]\n\tsample_move -l <lot-number> -c \"Comment to add\" [-e env]\n" if (!defined($ARGV_f) && !defined($ARGV_l)) ;

LOGDIE "Cannot find java: $st_props::JAVA_CMD\n" unless ( -e $st_props::JAVA_CMD );
##
## Change to prod later:
##
my $env = "prod";

$env = $ARGV_e if (defined($ARGV_e));

my %props = %{ $st_props::props{$env} };

DEBUG "server = $props{jira_server}\n";


my ($csv_fh, $csv_filename) = st_funcs::makeTempfile("sample-move-lookup", "csv");
get_logger()->debug( "CSV file is $csv_filename\n");

##
## CSV Header
##
print $csv_fh "issue,comment\n";

my $attribute_name = "jira_id";

LOGDIE "Please choose tuple file or lot no & database, but not both.\n" if (defined($ARGV_f) && (defined($ARGV_l) || defined($ARGV_d)));
LOGDIE "Please choose tuple file or lot no & database.\n" if (!defined($ARGV_f) && !defined($ARGV_l) && !defined($ARGV_d));

my @issueList;

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
	## Returns map of bac_id => ST_XXXX issue
	##
	my %issueHash = st_funcs::getIssuesForTuples($env, %tuples);
	@issueList = values %issueHash ;
}
elsif (defined($ARGV_l))
{
	LOGDIE "-l Lot number also requires -d DB name parameter.\n" unless (defined($ARGV_d));
	@issueList = st_funcs::getIssuesForDbLot($env,$ARGV_d, $ARGV_l);
}

die "No issues found.\n" unless (@issueList);
foreach my $issue (@issueList)
{
	print $csv_fh "$issue,\'$ARGV_c\'\n";
}

close($csv_fh);

my ($jira_user, $jira_password) = st_funcs::getJiraLogin();

##java -jar /usr/local/devel/VIRIFX/software/SampleTracking/lib/jira-cli.jar --quiet --common '--action getAvailableSteps' -a runFromCSV   --file /usr/local/scratch/VIRAL/ST/sample-assign_lserver2_23960.csv --server http://sampletracking-dev.jcvi.org:8380  --password 'Andie2k3!' --user 'bbishop'


my $get_steps_command =  "$st_props::JAVA_CMD -jar $st_props::JIRA_CLI_JAR --quiet -a runFromCSV "
	."--common '--action getAvailableSteps'  "
	."--file $csv_filename  --continue "
	."--server $props{jira_server}  --password '$jira_password' --user '$jira_user'"
	." 2>&1"
	;

DEBUG "$get_steps_command\n";

my $repeat_step = "Y";

while ("Y" eq $repeat_step)
{
	print "Looking up steps....\n";
	my $output = `$get_steps_command`;
	chomp $output;
	if (${^CHILD_ERROR_NATIVE} == 0)
	{
		my %selections;
		foreach my $line (split("\n",$output))
		{
			chomp $line;
			if ($line =~ /.*,.*/ && $line !~ /^"ID"/i)
			{
				$selections{$line}++;
			}
		}
		my @validSelections;
		foreach my $choice (sort keys %selections)
		{
			if ($selections{$choice} == $#issueList + 1)
			{
					$choice =~ s/"//g;
					push(@validSelections,$choice);
			}
		}
	
		if (@validSelections)
		{
			print "Choose a step (name or number):\n";
			print join "\n",@validSelections;
			print "\n\nSelect: ";
	
			my $new_state = ReadLine(0);
    		chomp $new_state;
	
			my $reject_reason_command = "";
			if ($new_state =~ /9300/ || $new_state =~ /Unresolved/i)
			{
				my $reject_reason;
				print "Please give a reason for rejecting this sample: ";
				$reject_reason = ReadLine(0);
    			chomp $reject_reason;
				$reject_reason_command = "--field \"$st_props::jira_fields{REASON_FOR_REJECTING}\" --values \"$reject_reason\"";
			}
	

##jira-cli.jar --action progressIssue --issue "ST-99166" --comment "test unresolved" --step "9300" --server http://sampletracking-dev.jcvi.org:8380 --user bbishop --password  'Andie2k3!' --field 'Reason for rejecting sample'  --values 'Reject reason 2 here'


			my $move_steps_command =  "$st_props::JAVA_CMD -jar $st_props::JIRA_CLI_JAR --action runFromCSV "
				."--common '--action progressIssue --step \"$new_state\" $reject_reason_command' "
				."--file $csv_filename  --continue "
				."--server $props{jira_server}  --password '$jira_password' --user '$jira_user'"
				." 2>&1"
				;

				DEBUG "$move_steps_command\n";

				print "Moving....\n";
    			my $output = `$move_steps_command`;
    			chomp $output;
				DEBUG "$output\n";
				if (${^CHILD_ERROR_NATIVE} == 0)
    			{

    				my $jira_updated = "";
    				my $jira_count = "?";
    				$jira_count = $1 if ($output =~ /(\d+) actions were successful/i);
    				$jira_updated = "$jira_count records moved in JIRA.";

					DEBUG "Complete. $jira_updated\n";
        			print "Complete. $jira_updated\n";
				}
				else
				{
    				LOGDIE "JIRA Error! Output = $output\n";
				}
		}
		else
		{
			LOGDIE "No common steps found.\n";
		}
	}
	else
	{
		LOGDIE "Error looking up steps! Output = $output\n";
	}
	print "Move them again (Y/N)? ";
	$repeat_step = ReadLine(0);
   	chomp $repeat_step;
	$repeat_step = uc $repeat_step;
}
