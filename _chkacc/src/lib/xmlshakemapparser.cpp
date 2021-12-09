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
 * It has been co-financed by the European Union and le Ministère de    *
 * l'Ecologie, du Développement Durable, des Transports et du Logement. *
 *                                                                      *
 ************************************************************************/

#include <stdio.h>
#include "xmlshakemapparser.h"
#include <libxml/parser.h>
#include <libxml/tree.h>
#include <libxml/xpath.h>
#include <libxml/xmlstring.h>
#include <iostream>
#include <map>
#include <ctime>
#include "tools.hpp"


using namespace std;

//! This shouldn't change any time soon... we can keep it non-configurable
static const size_t MAXSMSCHARS = 160;


namespace {

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
static string now() {

	time_t now = std::time(0);
	tm* now_tm = std::gmtime(&now);
	char buf[42];
	strftime(buf, 42, "%Y%m%d %X", now_tm);

	return buf;
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

}




namespace IPGP {
namespace Core {


// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
XMLShakemapParser::XMLShakemapParser(const ParserSettings& set,
                                     const bool& debug) :
		_settings(set), _retCode(0), _debug(debug) {}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
XMLShakemapParser::~XMLShakemapParser() {}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
void XMLShakemapParser::processEvent(const string& xmlfile) {

	if ( !fileExists(xmlfile) ) {
		_retCode = ERROR_1;
		return;
	}

	_currentEvent.reset();

	/*!
	 * Mechanism:
	 *   a) Store only stations inside defined polygon(s)
	 *   b) Remove blacklisted stations from the previously selected ones
	 *   c) Add remaining stations to SMS message if acc(STA) > 1.0mg
	 */

	_stations.clear();

	xmlDoc* doc = NULL;
	xmlNode* root_element = NULL;

	LIBXML_TEST_VERSION

	doc = xmlReadFile(xmlfile.c_str(), NULL, 0);

	if ( !doc ) {
		cerr << "Couldn't parse file " << xmlfile << endl;
		_retCode = ERROR_0;
		return;
	}

	root_element = xmlDocGetRootElement(doc);

	xmlNode* cur_node = NULL;
	for (cur_node = root_element; cur_node; cur_node = cur_node->next) {

		//! Only get the first node, it should be "earthquake"
		if ( cur_node->type == XML_ELEMENT_NODE &&
		        xmlStrcmp(cur_node->name, (const xmlChar *) "earthquake") )
		    continue;


		string datetime, lat, lon, depth, mag;
		//! Get the current earthquake information
		for (xmlAttrPtr attr = cur_node->properties; NULL != attr;
		        attr = attr->next) {

			if ( !xmlStrcmp(attr->name, (const xmlChar *) "id") )
			    _currentEvent.id = toString(attr->children->content);

			if ( !xmlStrcmp(attr->name, (const xmlChar *) "locstring") ) {
				string tmpstr = toString(attr->children->content);
				vector<string> tmp;
				stringExplode(tmpstr, "/", &tmp);
				try {
					_currentEvent.sc3id = stripBlankSpace(tmp[0]);
				} catch ( ... ) {
					_currentEvent.sc3id = "unset";
				}
			}

			if ( !xmlStrcmp(attr->name, (const xmlChar *) "lat") )
			    _currentEvent.latitude = stringToFloat(toString(attr->children->content));

			if ( !xmlStrcmp(attr->name, (const xmlChar *) "lon") )
			    _currentEvent.longitude = stringToFloat(toString(attr->children->content));

			if ( !xmlStrcmp(attr->name, (const xmlChar *) "depth") )
			    _currentEvent.depth = stringToFloat(toString(attr->children->content));

			if ( !xmlStrcmp(attr->name, (const xmlChar *) "mag") )
			    _currentEvent.magnitude = stringToFloat(toString(attr->children->content));


		}
	}

	if ( _debug ) {
		cout << "--------------------------------------------------------------" << endl;
		cout << "|                         New event                          |" << endl;
		cout << "--------------------------------------------------------------" << endl;
		cout << "Earthquake " << _currentEvent.sc3id << " at " << _currentEvent.id << endl;
		cout << "Latitude:  " << _currentEvent.latitude << endl;
		cout << "Longitude: " << _currentEvent.longitude << endl;
		cout << "Depth:     " << _currentEvent.depth << endl;
		cout << "Magnitude: " << _currentEvent.magnitude << endl;
		cout << "--------------------------------------------------------------" << endl;
	}

	xmlFreeDoc(doc);
	xmlCleanupParser();
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
void XMLShakemapParser::processEventData(const string& xmlfile) {

	if ( !fileExists(xmlfile) || _currentEvent.id.empty() ) {
		_retCode = ERROR_1;
		return;
	}

	/*!
	 * Mechanism:
	 *   a) Store only stations inside defined polygon(s)
	 *   b) Remove blacklisted stations from the previously selected ones
	 *   c) Add remaining stations to SMS message if acc(STA) > 1.0mg
	 */

	_stations.clear();

	xmlDoc* doc = NULL;
	xmlNode* root_element = NULL;

	LIBXML_TEST_VERSION

	doc = xmlReadFile(xmlfile.c_str(), NULL, 0);

	if ( !doc ) {
		cerr << "Couldn't parse file " << xmlfile << endl;
		_retCode = ERROR_0;
		return;
	}

	root_element = xmlDocGetRootElement(doc);

	xmlNode* cur_node = NULL;
	for (cur_node = root_element; cur_node; cur_node = cur_node->next) {

		//! Skip any node that isn't 'stationlist'
		if ( cur_node->type == XML_ELEMENT_NODE &&
		        xmlStrcmp(cur_node->name, (const xmlChar *) "stationlist") )
		    continue;

		xmlNode* sec_node = NULL;
		for (sec_node = cur_node->children; sec_node;
		        sec_node = sec_node->next) {

			//! Skip any node that isn't 'station'
			if ( xmlStrcmp(sec_node->name, (const xmlChar *) "station") )
			    continue;

			string code;
			string instType;
			float latitude = .0;
			float longitude = .0;

			//! Get the current station information
			for (xmlAttrPtr attr = sec_node->properties; NULL != attr;
			        attr = attr->next) {

				if ( !xmlStrcmp(attr->name, (const xmlChar *) "code") )
				    code = toString(attr->children->content);

				if ( !xmlStrcmp(attr->name, (const xmlChar *) "insttype") )
				    instType = toString(attr->children->content);

				if ( !xmlStrcmp(attr->name, (const xmlChar *) "lat") )
				    latitude = stringToFloat(toString(attr->children->content));

				if ( !xmlStrcmp(attr->name, (const xmlChar *) "lon") )
				    longitude = stringToFloat(toString(attr->children->content));
			}

			//! If the current station isn't inside a polygon, we skip it
			if ( !stationIsAllowed(latitude, longitude) )
			    continue;

			float acceleration = .0;

			xmlNode* third_node = NULL;
			for (third_node = sec_node->children; third_node;
			        third_node = third_node->next) {

				//! Skip any node that isn't 'comp'
				if ( xmlStrcmp(third_node->name, (const xmlChar *) "comp") )
				    continue;

				//! Get the current station acceleration, velocity...
				xmlNode* fourth_node = NULL;
				for (fourth_node = third_node->children; fourth_node;
				        fourth_node = fourth_node->next) {

					//! Skip any node that isn't 'acc', we only check the
					//! acceleration value in this version of the program
					if ( xmlStrcmp(fourth_node->name, (const xmlChar *) "acc") )
					    continue;

					string acc;
					for (xmlAttrPtr attr = fourth_node->properties;
					NULL != attr; attr = attr->next)
						if ( !xmlStrcmp(attr->name, (const xmlChar *) "value") )
						    acceleration = stringToFloat(toString(attr->children->content));
				}

			}

			//! If the current station is blacklisted, we skip it
			if ( stationIsBlacklisted(code) )
			    continue;

			//! If the current station acceleration is under the minimum
			//! acceleration value, we skip it...
			//! @note Acceleration values in XML are stored in %g, make sure
			//!       the user knows it first
			if ( acceleration < _settings.minStationAcc || acceleration > 100. )
			    continue;

			//! Converts acceleration from %g to mg
			acceleration *= 10.;

			bool alreadyAdded = false;
			for (size_t i = 0; i < _stations.size(); ++i)
				if ( _stations[i].code == code )
				    alreadyAdded = true;

			if ( alreadyAdded )
			    continue;

			if ( !isNan(acceleration) && !isMinusInf(acceleration) && !isPlusInf(acceleration) )
			    _stations.push_back(Station(code, instType, latitude, longitude, acceleration));
		}
	}

	writeMessage();

	xmlFreeDoc(doc);
	xmlCleanupParser();
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
bool XMLShakemapParser::stationIsBlacklisted(const string& code) {

	//! If no blacklist has been established, let the station be
	if ( _settings.stations.size() == 0 )
	    return false;

	for (size_t i = 0; i < _settings.stations.size(); ++i)
		if ( _settings.stations[i] == code )
		    return true;

	return false;
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
bool XMLShakemapParser::stationIsAllowed(const float& lat,
                                         const float& lon) {

	//! First check, no polygons, let the station be
	if ( _settings.polygons.size() == 0 )
	    return true;

	for (size_t i = 0; i < _settings.polygons.size(); ++i)
		if ( _settings.polygons[i].latitudeMax > lat && _settings.polygons[i].latitudeMin < lat )
		    if ( _settings.polygons[i].longitudeMax > lon && _settings.polygons[i].longitudeMin < lon )
		        return true;

	return false;
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
void XMLShakemapParser::writeMessage() {

	if ( _stations.size() == 0 ) {
		cout << "No alert raised from station acceleration" << endl;
		_retCode = OK;
		return;
	}

	if ( _settings.minStationAlert > 0 && _stations.size() < _settings.minStationAlert ) {
		cout << _stations.size() << "alert(s) raised but still under min. of "
		     << _settings.minStationAlert << " alert(s) before sending SMS message" << endl;
		_retCode = WARNING;
		return;
	}

	_message.clear();
	_retCode = ALARM;

	if ( _currentEvent.id.size() == 14 ) {
		char mShort[5];
		snprintf(mShort, 5, "%1.1fM", _currentEvent.magnitude);
		string msgHead = _currentEvent.id.substr(0, 8) + " "
		        + _currentEvent.id.substr(8, 2) + ":" + _currentEvent.id.substr(10, 2) + ":"
		        + _currentEvent.id.substr(12, 2) + " " + toString(mShort);
		_message.append(msgHead);
	}
	else
		_message.append(now());
	_message.append("\n");

	map<string, float> sortedStations;
	for (size_t i = 0; i < _stations.size(); ++i)
		sortedStations.insert(make_pair(_stations[i].code, _stations[i].acceleration));

	char buff[13];
	multimap<float, string> sortedAcc = flipMap(sortedStations);
	for (multimap<float, string>::const_reverse_iterator it = sortedAcc.rbegin();
	        it != sortedAcc.rend(); ++it) {

		if ( _message.length() < MAXSMSCHARS - 1 && _message.length() + 13 < MAXSMSCHARS - 1 ) {

			if ( it->first < 9.99 )
				snprintf(buff, 13, "%4s %1.2fmg\n", it->second.c_str(), it->first);
			else if ( it->first > 9.99 && it->first < 99.99 )
				snprintf(buff, 13, "%4s %2.1fmg\n", it->second.c_str(), it->first);
			else if ( it->first > 99.99 && it->first < 999.99 )
				snprintf(buff, 13, "%4s %3dmg\n", it->second.c_str(), static_cast<int>(it->first));
			else
				snprintf(buff, 13, "%4s +999mg\n", it->second.c_str(), it->first);

			_message += toString(buff);
		}
		else {
			if ( _debug ) {
				cout << "No more space available to store station acceleration information ("
				     << _message.length() << "/" << MAXSMSCHARS << " : "
				     << MAXSMSCHARS - _message.length() << " char(s) left)" << endl;
				cout << "Remaining station(s) will not be part of the message" << endl;
			}
			break;
		}
	}

	if ( _debug ) {
		cout << "Message content:" << endl;
		cout << _message;
		cout << "Message total chars: " << _message.length() << endl;
	}
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
void XMLShakemapParser::writeLog() {
	// Write some fancy log...
}
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


}// namespace Core
} // namespace IPGP
