default:
	mvn package
	mv target/EarlyWarning.jar .
	rm -rf target
	mkdir target
	cp -r src/main/resources target
	mv EarlyWarning.jar target

clean:
	rm -rf target
