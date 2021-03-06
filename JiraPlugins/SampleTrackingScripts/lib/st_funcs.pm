package st_funcs;
use strict;
use st_props;
use lib "/usr/local/devel/VIRIFX/software/Elvira/perllib/TIGR";
use GLKLib;
use Log::Log4perl qw(:easy);
use Sys::Hostname;
use Term::ReadKey;

my  $host = hostname;


##
## make sure our dir is here
##
mkdir $st_props::LOG_DIR, 0777;


my $logging_basename = "";
my $logger;

sub initLogging
{
	$logging_basename = shift @_;

	Log::Log4perl->easy_init( { level => $DEBUG, file => $st_props::LOG_DIR."/".$logging_basename."_".$host."_$$.log" });
	$logger = get_logger($logging_basename);
	$logger->info("Logging initiated for $logging_basename.");
}

sub makeTempfile
{
	my $basename = shift @_;
	my $extension = shift @_;

	my $filename = $st_props::LOG_DIR."/".$basename."_".$host."_$$.".$extension;

	open (my $fh, "> $filename") || LOGDIE "Cannot open temp file $filename.\n";

	($fh,$filename);
}

sub getJiraLogin
{
	print "Please enter the JIRA username to use: ";
	my $jira_user  = ReadLine(0);
	chomp $jira_user;
	print "Please enter the JIRA user's password to use: ";
	ReadMode('noecho');
	my $jira_password = ReadLine(0);
	chomp $jira_password;
	ReadMode('normal');
	print "\n";
	($jira_user, $jira_password );

}

sub getIssuesForTuples
{
	my $env = shift @_;
	my %tuples = @_;

	my %props = %{ $st_props::props{$env} };

	my %results;
	my $attribute_name = "jira_id";

  	my $glk = TIGR::GLKLib::newConnect($props{sybase_server}, "", $props{sybase_ro_user}, $props{sybase_ro_password});
    $glk->setLogger($logger);
    foreach my $db ( keys %tuples )
    {
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
                $results{$bac} = $attribute;
            }
            else
            {
                LOGDIE "ERROR: Cannot find $attribute_name for BAC ID $bac in database $db\!\n";
            }
        }
    }

	%results;
}

sub getIssuesForDbLot
{
 	my $env = shift @_ ;
 	my $db = shift @_ ;
 	my $lot = shift @_ ;

	my %props = %{ $st_props::props{$env} };

	my @results;
	my $attribute_name = "jira_id";

	my $glk = TIGR::GLKLib::newConnect($props{sybase_server}, $db, $props{sybase_ro_user}, $props{sybase_ro_password});
    $glk->setLogger(get_logger());

    my $lot_extent_id = $glk->getExtentByTypeRef("LOT", $lot);

    LOGDIE "No Lot $lot found in DB $db.\n" unless (defined($lot_extent_id));

	#print "Lot extents = $lot_extent_id\n";

    my @extent_ids = $glk->getExtentChildrenByType($lot_extent_id, "SAMPLE");
    LOGDIE "No children found for Lot $lot in datavase $db.\n" unless (@extent_ids && $#extent_ids > -1);

    foreach my $extent_id1 (@extent_ids)
    {
        foreach my $extent_id (@{ $extent_id1 })
        {
            DEBUG "processing child $extent_id\n";

            my $jira_id = $glk->getExtentAttribute($extent_id, $attribute_name);
            if (defined($jira_id))
			{
				push(@results, $jira_id);
			}
		}
	}

	@results;

}


sub getAttributesForSampleExtentID
{
        my $extent_id = shift ;
        my $glk = shift ;

	my %extent_info = %{ $glk->getExtentInfo($extent_id) };
	my $bac_id = $extent_info{(st_props::GLK_REF_ID)};
	LOGDIE "No BAC ID found for sample extent $extent_id" unless (defined($bac_id));

        my %attributes = %{ $glk->getExtentAttributes($extent_id) } ;


	DEBUG "---ATTR DUMP FOR BAC ID $bac_id---";
        foreach my $att_type ( keys %attributes)
        {
                #DEBUG "$att_type = $val\n";
                DEBUG "$att_type = $attributes{$att_type}\n";
        }

        ##
        ## Add BAC id
        ##
        $attributes{(st_props::GLK_BAC_ID)} = $bac_id;

        ##
        ## Add extent id
        ##
        $attributes{(st_props::GLK_EXTENT_ID)} = $extent_id;

        ##
        ## Add deprecated
        ##
        my $deprecated = 0;
        $deprecated = 1 if ($glk->isDeprecated($extent_id));
        DEBUG "deprecated = $deprecated\n";
        $attributes{(st_props::GLK_DEPRECATED)} = $deprecated;

        ##
        ## Add lot_id
        ##
        my $collection_id = "";
        my $lot_eid = $glk->getParentExtent($extent_id);
        my $lot_id = "";
        LOGDIE "No lot found for extent id $lot_eid.\n" unless (defined($lot_eid));
        my %bag = %{ $glk->getExtentInfo($lot_eid) } ;
        $lot_id = $bag{"ref"};
        $attributes{(st_props::GLK_LOT_ID)} = $lot_id;
        DEBUG st_props::GLK_LOT_ID." = $attributes{st_props::GLK_LOT_ID}\n";

        ##
        ## Add Collection Code
        ##
        DEBUG "LOT type = ".$bag{"type"}."\n";
        my $collection_eid = $glk->getParentExtent($lot_eid);
        LOGDIE "No collection found for extent id $collection_eid.\n" unless (defined($collection_eid)) ;
        %bag =  %{ $glk->getExtentInfo($collection_eid) };
        DEBUG "COLLECTION type = ".$bag{"type"}."\n";
        $collection_id = $bag{"ref"};
        $attributes{(st_props::GLK_COLLECTION_CODE)} = $collection_id;
        DEBUG st_props::GLK_COLLECTION_CODE." = $attributes{st_props::GLK_COLLECTION_CODE}\n";

	DEBUG "---END ATTR DUMP FOR BAC ID $bac_id---";

        \%attributes;

}

sub getAttributesForSample
{
        my $bac_id = shift ;
        my $glk = shift ;
        my $extent_id = $glk->getExtentByTypeRef( "SAMPLE", $bac_id ) ;

        LOGDIE "No sample found for BAC ID $bac_id.\n" unless (defined($extent_id));

	getAttributesForSampleExtentID( $extent_id, $glk );
}


sub getAttributesForSampleBacID
{
        my $bac_id = shift ;
        my $glk = shift ;
        my $extent_id = $glk->getExtentByTypeRef( "SAMPLE", $bac_id ) ;

        LOGDIE "No sample found for BAC ID $bac_id.\n" unless (defined($extent_id));

        my %attributes = %{ $glk->getExtentAttributes($extent_id) } ;

	DEBUG "---ATTR DUMP FOR BAC ID $bac_id---";
        foreach my $att_type ( keys %attributes)
        {
                #DEBUG "$att_type = $val\n";
                DEBUG "$att_type = $attributes{$att_type}\n";
        }

        ##
        ## Add BAC id
        ##
        $attributes{(st_props::GLK_BAC_ID)} = $bac_id;

        ##
        ## Add extent id
        ##
        $attributes{(st_props::GLK_EXTENT_ID)} = $extent_id;

        ##
        ## Add deprecated
        ##
        my $deprecated = 0;
        $deprecated = 1 if ($glk->isDeprecated($extent_id));
        DEBUG "deprecated = $deprecated\n";
        $attributes{(st_props::GLK_DEPRECATED)} = $deprecated;

        ##
        ## Add lot_id
        ##
        my $collection_id = "";
        my $lot_eid = $glk->getParentExtent($extent_id);
        my $lot_id = "";
        LOGDIE "No lot found for extent id $lot_eid.\n" unless (defined($lot_eid));
        my %bag = %{ $glk->getExtentInfo($lot_eid) } ;
        $lot_id = $bag{"ref"};
        $attributes{(st_props::GLK_LOT_ID)} = $lot_id;
        DEBUG st_props::GLK_LOT_ID." = $attributes{st_props::GLK_LOT_ID}\n";

        ##
        ## Add Collection Code
        ##
        DEBUG "LOT type = ".$bag{"type"}."\n";
        my $collection_eid = $glk->getParentExtent($lot_eid);
        LOGDIE "No collection found for extent id $collection_eid.\n" unless (defined($collection_eid)) ;
        %bag =  %{ $glk->getExtentInfo($collection_eid) };
        DEBUG "COLLECTION type = ".$bag{"type"}."\n";
        $collection_id = $bag{"ref"};
        $attributes{(st_props::GLK_COLLECTION_CODE)} = $collection_id;
        DEBUG st_props::GLK_COLLECTION_CODE." = $attributes{st_props::GLK_COLLECTION_CODE}\n";

	DEBUG "---END ATTR DUMP FOR BAC ID $bac_id---";

        \%attributes;
}



1;
