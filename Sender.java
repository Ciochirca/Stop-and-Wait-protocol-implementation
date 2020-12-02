/*************************************
 * Filename:  Sender.java
 * Names: Teodor-Avram Ciochirca
 * Student-IDs: 201360672
 * Date: 17.11.2020
 *************************************/
import java.util.Random;

public class Sender extends NetworkHost

{
    /*
     * Predefined Constant (static member variables):
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
     *  Message: Used to encapsulate the message coming from application layer
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
    // state information for the sender.
    int sender_state, checksum; //holds the sender state and the checksum
    Packet stored_packet; //holds the packet
    boolean acknowledged; //decides if the next packet will be created or not

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
    public Sender(int entityName, EventList events, double pLoss, double pCorrupt, int trace, Random random)
    {
        super(entityName, events, pLoss, pCorrupt, trace, random);
    }

    // This routine will be called whenever the application layer at the sender
    // has a message to  send.  The job of your protocol is to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving application layer.
    protected void Output(Message message)
    {
        if(acknowledged) { //create a message only if the last one was acknowledged
            if(sender_state == 0) { //set seqnum and acknum to 0
                checksum = checksumOfString(message.getData(), 0, 0); //create checksum
                stored_packet = new Packet(0, 0, checksum, message.getData()); //create packet
                udtSend(stored_packet); //send packet
            } else if(sender_state == 1) { //set the seqnum and acknum to 1
                checksum = checksumOfString(message.getData(), 1, 1); //create checksum
                stored_packet = new Packet(1, 1, checksum, message.getData()); //create packet
                udtSend(stored_packet); //send packet
            }
            acknowledged = false; //set to false to prevent double send
            startTimer(100); //start timer
        }
    }
    
    // This routine will be called whenever a packet sent from the receiver 
    // (i.e. as a result of udtSend() being done by a receiver procedure)
    // arrives at the sender.  "packet" is the (possibly corrupted) packet
    // that was sent from the receiver.
    protected void Input(Packet packet)
    {

        if(!corruptPacket(packet)){ //check if the packet is corrupt
            System.out.println("Corruption detected..."); //output appropriate information
            return;
        } else if (packet.getAcknum() == 0 && packet.getSeqnum() == 0 && checksum == packet.getChecksum()) {
            sender_state = 1; //set the sender state such that the next message will be made
            System.out.println("Got acknum 0, we're waiting for acknum 1."); //output appropriate information
        } else if(packet.getAcknum() == 1 && packet.getSeqnum() == 1 && checksum == packet.getChecksum()) {
            sender_state = 0; //set the sender state such that the next message will be made
            System.out.println("Got acknum 1, we're waiting for acknum 0."); //output appropriate information
        }
        acknowledged = true; //set to true so that next message will be created
    }
    
    // This routine will be called when the senders's timer expires (thus 
    // generating a timer interrupt). You'll probably want to use this routine 
    // to control the retransmission of packets. See startTimer() and 
    // stopTimer(), above, for how the timer is started and stopped. 
    protected void TimerInterrupt()
    {
        System.out.println("Timer interrupt, resending packet"); //output appropriate information
        udtSend(stored_packet); //re-send packet
        startTimer(100); //start timer
    }
    
    // This routine will be called once, before any of your other sender-side 
    // routines are called. The method should be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of the sender).
    protected void Init()
    {
        sender_state = 0; //first state for the first packet
        checksum = 0; //empty checksum
        stored_packet = null; //empty packet
        acknowledged = true; //set to true, allows creation of the first packet
    }

}
