#!/usr/local/bin/perl -w
umask 002;
use strict;

use File::Basename;
my $SCRIPT = basename($0);

# #########################################################################
#
#    Version Strings
#
# #########################################################################
my $VERSION = "1.00";
my $BUILD = (qw/$Revision: Beta $/ )[1];

# #########################################################################
#
#    Load Modules
#
# #########################################################################
my $elviraDir;
#Setting must be done in the BEGIN block to occur before the use lib
#is interpreted
BEGIN {$elviraDir = "/usr/local/devel/VIRIFX/software/Elvira"; }

# Elvira Modules
use lib "$elviraDir/bin";
use lib "$elviraDir/perllib";
#
#  Standard USE statments
#
use Getopt::Long;

#
#  Load JavaRunner
#
use Java::Runner;


# #########################################################################
#
#    Global Variables
#
# #########################################################################

# #########################################################################
#
#    Set up the JavaRunner
#
# #########################################################################

my $runner = Java::Runner->new();

$runner->useJavaPreset("7");

$runner->clearClassPath();
$runner->addClassLocation("$elviraDir/resources");
$runner->addJarDirectory("$elviraDir/lib/");
$runner->addJarDirectory("$elviraDir/lib/apache-commons");
$runner->addJarDirectory("$elviraDir/lib/elvirautilities");
$runner->addJarDirectory("$elviraDir/lib/euid");
$runner->addJarDirectory("$elviraDir/lib/glklib");
$runner->addJarDirectory("$elviraDir/lib/guava");
$runner->addJarDirectory("$elviraDir/lib/hibernate");
$runner->addJarDirectory("$elviraDir/lib/jdbc");
$runner->addJarDirectory("$elviraDir/lib/jillion");
$runner->addJarDirectory("$elviraDir/lib/jillion");
$runner->addJarDirectory("$elviraDir/lib/jodatime");
$runner->addJarDirectory("$elviraDir/lib/log4j");
$runner->mainClass("org.jcvi.taxonomy.RequestTaxonIdCmd");
#pass all arguments thru as is
foreach my $arg (@ARGV){
	$runner->addParameters($arg);
}


# #########################################################################
#
#    Kick off the JavaRunner
#
# #########################################################################


    $runner->execute();


