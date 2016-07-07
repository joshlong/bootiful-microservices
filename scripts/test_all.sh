#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

echo -e "This script will run Brixton and then Camden tests"

./scenario_brixton_tester.sh || ./scripts/scenario_brixton_tester.sh
./scenario_camden_tester.sh || ./scripts/scenario_camden_tester.sh

echo -e "The tests passed successfully!"

cd $ROOT_FOLDER