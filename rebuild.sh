#!/bin/bash

rm -fr ./node_modules/
rm -fr ./out/

npm i
tsc -p ./
