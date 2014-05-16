package st_props;
use strict;


our $JIRA_CLI_JAR 	= "/usr/local/devel/VIRIFX/software/SampleTracking/lib/jira-cli.jar";
our $JAVA_CMD 	        = "/usr/local/java/1.7.0/bin/java";

our $LOG_DIR = "/usr/local/scratch/VIRAL/ST";

our %props = (
	dev => {
                    jira_server    		=> "http://sampletracking-dev.jcvi.org:8380",
                    jira_user  			=> "sampletracking",
                    jira_password  		=> "a2c4e6g8",
                    sybase_server		=> "SYBIL"	,
					sybase_ro_user  	=> "access",
					sybase_ro_password  => "access",
					sybase_rw_user  	=> "glk_admin",
					sybase_rw_password  => "glk_admin99"
                   },

	prod => {
                    jira_server    		=> "http://sampletracking.jcvi.org",
                    jira_user			=> "sampletracking",
                    jira_password  		=> "a2c4e6g8",
					sybase_server		=> "SYBPROD",
					sybase_ro_user  	=> "access",
					sybase_ro_password  => "access",
					sybase_rw_user  	=> "glk_admin",
					sybase_rw_password  => "glk_admin99"
                   }
	);


##
## JIRA Fields
##
our $JIRA_PROJECT		= "Sample Tracking";
our $JIRA_ISSUE_TYPE	= "Sample";

our %jira_fields = (

	PROJECT				=>	"Project",
	TYPE				=>	"Type",
	SUMMARY				=>	"Summary",
	COMMENT				=>	"Comment",
	## Custom
	SAMPLE_ID			=> 	"Sample Id",
	DATABASE 			=> 	"Database",
	COLLECTION_CODE_LOAD	=>	"Collection Code (load)",
	BAC_ID_LOAD			=>	"BAC Id (load)",
	BLINDED_NUMBER		=>	"Blinded Number",
	COMPUTED_SUBTYPE	=>	"Computed Subtype",
	LOT_LOAD			=>	"Lot (load)",
	BATCH_ID_LOAD		=>	"Batch Id (load)",
	EXTENT_ID_LOAD		=>	"Extent Id (load)",
	REASON_FOR_REJECTING	=>	"Reason for rejecting sample"
);

our %jira_fields_regex = (

	PROJECT				=>	"Project",
	TYPE				=>	"Type",
	SUMMARY				=>	"Summary",
	COMMENT				=>	"Comment",
	## Custom
	SAMPLE_ID			=> 	"Sample Id",
	DATABASE 			=> 	"Database",
	COLLECTION_CODE_LOAD	=>	"Collection Code \Q(load)\E",
	BAC_ID_LOAD			=>	"BAC Id \Q(load)\E",
	BLINDED_NUMBER		=>	"Blinded Number",
	COMPUTED_SUBTYPE	=>	"Computed Subtype",
	LOT_LOAD			=>	"Lot \Q(load)\E",
	BATCH_ID_LOAD		=>	"Batch Id \Q(load)\E",
	EXTENT_ID_LOAD		=>	"Extent Id \Q(load)\E",
	REASON_FOR_REJECTING	=>	"Reason for rejecting sample"
);

our %custom_field_ids = (

	SAMPLE_ID					=>	10100,
	DATABASE					=>	10120,
	COLLECTION_CODE_LOAD		=>	10121,
	BAC_ID_LOAD					=>	10122,
	BLINDED_NUMBER				=>	10123,
	COMPUTED_SUBTYPE			=>	10126,
	LOT_LOAD					=>	10630,
	BATCH_ID_LOAD				=>	10730,
	EXTENT_ID_LOAD				=>	10731

);

## 
## GLK columns/props
##
## What we care about:
##
##-- bac_id          VARCHAR(36),  --e.g. 38340
##-- collection_code VARCHAR(36),  --e.g. RFH3
##-- lot_id          VARCHAR(36),  --e.g. RFH301
##-- sample_number   VARCHAR(128), --e.g. 00282
##-- jira_id         VARCHAR(128), --e.g. ST-1599
##-- blinded_number  VARCHAR(128), --e.g. NIGSP_CEIRS_CIP047_RFH3_00282 [Sample Creation Only]
##-- extent_id       NUMERIC(20,0),--e.g. 1128822655440                 [Sample Creation Only]
##-- subtype        VARCHAR(128), --e.g. H3N2                          [Sample Creation Only]
##-- batch_id        VARCHAR(128), --e.g.                               [Sample Creation Only]
##-- deprecated
##
use constant GLK_BAC_ID             => "bac_id";
use constant GLK_COLLECTION_CODE    => "collection_code";
use constant GLK_LOT_ID             => 'lot_id';
use constant GLK_SAMPLE_NUMBER      => "sample_number";
use constant GLK_JIRA_ID            => "jira_id";
use constant GLK_BLINDED_NUMBER     => "blinded_number";
use constant GLK_EXTENT_ID          => "extent_id";
use constant GLK_SUBTYPE            => "subtype";
use constant GLK_BATCH_ID           => "batch_id";
use constant GLK_DEPRECATED         => "deprecated";
use constant GLK_REF_ID				=> "ref";


1;
