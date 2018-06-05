#! /usr/bin/env bash

FLOWSERVER_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' flowserver)
TOOLS_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' bristol)

fn config app travel COMPLETER_BASE_URL "http://$FLOWSERVER_IP:8081"

fn config route travel /flight/book FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn config route travel /flight/book FLIGHT_API_SECRET "shhhh"

fn config route travel /flight/cancel FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn config route travel /flight/cancel FLIGHT_API_SECRET "shhhh"

fn config route travel /hotel/book HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"
fn config route travel /hotel/cancel HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"

fn config route travel /car/book CAR_API_URL "http://$TOOLS_IP:3001/car"
fn config route travel /car/cancel CAR_API_URL "http://$TOOLS_IP:3001/car"

fn config route travel /email EMAIL_API_URL "http://$TOOLS_IP:3001/email"
