default: help

help:
	@echo "Available targets "
	@echo "    help     : Shows this message"
	@echo "    test     : Executes unit tests and generates the HTML report"
	@echo "    validate : Validates (or installs) dependencies"
	@echo "    compile  : Compiles the project classes"
	@echo "    javadoc  : Generates the Javadoc for the project"
	@echo "    package  : Run the packaging routine and cleans the compiled, unpackaged files"
	@echo "    clean    : Cleans the compiled files"
	@echo ""

test:
	mvn clean test surefire-report:report
	mv target/site/surefire-report.html target/tests-report.html
	rm -rf target/site

validate:
	mvn validate

compile:
	mvn compile

javadoc: package
	rm target/EarlyWarning.jar
	rm -rf target/resources

lpackage:
	mvn package
	mv target/EarlyWarning.jar .
	rm -rf target
	mkdir target
	mv EarlyWarning.jar target

hpackage: lpackage
	mv target/EarlyWarning.jar .

package:
	mvn package
	mvn javadoc:javadoc
	mv target/site/apidocs Javadoc
	mv target/EarlyWarning.jar .
	rm -rf target
	mkdir target
	cp -r src/main/resources target
	mv EarlyWarning.jar target
	mv Javadoc target

mvninstall:
	mvn install:install-file -Dfile=EarlyWarning.jar -DgroupId=fr.ipgp -DartifactId=earlywarning -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true

hrun:
	java -jar EarlyWarning.jar

clean:
	rm -rf target

# Aliases

build: compile
