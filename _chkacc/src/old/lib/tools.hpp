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

#ifndef __IPGP_CORE_TOOLS_HPP__
#define __IPGP_CORE_TOOLS_HPP__



#include <string>
#include <iostream>
#include <sstream>
#include <ostream>
#include <vector>
#include <complex>
#include <map>
#include <algorithm>
#include <math.h>
#include <sys/stat.h>
#include <numeric>

namespace IPGP {
namespace Core {


static inline bool fileExists(const std::string& file) {
	struct stat buf;
	int s = stat(file.c_str(), &buf);
	if ( s != 0 )
		return false;

	if ( !S_ISREG(buf.st_mode) )
		return false;
	return true;
}


static inline int stringToInt(const std::string& str) {
	int value;
	std::istringstream iss(str);
	iss >> value;
	return value;
}

static inline float stringToFloat(const std::string& str) {
	float value = atof(str.c_str());
	return value;
}

static inline std::string stripBlankSpace(std::string str) {
	for (size_t i = 0; i < str.length(); i++)
		if ( str[i] == ' ' ) {
			str.erase(i, 1);
			i--;
		}
	return str;
}

static inline void stringExplode(std::string str, const std::string& separator,
                                 std::vector<std::string>* results) {
	int found;
	found = str.find_first_of(separator);
	while ( found != (signed) std::string::npos ) {
		if ( found > 0 )
			results->push_back(str.substr(0, found));
		str = str.substr(found + 1);
		found = str.find_first_of(separator);
	}
	if ( str.length() > 0 )
		results->push_back(str);
}

template<typename T>
static inline std::string toString(const T& v) {
	std::ostringstream os;
	os.precision(10);
	os << v;
	return os.str();
}

template<typename T>
static inline std::string toString(const std::complex<T>& v) {
	std::ostringstream os;
	os << "(" << toString(v.real()) << "," << toString(v.imag()) << ")";
	return os.str();
}


template<typename A, typename B>
static inline std::multimap<B, A> flipMap(const std::map<A, B>& src) {
	std::multimap<B, A> dst;
	for (typename std::map<A, B>::const_iterator it = src.begin();
	        it != src.end(); ++it)
		dst.insert(std::pair<B, A>(it->second, it->first));
	return dst;
}

static inline bool isNan(const float& number) {
	return (number != number);
}

static inline bool isMinusInf(const float& number) {
	return (number < 0 && number / number != number / number);
}

static inline bool isPlusInf(const float& number) {
	return (number > 0 && number / number != number / number);
}

} // namespace Code
} // namespace IPGP

#endif
