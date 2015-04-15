.PHONY: clean all header aspell

SCALA_SRC=./src/main/scala

all: aspell
	gcc -shared -o liblucidaspell.so Aspell.o -Wl,-rpath,/usr/local/lib -L/usr/local/lib -laspell

aspell: header Aspell.cc
	gcc -c -fPIC ./Aspell.cc -o Aspell.o  -I/usr/lib/jvm/java-6-openjdk-amd64/include/  -I/usr/lib/jvm/java-6-openjdk-amd64/include/linux

header: 
	scalac ${SCALA_SRC}/Aspell.scala
	javah -jni -o Aspell.h com.lucidchart.aspell.Aspell 

clean: 
	sbt clean
	rm -rf liblucidaspell.so *.o com/ project/ target/
