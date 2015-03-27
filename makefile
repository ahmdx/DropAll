SRCPATH = ./src/DropAll/
PACKAGE1 = ./src/exceptions/
PACKAGE2 = ./src/index/
PACKAGE3 = ./src/page/
PACKAGE4 = ./src/table/
CLSSPATH = ./classes/

main: 
	       javac -d $(CLSSPATH) -classpath $(CLSSPATH) $(PACKAGE1)*.java
	       javac -d $(CLSSPATH) -classpath $(CLSSPATH) $(PACKAGE2)*.java
	       javac -d $(CLSSPATH) -classpath $(CLSSPATH) $(PACKAGE3)*.java
	       javac -d $(CLSSPATH) -classpath $(CLSSPATH) $(PACKAGE4)*.java
		   javac -d $(CLSSPATH) -classpath $(CLSSPATH) $(SRCPATH)*.java

all: main


clean: 
	   rm -rf $(CLSSPATH)*

