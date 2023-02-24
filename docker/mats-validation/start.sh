#!/bin/bash

cd /home/rhel/bin
python3 main.py

cd /home/rhel/great_expectations

great_expectations checkpoint run $CHECK_POINT
