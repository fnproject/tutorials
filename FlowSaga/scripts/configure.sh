#! /usr/bin/env bash

if ! [ -x "$(command -v jq)" ]; then
  echo 'Make sure to have `jq` installed, see https://stedolan.github.io/jq/.' >&2
  exit 1
fi


FLOWSERVER_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' flowserver)
TOOLS_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' bristol)

fn config app travel COMPLETER_BASE_URL "http://$FLOWSERVER_IP:8081"

TOOLS_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' bristol)


fn config fn travel flight-book FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn config app travel FLIGHT-BOOK-ID $(fn inspect fn travel flight-book | jq '.id' | xargs)
fn config fn travel flight-book FLIGHT_API_SECRET "shhhh"


fn config fn travel flight-cancel FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn config app travel FLIGHT-CANCEL-ID $(fn inspect fn travel flight-cancel | jq '.id' | xargs)
fn config fn travel flight-cancel FLIGHT_API_SECRET "shhhh"


fn config fn travel hotel-book HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"
fn config app travel HOTEL-BOOK-ID $(fn inspect fn travel hotel-book | jq '.id' | xargs)
fn config fn travel hotel-book HOTEL_API_SECRET "shhhh"


fn config fn travel hotel-cancel HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"
fn config app travel HOTEL-CANCEL-ID $(fn inspect fn travel hotel-cancel | jq '.id' | xargs)
fn config fn travel hotel-cancel HOTEL_API_SECRET "shhhh"

fn config fn travel car-book CAR_API_URL "http://$TOOLS_IP:3001/car"
fn config app travel CAR-BOOK-ID $(fn inspect fn travel car-book | jq '.id' | xargs)
fn config fn travel car-book CAR_API_SECRET "shhhh"


fn config fn travel car-cancel CAR_API_URL "http://$TOOLS_IP:3001/car"
fn config app travel CAR-CANCEL-ID $(fn inspect fn travel car-cancel | jq '.id' | xargs)
fn config fn travel car-cancel CAR_API_SECRET "shhhh"


fn config fn travel email EMAIL_API_URL "http://$TOOLS_IP:3001/email"
fn config app travel EMAIL-ID $(fn inspect fn travel email | jq '.id' | xargs)

