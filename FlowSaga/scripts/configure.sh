#! /usr/bin/env bash

FLOWSERVER_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' flowserver)
TOOLS_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' bristol)

fn config apps travel COMPLETER_BASE_URL "http://$FLOWSERVER_IP:8081"

fn config routes travel /flight/book FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn config routes travel /flight/book FLIGHT_API_SECRET "shhhh"

fn config routes travel /flight/cancel FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn config routes travel /flight/cancel FLIGHT_API_SECRET "shhhh"

fn config routes travel /hotel/book HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"
fn config routes travel /hotel/cancel HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"

fn config routes travel /car/book CAR_API_URL "http://$TOOLS_IP:3001/car"
fn config routes travel /car/cancel CAR_API_URL "http://$TOOLS_IP:3001/car"

fn config routes travel /email EMAIL_API_URL "http://$TOOLS_IP:3001/email"
