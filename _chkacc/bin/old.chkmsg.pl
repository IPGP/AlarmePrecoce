#!/usr/bin/perl -w

=head1 SCRIPT

    chkmsg.pl : Check Shakemaps files

=head1 ENVIRONNEMENT

    Linux

=head1 CONTEXT

    @project Shakemap acceleration, send SMS
    @author  Stephen Roselia <stephen.roselia@supinfo.com>
    @brief   This script executes an external program, reads outputed values
             and injects thoses values inside a SQL table.
    Copyright (c) 2014 OVSM/IPGP. All rights reserved.  

=head1 HISTORY

    Revision 1.0  2014/08/21  sr
     o script creation

=cut

use strict;
use DBI;
use File::Find;
use Time::Piece;
use warnings;
use File::Slurp qw(read_dir);

## Check the argv, see if the script it executed if the correct parameters
my $total    = $#ARGV + 1;
my $argerror =
"Arguments error!\nTry 'perl chkmsg.pl eventID shakeEventID /xml/shakemap/path'\n"
  ;

if ( $total != 3 ) { die $argerror; }

## ------------------------------------------------------------------------##
## Script configuration
## ------------------------------------------------------------------------##
##
## The seiscomp database eventID
my $eventID = $ARGV[0];
##
## The seiscomp database shakemap eventID
my $shakemapEventID = $ARGV[1];
##
## The root folder of the XML files to be checked (given by argument N°3
## whenever scwfparam calls this script)
my $xmlpath = $ARGV[2];
##
## The pattern tag used in debug
my $idpattern = "[perl]";
##
## ------------------------------------------------------------------------##

## ------------------------------------------------------------------------##
## Script variables
## ------------------------------------------------------------------------##
##
## The home directory of the program
my $homeroot = "/home/sysop/.chkacc";
##
## The file in which processed events will be logged
my $history = "$homeroot/history.txt";
##
## The checkacc application executable
my $chkapp = "$homeroot/bin/chkacc";
##
## The checkacc application config file
my $chkconf = "$homeroot/conf/chkacc.cfg";
##
## The file in which SMS alert will be written by the checkcc application
my $smsfile = "$homeroot/message.sms";
##
## The MySQL user login
my $dbuser = "modemuser";
##
## The MySQL user password
my $dbpasswd = "plokij";
##
## The MySQL database
my $dbname = "smsd";
##
##The MySQL entity containing the messages
my $dbtable = "outbox";
##
## The MySQL server address
my $dbhost = "195.83.190.51";
##
## The MySQL server port
my $dbport = "3306";
##
## The phone numbers to which the SMS alert should be sent to
#my $phone = "0696278622"; # Astreinte's phone
my $phone = "0696289444";    # SR
my @phones = ("0696809966","0696800253","0696414842","0696441276", "0696278622"
              ,"0696289444");
##
## ------------------------------------------------------------------------##

print "$idpattern -----------------------------------------------------\n";
print "$idpattern |                                                   |\n";
print "$idpattern |               Check Shakemap message              |\n";
print "$idpattern |                                                   |\n";
print "$idpattern |                                                   |\n";
print "$idpattern |                                             v 1.0 |\n";
print "$idpattern -----------------------------------------------------\n";
print "\n";
print "$idpattern    +homeroot: $homeroot\n";
print "$idpattern    +history : $history\n";
print "$idpattern    +chkapp  : $chkapp\n";
print "$idpattern    +chkconf : $chkconf\n";
print "$idpattern    +smsfile : $smsfile\n";
print "$idpattern    +eventID : $eventID ($shakemapEventID)\n";
print "$idpattern    +xmlpath : $xmlpath\n";
print "$idpattern -----------------------------------------------------\n";
print "\n";

## Check the history, see if this event has been processed already
my $found = 0;
open( my $in, '<', $history )
  or die "$idpattern Couldn't open file '$history' $!";
while ( my $line = <$in> ) {
	if ( $line == $shakemapEventID ) {
		$found = 1;
		last;
	}
}
close($in);

## If there is no record of the event, proceed with detection
if ( $found eq 0 ) {
	print "$idpattern Event $shakemapEventID hasn't been processed yet.\n";

	## Process the event with chkacc app
	my $xmlfile = "$xmlpath/input/event_dat.xml";
	print
	  "$idpattern Executing system call `$chkapp -i $xmlfile -c $chkconf`\n";
	system("$chkapp -i $xmlfile -c $chkconf");
	my $cmd = $? >> 8;
	print "$idpattern chkacc run retcode: $cmd\n";

	## Check the retcode of this run, see if an alarm has been raised
	if ( $cmd eq 0 ) {
		print "$idpattern No alarm has been raised by chkapp\n";
	} elsif ( $cmd eq 1 ) {
		print "$idpattern Received new alarm from chkacc\n";

		## Display the message content
		open( my $msg, '<', $smsfile )
		  or die "$idpattern Couldn't open file '$smsfile' $!";
		my @smslines = $msg;
		my $message  = "";
		while ( my $l = <$msg> ) {
			$message .= $l;
			print "$idpattern $l";
		}
		close($msg);

		## Write new acceleration message into MySQL database entity
		my $db =
		  DBI->connect( "dbi:mysql:dbname=$dbname;host=$dbhost;port=$dbport",
			"$dbuser", "$dbpasswd", { RaiseError => 1 } )
		  or die "$idpattern Failed to connect to database: $DBI::errstr";

#			$db->do(
#				"INSERT INTO `$dbname`.`$dbtable` (`id`, `number`, `processed_date`,
#			`insertdate`, `text`, `phone`, `processed`, `error`, `dreport`, `not_before`, `not_after`)
#			VALUES (NULL, '$phone', NULL, NULL, '$message', NULL, '', '', '', '', '');"
#			);
		foreach ( @phones ) {
			$db->do(
				"INSERT INTO `$dbname`.`$dbtable` (`number`, `text`)
				VALUES ('$_', '$message');"
			);
		}
		$db->disconnect();
	
	} elsif ( $cmd eq 4 ) {
		print "$idpattern Received new alert(s) from chkacc:\n";
		print
		  "$idpattern motions detected on site(s) of interest but were to low,";
		print "$idpattern  no SMS should be sent yet.\n";
	}

	## Writing log about processed file...
	open( my $out, '>>', $history )
	  or die "$idpattern Could not open file '$history' $!";
	print $out "$shakemapEventID\n";
	close $out;
	print
"$idpattern Added new log entry about processed event $shakemapEventID in history\n";
} else {
	print "$idpattern Ignoring already processed event $shakemapEventID\n";
}
print "\n";

print "$idpattern Run terminated, exiting...\n";
print "$idpattern Goodbye!\n";

exit 0;

