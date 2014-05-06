#!/usr/local/bin/perl

=head1 NAME
    
    get_tuple
    
=head1 USAGE

    get_tuple <JIRA_ST_ISSUE_ID>
    get_tuple -f <FILE_OF_JIRA_ST_ISSUE_IDS>

=head1 REQUIRED ARGUMENTS

=over

=head1 OPTIONS

=item<jira_id>

=item [-]-f[ile] <file_path>

Path to JIRA case name file

=for Euclid:

    file_path.type:   readable
    
=item [-]-e[nv] <env>

Env to use (prod,dev). Default is prod

=for Euclid:

    env.type:   /env|prod/

=over

=back

=head1 DESCRIPTION

This script will look up tuples for JIRA Sample Tracking cases IDS

=cut

use strict;
use FindBin;
use lib "$FindBin::Bin/lib";
use Getopt::Euclid 0.2.4 qw(:vars);
use st_props;
use st_funcs;


our ($ARGV_file,$ARGV_env,$ARGV_jira_id);

die "Usage:\n\tget_tuple -f <ST-file>\n\tget_tuple <ST-nnn> \n" if (!defined($ARGV_file) && !defined($ARGV_jira_id)) ;
die "Cannot find java: $st_props::JAVA_CMD\n" unless ( -e $st_props::JAVA_CMD );

my $env = "prod";
$env = $ARGV_env if (defined($ARGV_env));

# print "server = $st_props::props{$env}{jira_server}\n";
my %props = %{ $st_props::props{$env} };

my $jira_user = $props{jira_user};
my $jira_password = $props{jira_password};

sub get_one_result
{
	my $id = shift;
	my $result;

	my $command =  "$st_props::JAVA_CMD -jar $st_props::JIRA_CLI_JAR --quiet --action getFieldValue --issue \"$id\" --field \"Summary\" --server $props{jira_server}  --password '$jira_password' --user '$jira_user' 2>/dev/null";
	#print $command."\n";
	my $output = `$command`;
	chomp $output;
	#print "Output = $output\n";
        if (${^CHILD_ERROR_NATIVE} == 0)
	{
		$output =~ s/^'//;
		$output =~ s/'$//;
		$output =~ s/_/,/g;
                $result =  "$output,$id\n";
	}
        else
	{
               die "ERROR $id NOT FOUND\n";
        }

	$result;
}

my @results;
if (defined($ARGV_file))
{
	open(FILE,"< $ARGV_file") || die ("Cannot open $ARGV_file\n");
	while(<FILE>)
	{
		chomp;
		push(@results,get_one_result($_));
	}
	close FILE;

	for my $result (@results)
	{
		print $result;
	}
}
else
{
	print get_one_result($ARGV_jira_id);
}

