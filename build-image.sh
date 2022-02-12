#!/bin/bash
mkdir -p target/dependency
./mvnw package
cd target/dependency
jar -xf ../*.jar
cd ../../
docker build .
