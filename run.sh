rm -rf bin/* && \
javac -cp bin/* -d bin src/model/*.java src/controller/*.java src/view/*.java && \
java -cp ./bin Main
