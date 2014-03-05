package ca.bcit.A00852406;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 *
 */
public class ChooseModeActivity extends Activity {

    /**
     * Initiliases the activity and sets the layout.
     *
     * @param savedInstanceState The saved state from the last time the activity was running.
     *                           This may be null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fullscreen);
    }

    /**
     * Called after the activity has been created.
     *
     * If there's saved state information (e.g., if the user rotated the screen), it can be restored
     * here.
     * @param savedInstanceState The saved state from the last time the activity was running. This may
     *                           be null.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Starts the Client activity, where the user can gather location data and send it to the
     * server.
     *
     * @param view Used by the other activity when receiving the intent.
     */
    public void startClient(View view)
    {
        Intent intent = new Intent(this, ClientActivity.class);
        startActivity(intent);
    }

    /**
     * Starts the server activity.
     *
     * @param view Unused
     *
     * @author Shane Spoor
     */
    public void startServer(View view)
    {
        Intent intent = new Intent(this, ServerActivity.class);
        startActivity(intent);
    }

}
