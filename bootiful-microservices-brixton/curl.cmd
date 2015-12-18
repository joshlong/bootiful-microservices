#!/bin/bash

curl -H"content-type: application/json" -d "{ \"reservationName\":\"Bob\"}" http://localhost:9999/reservations
