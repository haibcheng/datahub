#!/bin/bash

cd /home/rhel/great_expectations

export SSL_CERT_FILE=/home/rhel/great_expectations/uncommitted/trinoroot.pem

great_expectations checkpoint run $CHECK_POINT
