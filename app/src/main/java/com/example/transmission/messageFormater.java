package com.example.transmission;

public class messageFormater {
    String formattedMessage="";
    NodeInfo nodeInfo= new NodeInfo();
    int format=0;
    String message="";
    String destination;

    public messageFormater(int format, String message, String destination)
    {
        this.format=format;
        this.message=message;
        this.destination=destination;
    }

    public String Receiver(int format, String message)
    {
        if(format==1)
        {
            message= "1;"+nodeInfo.getWFDMacAddress()+";"+message+";"+destination;
        }
        return message;
    }


}
