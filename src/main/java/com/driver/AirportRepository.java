package com.driver;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AirportRepository {
    HashMap<String,Airport> airportsDb;
    HashMap<Integer,Flight> flightsDb;
    HashMap<Integer,Passenger> passengersDb;
    HashMap<Integer, List<Integer>> passengerBookedFlightDb;
    HashMap<Integer, List<Integer>> totalFlightBookedByPassengerDb;

    public AirportRepository() {
        this.airportsDb = new HashMap<>();
        this.flightsDb = new HashMap<>();
        this.passengersDb = new HashMap<>();
        this.passengerBookedFlightDb = new HashMap<>();
        this.totalFlightBookedByPassengerDb = new HashMap<>();
    }


    public String addPassenger(Passenger passenger) {
        String message = null;
        if(!passengersDb.containsKey(passenger.getPassengerId())) {
            passengersDb.put(passenger.getPassengerId(), passenger);
            passengerBookedFlightDb.put(passenger.getPassengerId(), new ArrayList<>());
            message = "SUCCESS";
        }
        return message;
    }

    public String getAirportNameFromFlightId(Integer flightId) {
        String name = null;
        String code = null;
        if(flightsDb.containsKey(flightId)) {
            Flight flight = flightsDb.get(flightId);
            name = String.valueOf(flight.getFromCity());
            code = name.substring(0,1) + name.substring(2,3);
        }

        return code;
    }

    public String addFlight(Flight flight) {
        flightsDb.put(flight.getFlightId(),flight);
        totalFlightBookedByPassengerDb.put(flight.getFlightId(), new ArrayList<>());
        return "SUCCESS";
    }

    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId) {
        int bookings = 0;
        if(totalFlightBookedByPassengerDb.containsKey(passengerId)) {
            bookings = totalFlightBookedByPassengerDb.get(passengerId).size();
        }
        return bookings;
    }

    public void addAirport(Airport airport) {
        airportsDb.put(airport.getAirportName(), airport);
    }

    public String getLargestAirportName() {
        int maxTerminals = 0;
        int countAirports = 0;

        for(Airport airport : airportsDb.values()) {
            if(airport.getNoOfTerminals()==maxTerminals) {
                countAirports++;
            }
            else if(airport.getNoOfTerminals()>maxTerminals) {
                countAirports = 1;
                maxTerminals = airport.getNoOfTerminals();
            }
        }

        String name = null;

        if(countAirports==1) {
            for(String s : airportsDb.keySet()) {
                if(airportsDb.get(s).getNoOfTerminals()==maxTerminals) {
                    name = s;
                    break;
                }
            }
        }
        else {
            List<String> airport = new ArrayList<>();
            for(String s : airportsDb.keySet()) {
                if(airportsDb.get(s).getNoOfTerminals()==maxTerminals) {
                    airport.add(s);
                }
            }
            Collections.sort(airport);
            name = airport.get(0);
        }

        return name;
    }

    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity) {
        double duration = Integer.MAX_VALUE;
        for(Flight flight : flightsDb.values()) {
            if(flight.getFromCity().equals(fromCity) && flight.getToCity().equals(toCity)) {
                if(duration > flight.getDuration()) {
                    duration = flight.getDuration();
                }
            }
        }
        if(duration == Integer.MAX_VALUE) duration = -1;
        return duration;
    }

    public int getNumberOfPeopleOn(Date date, String airportName) {
        int totalNumberPassengers = 0;
        if(flightsDb.size()==0) return totalNumberPassengers;

         City city = airportsDb.get(airportName).getCity();

        for(Integer id : flightsDb.keySet()) {
            Flight flight = flightsDb.get(id);
            if(flight.getFlightDate().compareTo(date)==0 && (flight.getFromCity().compareTo(city)==0 || flight.getToCity().compareTo(city)==0)) {
                totalNumberPassengers += passengerBookedFlightDb.get(id).size();
            }
        }
        return totalNumberPassengers;
    }

    public int calculateFlightFare(Integer flightId) {
        int fare = 0;
        if(passengerBookedFlightDb.containsKey(flightId)) {
            int bookedSeats = passengerBookedFlightDb.get(flightId).size();
            fare = 3000 + (50*bookedSeats);
        }

        return fare;
    }

    public String bookATicket(Integer flightId, Integer passengerId) {
        String message = "FAILURE";

        int bookedSeats = passengerBookedFlightDb.get(flightId).size();
        if(bookedSeats < flightsDb.get(flightId).getMaxCapacity()) {
            List<Integer> booked = passengerBookedFlightDb.get(flightId);
            for(Integer id : booked) {
                if(id==passengerId) {
                    message = "FAILURE";
                    return message;
                }
            }
            //book ticket
            List<Integer> l = passengerBookedFlightDb.get(flightId);
            l.add(passengerId);

            passengerBookedFlightDb.put(flightId, l);
            List<Integer> list = totalFlightBookedByPassengerDb.get(passengerId);
            list.add(flightId);
            totalFlightBookedByPassengerDb.put(passengerId, list);
            message = "SUCCESS";
        }
        else {
            message = "FAILURE";
        }

        return message;
    }

    public String cancelATicket(Integer flightId, Integer passengerId) {
        String message = null;

        if(flightsDb.containsKey(flightId)) {
            boolean booked = false;
            List<Integer> l = totalFlightBookedByPassengerDb.get(passengerId);

            for(Integer i : l) {
                if(i==flightId) {
                    booked = true;
                    break;
                }
            }

            if(!booked) {
                message = "FAILURE";
                return message;
            }

            //cancel ticket
            for(int i=0; i<l.size(); i++) {
                if(l.get(i)==flightId) {
                    l.remove(i);
                    break;
                }
            }

            totalFlightBookedByPassengerDb.put(passengerId, l);

            List<Integer> list = passengerBookedFlightDb.get(flightId);
            for(int i=0; i<list.size(); i++) {
                if(list.get(i)==passengerId) {
                    list.remove(i);
                    break;
                }
            }

            passengerBookedFlightDb.put(flightId, list);
            message = "SUCCESS";
        }
        else {
            message = "FAILURE";
        }
        return message;
    }

    public int calculateRevenueOfAFlight(Integer flightId) {
        int revenue = 0;
        int price = 3000;

        for(int i=0; i<passengerBookedFlightDb.get(flightId).size(); i++) {
            revenue += price;
            price += 50;
        }

        return revenue;
    }
}
