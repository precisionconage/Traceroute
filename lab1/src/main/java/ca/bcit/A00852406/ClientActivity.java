package ca.bcit.A00852406;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Encapsulates the client side of the application.
 *
 * This class allows the user to specify a server (port and IP address) to send their location updates
 * to and to start and stop the gathering of location data.
 *
 * @author Shane Spoor
 */
public class ClientActivity extends Activity
{
    private LocationListener    listener;   /** A listener to listen for and respond to location updates. */
    private LocationManager     manager;    /** A location manager to determine the location provider. */
    private String              provider;   /** The string identifying the chosen location provider. */

    /**
     * This object encapsulates an asynchronous task to send the location data to the network.
     *
     * No network calls may be used on the main thread, so it's necessary to create another thread to
     * update the server.
     *
     * @author Shane Spoor
     */
    private class SendLocationUpdate extends AsyncTask<String, String, Void> {
        private static final int SERVER_ADDR_IDX    = 0; /** The index of the server's IP address string in the params array. */
        private static final int SERVER_PORT_IDX    = 1; /** The index of the string representing the port to send on. */
        private static final int DATA_IDX           = 2; /** The index into the params array of the data to be sent. */

        private InetAddress     serverAddress;           /** A byte representation of the server's internet address. */
        private DatagramSocket  udpSock;                 /** A UDP socket on which to send data. */
        private DatagramPacket  dgramPacket;             /** A datagram packet to hold the data to be sent. */
        private int             port;                    /** The port on which to send the data. */

        /**
         * Creates a socket and datagram and sends the data to the specified address on the specified port.
         *
         * If the function raises an exception at any point, the exception is caught and published to
         * UI thread.
         *
         * @param params String representations of the server IP address and port, and the data to be sent.
         * @return Null
         * @author Shane Spoor
         */
        protected Void doInBackground(String... params) {
            try
            {
                serverAddress = InetAddress.getByName(params[SERVER_ADDR_IDX]);
                port = Integer.parseInt(params[SERVER_PORT_IDX]);
                udpSock = new DatagramSocket();
                dgramPacket = new DatagramPacket(params[DATA_IDX].getBytes(), params[DATA_IDX].length(), serverAddress, port);
                udpSock.send(dgramPacket);

            } catch (Exception e) {
                publishProgress("Send failure: " + e.getMessage() + "\n");
            } finally {
                if(udpSock != null)
                    udpSock.close();
                return null;
            }
        }

        /**
         * Displays the error message sent by the background thread.
         *
         * The background thread for this task only publishes updates on error, so this function
         * assumes that any string received is an error string.
         *
         * @param results The error string to be displayed/
         * @author Shane Spoor
         */
        protected void onProgressUpdate(String... results)
        {
            TextView programLog = (TextView)findViewById(R.id.programLog);
            programLog.setTextColor(Color.RED);
            programLog.append(results[0]);
        }
    }

    /**
     * An object to listen for updates, display them, and send them to the server.
     * @author Shane Spoor
     */
    private class ListenForUpdates implements LocationListener
    {
        private TextView programLog = (TextView)findViewById(R.id.programLog);
        private String IP;
        private String port;

        /**
         * Creates a new ListenForUpdates object.
         *
         * @param IPStr     The string representation of the server's IP address.
         * @param portStr   The string representation of the port on which to send.
         */
        public ListenForUpdates(String IPStr, String portStr)
        {
            super();
            IP = IPStr;
            port = portStr;
        }

        /**
         * Must be implemented (specified in <i>LocationListener</i>).
         *
         * @param provider Unused
         * @param status   Unused
         * @param extras   Unused
         */
        public void onStatusChanged (String provider, int status, Bundle extras)
        {}

        /**
         * Must be implemented (specified in <i>LocationListener</i>).
         * @param provider Unused
         */
        public void onProviderEnabled(String provider)
        {}

        /**
         * Must be implemented (specified in <i>LocationListener</i>).
         * @param provider Unused
         */
        public void onProviderDisabled(String provider)
        {}

        /**
         * Displays the location value on the client's screen and sends it to the server.
         *
         * The function reads and formats the location data, then creates an AsyncTask to forward the
         * data to the server specified by the user.
         *
         * @param loc The location data read from the provider.
         * @author Shane Spoor
         */
        public void onLocationChanged(Location loc)
        {
            final DateFormat df     = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            final String nowAsISO   = df.format(loc.getTime());
            final double latVal     = loc.getLatitude();
            final double longVal    = loc.getLongitude();
            final String latStr     = (latVal < 0 ? latVal * -1 + "\u00B0 S" : latVal + "\u00B0 N");
            final String longStr    = (longVal < 0 ? longVal * -1 + "\u00B0 W" : longVal + "\u00B0 E");
            final TextView log      = (TextView)findViewById(R.id.programLog);

            String data = "Time: " + nowAsISO + "\nLatitude: " + latStr + "\nLongitude: " + longStr + "\n";

            log.setTextColor(Color.BLACK);
            log.append(data);
            new SendLocationUpdate().execute(IP, port, data);
        }

        /**
         * Sets the port string to the string <i>>newIP</i>.
         *
         * If <i>newIP</i> is null, the IP address string is left unchanged and the function returns null.
         *
         * @param newIP   The new IP address to store.
         * @return        Returns <i>newIP</i>.
         *
         * @autbor Shane Spoor
         */
        public String setIP(String newIP)
        {
            IP = newIP;
            return IP;
        }

        /**
         * Sets the port string to the string <i>>newPort</i>.
         *
         * If <i>newPort</i> is null, the port is left unchanged and the function returns null.
         *
         * @param newPort   The new port to store.
         * @return          Returns <i>newPort</i>.
         *
         * @autbor Shane Spoor
         */
        public String setPort(String newPort)
        {
            if(newPort == null)
                return null;

            port = newPort;
            return port;
        }
    }


    /**
     * Sets the initial layout and creates a LocationManager for use with the plotting functions.
     *
     * @param savedInstanceState The state of the application before it was destroyed by the OS (e.g., the
     *                           user rotated the screen, it was swapped out of memory, etc). (currently unused)
     * @author Shane Spoor
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_client);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setCostAllowed(false);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_MEDIUM);

        manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        provider = manager.getBestProvider(criteria, true);
    }

    /**
     * Registers the location listener with the best available provider and tells it to begin listening.
     *
     * If the user enters an empty string for the port number and/or IP address, the function displays
     * an error message in the respective text edit control and doesn't start plotting the points.
     *
     * @param view Unused
     *
     * @author Shane Spoor
     */
    public void startPlotting(View view)
    {
        EditText editIP = (EditText) findViewById(R.id.IP);
        EditText editPort = (EditText) findViewById(R.id.port);
        boolean error = false;

        if(editIP.getText().toString().equals(""))
        {
            editIP.setHintTextColor(Color.RED);
            editIP.setHint("IP cannot be empty");
            error = true;
        }
        if(editPort.getText().toString().equals(""))
        {
            editPort.setHintTextColor(Color.RED);
            editPort.setHint("Port cannot be empty");
            error = true;
        }
        if(error)
            return;
        if(listener == null)
            listener = new ListenForUpdates(editIP.getText().toString(), editPort.getText().toString());

        manager.requestLocationUpdates(provider, 0, 1, listener);
        ((ListenForUpdates)listener).setIP(editIP.getText().toString());
        ((ListenForUpdates)listener).setPort(editPort.getText().toString());

    }

    /**
     * Removes all pending updates from the listener and unregisters it from the location provider.
     *
     * @param view Unused
     * @author Shane Spoor
     */
    public void stopPlotting(View view)
    {
        if(listener != null)
            manager.removeUpdates(listener);
    }
}
