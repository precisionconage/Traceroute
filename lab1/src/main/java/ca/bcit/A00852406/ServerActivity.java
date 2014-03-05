package ca.bcit.A00852406;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 * Encapsulates the server's functionality.
 *
 * This class displays a GUI allowing the user to start and stop listening for updates on a given
 * port. It also prints out the data received, the client from whom it was received, and plots the
 * points on a map as they're generated.
 *
 * @author Shane Spoor
 */
public class ServerActivity extends Activity
{

    private RecvLocationUpdate recvTask;    /** Handle to an AsyncTask object which listens for and displays client data.*/
    private TextView  log;                  /** Handle to the Text View in which to display the data. */
    private GoogleMap mapHandle;            /** Handle to a GoogleMap object for plotting the points. */

    /**
     * This class creates a socket, listens for client data on it, and displays any data received.
     * @author <u>Aman Abdulla</u>
     * @author Shane Spoor
     */
    private class RecvLocationUpdate extends AsyncTask<String, String, Void> {
        private static final int DGRAM_SIZE = 1024; /** The size of the datagram in which incoming data will be stored. */

        private InetAddress     clientAddr;                                         /** The client's address. */
        private DatagramSocket  udpSock;                                            /** A UDP socket for receiving data. */
        private int             port;                                               /** The port on which to listen. */
        private byte[]          data = new byte[DGRAM_SIZE];                        /** A byte array in which to actually store the data. */
        private DatagramPacket  dgramPacket = new DatagramPacket(data, DGRAM_SIZE); /** A packet object that encapsulates the buffer. */

        @Override
        /**
         * Notifies the user that the server is listening for data before attempting to open the socket.
         * @author Shane Spoor
         */
        protected void onPreExecute()
        {
            log.append("Listening for data...\n");
        }

        /**
         * Listens for data and publishes it to the UI thread for display.
         *
         * This function will execute in its own thread (which is handled by the AsyncTask object). It
         * publishes data to the UI thread upon receiving it, then continues to monitor the socket. If
         * the user cancels the asynchronous task, this thread will exit.
         *
         * @param params A string representation of the port on which to listen.
         * @return Null
         *
         * @author Shane Sporr
         */
        protected Void doInBackground(String... params) {
            try
            {
                port               = Integer.parseInt(params[0]);
                udpSock            = new DatagramSocket(port);
                boolean cancelled  = isCancelled();
                String data;
                while(!cancelled)
                {
                    udpSock.receive(dgramPacket);

                    clientAddr      = dgramPacket.getAddress();
                    data = new String(dgramPacket.getData(), 0, dgramPacket.getLength());
                    publishProgress(data + "\nClient address: " + clientAddr.getHostAddress() + "\n");
                    cancelled = isCancelled();
                }
            } catch (Exception e) {
                publishProgress("Receive failure: " + e.getMessage());
            } finally {
                if(udpSock != null)
                    udpSock.close();
                return null;
            }
        }

        /**
         * Retrieves the update from the background thread and displays it in the GUI. If the thread
         * reported an error condidition, this is shown in red.
         *
         * @param update The update string to display.
         *
         * @author Shane Sporr
         */
        protected void onProgressUpdate(String... update)
        {
            if(update[0].contains("Receive failure:"))
            {
                log.setTextColor(Color.RED);
                log.append(update[0]);
                this.cancel(true);
            }
            else
            {
                LatLng loc;
                Scanner scan = new Scanner(update[0]);
                double lat = scan.nextDouble(), lon = scan.nextDouble();
                String latStr, longStr;
                loc                         = new LatLng(lat, lon);

                mapHandle.addMarker(new MarkerOptions().position(loc));
                latStr  = "Latitude: " + (lat < 0 ? lat * -1 + "\u00B0 S" : lat + "\u00B0 N") + "\n";
                longStr = "Longitude: " + (lon < 0 ? lon * -1 + "\u00B0 W" : lon + "\u00B0 E") + "\n";
                log.setTextColor(Color.BLACK);
                log.append(scan.next() + "\n" + latStr + longStr);
            }

        }
    }

    /**
     * Sets the layout and obtains a handle to the map.
     *
     * @param savedInstanceState The state of the app before being destroyed by a swap out of memory
     *                           or the user rotating the screen (currently unused).
     * @author Shane Spoor
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_server);
        log = (TextView)findViewById(R.id.server_log);
        mapHandle = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    }

    /**
     * Kills the server thread if it's active.
     *
     * If the server thread isn't released, it may cause issues when trying to run the server twice.
     * It must therefore be cancelled when the activity is destroyed.
     *
     * @author Shane Spoor
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(recvTask != null)
            recvTask.cancel(true);
    }


    /**
     * Starts an asynchronous task to listen for incoming data.
     *
     * If the user didn't enter anything for the port, the program will not prompt them to enter a
     * valid port number and not create the task.
     *
     * @param view Unused
     *
     * @author Shane Spoor
     */
    public void startListening(View view)
    {
        if(recvTask != null)
            return;

        final EditText    editPort = (EditText)findViewById(R.id.edit_port);
        final String      portStr = editPort.getText().toString();

        if(portStr.equals(""))
        {
            editPort.setHintTextColor(Color.RED);
            editPort.setHint("Port cannot be empty");
            return;
        }
        recvTask = new RecvLocationUpdate();
        recvTask.execute(portStr);
    }

    /**
     * Cancels the current asynchronous task.
     *
     * This stops the listening thread and notifies the user that it was cancelled. If there is no
     * thread running, it simply returns immediately.
     *
     * @param view Unused
     *
     * @author Shane Spoor
     */
    public void stopListening(View view)
    {
        if(recvTask == null || recvTask.isCancelled())
            return;

        recvTask.cancel(true);
        recvTask = null;
        log.append("Stopped\n");
    }
}
