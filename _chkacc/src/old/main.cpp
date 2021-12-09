/************************************************************************
 *                                                                      *
 * Copyright (C) 2012 OVSM/IPGP                                         *
 *                                                                      *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or    *
 * (at your option) any later version.                                  *
 *                                                                      *
 * This program is distributed in the hope that it will be useful,      *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of       *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
 * GNU General Public License for more details.                         *
 *                                                                      *
 * This program is part of 'Projet TSUAREG - INTERREG IV Caraïbes'.     *
 * It has been co-financed by the European Union and le Minitère de     *
 * l'Ecologie, du Développement Durable, des Transports et du Logement. *
 *                                                                      *
 ************************************************************************/

#include <string>
#include <vector>
#include <stdio.h>

#include "lib/configfile/configfile.h"
#include "lib/tclap/CmdLine.h"
#include "lib/tools.hpp"
#include "lib/xmlshakemapparser.h"


using namespace std;
using namespace TCLAP;
using namespace IPGP::Core;



//! Global variables
int minStationAlert = -1;
float minStationAcc = .0;
bool debug = false;
string configFile, inputFile, outputFile, stations, polygons;



// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
bool parseOptions(int argc, char** argv);
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
int main(int argc, char** argv) {

	int retCode = EXIT_SUCCESS;

	if ( parseOptions(argc, argv) ) {

		if ( !fileExists(inputFile) ) {
			cerr << "Input file " << inputFile << " wasn't found, exiting" << endl;
			return 5;
		}

		if ( !fileExists(configFile) ) {
			cerr << "Configuration file " << configFile << " wasn't found, exiting" << endl;
			return 5;
		}

		try {
			ConfigFile conf(configFile);
			conf.readInto(stations, "stations");
			conf.readInto(minStationAlert, "minStationAlert");
			conf.readInto(minStationAcc, "minStationAcc");
			conf.readInto(outputFile, "outputFile");
			conf.readInto(polygons, "polygons");
			conf.readInto(debug, "debug");
		}
		catch ( ... ) {
			cerr << "Coudn't read configuration values from file `" << configFile << "`" << endl;
			return EXIT_FAILURE;
		}

		//! Handle the station's blacklist
		stations = stripBlankSpace(stations);
		vector<string> stationList;
		stringExplode(stations, ",", &stationList);

		//! Handle the allowed regions polygons (rectangles)
		polygons = stripBlankSpace(polygons);
		vector<string> tmp;
		stringExplode(polygons, ",", &tmp);

		PolygonList polygonList;
		for (size_t i = 0; i < tmp.size(); ++i) {

			vector<string> tmp2;
			stringExplode(tmp[i], "|", &tmp2);
			if ( tmp2.size() == 4 )
				polygonList.push_back(Polygon(stringToFloat(tmp2[0]), stringToFloat(tmp2[1]),
				    stringToFloat(tmp2[2]), stringToFloat(tmp2[3])));
		}

		if ( debug ) {

			cout << endl;
			cout << "Check Station Acceleration init..." << endl;
			cout << " *Blacklisted stations: " <<
			     ((stationList.size() > 0) ? stations : "none") << endl;
			cout << " *Authorized regions: " <<
			     ((polygonList.size() > 0) ? polygons : "none") << endl;
			cout << " *Min. station acceleration: " << minStationAcc << endl;
			cout << " *Min. station alert: " <<
			     ((minStationAlert > 0) ? toString(minStationAlert) : "any") << endl;
			cout << endl;
		}

		ParserSettings set;
		set.stations = stationList;
		set.polygons = polygonList;
		set.minStationAcc = minStationAcc;
		set.minStationAlert = minStationAlert;

		XMLShakemapParser parser(set);
		parser.processFile(inputFile);
		retCode = parser.returnCode();

		ofstream file;
		file.open(outputFile.c_str(), ios::out);
		switch ( retCode ) {
			//! Alarm raised
			case 1: {
				if ( !file.is_open() ) {
					cerr << "Failed to open file " << outputFile << " in write mode" << endl;
					break;
				}
				file << parser.message();
				file.close();
				cout << "New message written into file " << outputFile << endl;
			}
			break;

				//! XML reading error
			case 2:
				retCode = 2;
				cout << "XML file format not recognized, empy " << outputFile << " file written" << endl;
			break;

				//! File error (not found)
			case 3:
				cout << "XML file not found, empty " << outputFile << " file written" << endl;
			break;

				//! Alert(s) found but station count under min. alert
			case 4:
				cout << "Alert(s) found but station count under min. alert, empty "
				     << outputFile << " file written" << endl;
			break;

				//! No alarm raised
			default:
				cout << "No alarm raise by parser, empty " << outputFile << " file written" << endl;
			break;
		}
	}
	else {
		cerr << "Couldn't read configuration file from arguments" << endl;
	}

	return retCode;
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
bool parseOptions(int argc, char** argv) {

	bool retCode = false;

	try {
		CmdLine cmd("Check Station Acceleration", ' ', "1.0.0");

		ValueArg<string> configArg("c", "Configfile",
		    "Configuration file (with path)", true, "configfile", "string");
		cmd.add(configArg);

		ValueArg<string> inputFileArg("i", "Inputfile",
		    "Input XML file (with path)", true, "inputfile", "string");
		cmd.add(inputFileArg);

		cmd.parse(argc, argv);

		configFile = configArg.getValue();
		inputFile = inputFileArg.getValue();

		if ( !configFile.empty() && !inputFile.empty() )
			retCode = true;
	}
	catch ( ArgException& e ) {
		cerr << "ERROR: " << e.error() << " " << e.argId() << endl;
	}

	return retCode;
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

