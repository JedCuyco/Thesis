package com.example.transmission;

public class RouteEntry {
    public String deviceName;
    public String nextHopMacAddress;
    public int hopCount;
    public int batteryPercenteage;
    public int phoneSignal;

    public RouteEntry(String deviceName, String nextHopMacAddress, int hopCount, int batteryPercenteage, int phoneSignal)
    {
        this.deviceName=deviceName;
        this.nextHopMacAddress= nextHopMacAddress;
        this.hopCount=hopCount;
        this.batteryPercenteage=batteryPercenteage;
        this.phoneSignal=phoneSignal;

    }


    public void setNextHopMacAddress(String nextHopMacAddress) {
        this.nextHopMacAddress = nextHopMacAddress;
    }

    public void setBatteryPercenteage(int batteryPercenteage) {
        this.batteryPercenteage = batteryPercenteage;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public void setPhoneSignal(int phoneSignal) {
        this.phoneSignal = phoneSignal;
    }

    public String getNextHopMacAddress() {
        return nextHopMacAddress;
    }
    public String getDestinationMac(){
        return deviceName;
    }
    public int getHopCount()
    {
        return hopCount;
    }

    public int getBatteryPercenteage() {
        return batteryPercenteage;
    }
    public int getPhoneSignal() {
        return phoneSignal;
    }
}
