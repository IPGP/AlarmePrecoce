#!/usr/bin/perl -w

=head1 SCRIPT

    chkmsg.pl : Check Shakemaps files

=head1 ENVIRONNEMENT

    Linux

=head1 CONTEXT

    @project Shakemap acceleration, send SMS
    @author  Stephen Roselia <stephen.roselia@supinfo.com>
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

# --------------------------------------------------------------------------- #
# Variables
# --------------------------------------------------------------------------- #
#
# The home directory of the program
my $homeroot = "/home/sysop/.chkacc";

# The file in which processed events will be logged
my $history = "$homeroot/history.txt";

# The checkacc application binary
my $chkapp = "$homeroot/bin/chkacc";

# The checkacc application config file
my $chkconf = "$homeroot/conf/chkacc.cfg";

# The SMS (message) file in which alert will be written by the checkcc app
my $smsfile = "$homeroot/message.sms";

# The root folder of the XML files to be checked (export of scwfparam)
#my $xmlroot = "/home/sysop/.seiscomp3/log/shakemaps";
my $xmlroot = "/home/sysop/seiscomp3/share/shakemaps";

# The date format (usually YearMonthDay)
#my $date    = localtime->strftime('%Y%m%d');
my $date = '20140915';

# The pattern tag used in debug
my $idpattern = "[perl]";

# The MySQL user
my $dbuser = "modemuser";

# The MySQL user password
my $dbpasswd = "plokij";

# The MySQL database
my $dbname = "smsd";

#The MySQL table containing the messages
my $dbtable = "outbox";

my $dbhost = "195.83.190.51";
my $dbport = "3306";

# The phone number
#my $phone = "0696278622"; # Astreinte phone
my $phone = "0696289444"; # SR

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
print "$idpattern    +xmlroot : $xmlroot\n";
print "$idpattern -----------------------------------------------------\n";
print "\n";

## Fetch local day folder(s)
## >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
#opendir( DIR, $xmlroot ) or die $!;

## Get the content of the XML root folder
#my @folders = grep { -d "$xmlroot/$_" } read_dir($xmlroot);

## Select only the current day events
#@folders = grep /$date/, @folders;
#print "$idpattern Looking for folders that match pattern $date*\n";
#closedir(DIR);
## <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

# Iterate thru the list, do some work...
for my $f (@folders) {

	print "\n";
	print "$idpattern Found event $f\n";

	# Check the history, see if this event has been processed already
	my $found = 0;
	open( my $in, '<', $history )
	  or die "$idpattern Couldn't open file '$history' $!";
	while ( my $line = <$in> ) {
		if ( $line == $f ) {
			$found = 1;
			last;
		}
	}
	close($in);

	# If there is no record of the event, proceed with detection
	if ( $found eq 0 ) {
		print "$idpattern Event $f hasn't been processed yet.\n";

		# Process the event with chkacc app
		my $xmlfile = "$xmlroot/$f/input/event_dat.xml";
		print
"$idpattern Executing system call `$chkapp -i $xmlfile -c $chkconf`\n";
		system("$chkapp -i $xmlfile -c $chkconf");
		my $cmd = $? >> 8;
		print "$idpattern chkacc run retcode: $cmd\n";

		# Check the retcode of this run, see if an alarm has been raised
		if ( $cmd eq 0 ) {
			print "$idpattern No alarm has been raised by chkapp\n";
		}
		elsif ( $cmd eq 1 ) {
			print "$idpattern Received new alarm from chkacc\n";

			# Display the message content
			open( my $msg, '<', $smsfile )
			  or die "$idpattern Couldn't open file '$smsfile' $!";
			my @smslines = $msg;
			my $message  = "";
			while ( my $l = <$msg> ) {
				$message .= $l;
				print "$idpattern $l";
			}
			close($msg);

			# Send new acceleration message to mysql database
			my $db =
			  DBI->connect( "dbi:mysql:dbname=$dbname;host=$dbhost;port=$dbport", "$dbuser", "$dbpasswd",
				{ RaiseError => 1 } )
			  or die $DBI::errstr;

#			$db->do(
#				"INSERT INTO `$dbname`.`$dbtable` (`id`, `number`, `processed_date`,
#			`insertdate`, `text`, `phone`, `processed`, `error`, `dreport`, `not_before`, `not_after`)
#			VALUES (NULL, '$phone', NULL, NULL, '$message', NULL, '', '', '', '', '');"
#			);
			$db->do(
				"INSERT INTO `$dbname`.`$dbtable` (`number`, `text`) 
			VALUES ('$phone', '$message');"
			);
			$db->disconnect();
		}
		elsif ( $cmd eq 4 ) {
			print "$idpattern Received new alert(s) from chkacc:\n";
			print
"$idpattern motions detected on site(s) of interest but were to low,";
			print "$idpattern  no SMS shouldn't be sent yet.\n";
		}

		# Writing log about processed file...
		open( my $out, '>>', $history )
		  or die "$idpattern Could not open file '$history' $!";
		print $out "$f\n";
		close $out;
		print
"$idpattern Added new log entry about processed event $f in history\n";
	}
	else {
		print "$idpattern Ignoring already processed event $f\n";
	}
	print "\n";
}

print "$idpattern Run terminated, exiting...\n";
print "$idpattern Goodbye!\n";

exit 0;

