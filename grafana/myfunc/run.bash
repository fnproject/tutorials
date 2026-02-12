#!/bin/bash

for value in {1..100}
do
  curl localhost:8080/t/myapp/myfunc
done
