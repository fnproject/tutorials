#! /usr/bin/env bash

FLOWSERVER_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' flowserver)
TOOLS_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' bristol)

fn apps config set travel COMPLETER_BASE_URL "http://$FLOWSERVER_IP:8081"

fn routes config set travel /flight/book FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn routes config set travel /flight/book FLIGHT_API_SECRET "shhhh"

fn routes config set travel /flight/cancel FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn routes config set travel /flight/cancel FLIGHT_API_SECRET "shhhh"

fn routes config set travel /hotel/book HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"
fn routes config set travel /hotel/cancel HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"

fn routes config set travel /car/book CAR_API_URL "http://$TOOLS_IP:3001/car"
fn routes config set travel /car/cancel CAR_API_URL "http://$TOOLS_IP:3001/car"

fn routes config set travel /email EMAIL_API_URL "http://$TOOLS_IP:3001/email"
