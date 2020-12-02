/*************************************
 * Filename:  Receiver.java
 * Names: Teodor-Avram Ciochirca
 * Student-IDs: 201360672
 * Date: 17.11.2020
 *************************************/
import java.util.Random;

public class Receiver extends NetworkHost

{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *
     * Predefined Member Methods:
     *
     *  void startTimer(double increment):
     *       Starts a timer, which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this in the Sender class.
     *  void stopTimer():
     *       Stops the timer. You should only call this in the Sender class.
     *  void udtSend(Packet p)
     *       Sends the packet "p" into the network to arrive at other host
     *  void deliverData(String dataSent)
     *       Passes "dataSent" up to application layer. Only call this in the
     *       Receiver class.
     *  double getTime()
     *       Returns the current time of the simulator.  Might be useful for
     *       debugging.
     *  String getReceivedData()
     *       Returns a String with all data delivered to receiving process.
     *       Might be useful for debugging. You should only call this in the
     *       Sender class.
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from application layer
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet, which is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          create a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */

    // Add any necessary class variables here. They can hold
    // state information for the receiver.
    Packet stored_packet; //holds the packet
    int last_chekcsum; //holds the checksum of the last received packet

    // Also add any necessary methods (e.g. checksum of a String)
    private int checksumOfString(String payload, int seqno, int ackno) //calculates the checksum based on payload, seqno, and ackno
    {
        int char_val;
        int sum = 0;

        // calculate checksum
        final int max = payload.length();

        for (int i = 0; i < max; i++) {
            char_val = payload.charAt(i);
            sum += char_val;
        }

        sum += seqno;
        sum += ackno;
        return sum;
    }

    private boolean corruptPacket(Packet packet) //checks if a packet is corrupt by checking the checksums
    {
        return packet.getChecksum() == checksumOfString(packet.getPayload(), packet.getSeqnum(), packet.getAcknum());
    }

    // This is the constructor.  Don't touch!
    public Receiver(int entityName,
                       EventList events,
                       double pLoss,
                       double pCorrupt,
                       int trace,
                       Random random)
    {
        super(entityName, events, pLoss, pCorrupt, trace, random);
    }

    
    // This routine will be called whenever a packet from the sender
    // (i.e. as a result of a udtSend() being done by a Sender procedure)
    // arrives at the receiver. Argument "packet" is the (possibly corrupted)
    // packet that was sent from the sender.
    protected void Input(Packet packet)
    {
        stored_packet = new Packet(packet); //store the received packet

        if(!corruptPacket(packet)){ //check for corruption
            udtSend(stored_packet); //send back packet
            System.out.println("Corruption detected..."); //output appropriate information
            return;
        } else if(packet.getChecksum() == last_chekcsum) { //check for duplicates
            udtSend(stored_packet); //re-send acknowledge
            System.out.println("Resending ack: " + packet.getAcknum()); //output appropriate information
            return;
        } else if((packet.getSeqnum() == 0 && packet.getAcknum() ==0) || (packet.getSeqnum() == 1 && packet.getAcknum() == 1)) { //if the correct packet arrived
            last_chekcsum = packet.getChecksum(); //store its checksum for future duplicate checking
            deliverData(packet.getPayload()); //deliver the data to application layer
            System.out.println("Got packet " + packet.getSeqnum());  //output appropriate information
            udtSend(stored_packet); //send acknowledgement
            System.out.println("Sending ack: " + packet.getAcknum()); //output appropriate information
        }
    }
    

    
    // This routine will be called once, before any of your other receiver-side
    // routines are called. The method should be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of the receiver).
    protected void Init()
    {
        stored_packet = null; //empty packet
        last_chekcsum = 0; //empty checksum
    }

}
