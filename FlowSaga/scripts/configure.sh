#!/bin/bash 

# Fn application
APP=travel

function getFuncId {
	fn ls fn $APP | grep $1 | awk 'NF>1{print $NF}'
}  

FLOWSERVER_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' flowserver)
TOOLS_IP=$(docker inspect --type container -f '{{.NetworkSettings.IPAddress}}' bristol)

fn config app $APP COMPLETER_BASE_URL "http://$FLOWSERVER_IP:8081"

# set various functionId's
fn config function $APP trip FLIGHT_BOOK_ID $(getFuncId flight-book)
fn config function $APP trip FLIGHT_CANCEL_ID $(getFuncId flight-cancel)
fn config function $APP trip HOTEL_BOOK_ID $(getFuncId hotel-book)
fn config function $APP trip HOTEL_CANCEL_ID $(getFuncId hotel-cancel)
fn config function $APP trip CAR_BOOK_ID $(getFuncId car-book)
fn config function $APP trip CAR_CANCEL_ID $(getFuncId car-cancel)
fn config function $APP trip EMAIL_ID $(getFuncId email)

fn config function $APP flight-book FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn config function $APP flight-book FLIGHT_API_SECRET "shhhh"
fn config function $APP flight-cancel FLIGHT_API_URL "http://$TOOLS_IP:3001/flight"
fn config function $APP flight-cancel FLIGHT_API_SECRET "shhhh"

fn config function $APP hotel-book HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"
fn config function $APP hotel-book HOTEL_API_SECRET "shhhh"
fn config function $APP hotel-cancel HOTEL_API_URL "http://$TOOLS_IP:3001/hotel"
fn config function $APP hotel-cancel HOTEL_API_SECRET "shhhh"

fn config function $APP car-book CAR_API_URL "http://$TOOLS_IP:3001/car"
fn config function $APP car-cancel CAR_API_URL "http://$TOOLS_IP:3001/car"

fn config function $APP email EMAIL_API_URL "http://$TOOLS_IP:3001/email"
