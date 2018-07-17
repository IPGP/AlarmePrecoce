default: package

validate:
	mvn validate

build: compile

compile:
	mvn compile

javadoc:
	package
	rm target/EarlyWarning.jar
	rm -rf target/resources

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



clean:
	rm -rf target
