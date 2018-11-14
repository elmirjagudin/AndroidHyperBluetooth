package ws.brab.pl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class HiperReader
{
    private static final String TAG = "BlueTest";

    static InputStream hiperIn = null;
    static OutputStream hiperOut = null;

    static BufferedReader br;

    public static void Init()
    {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null)
        {
            Log.i(TAG, "does not support bluetooth");
            return;
        }

        if (!ba.isEnabled())
        {
            Log.i(TAG, "bluetooth disabled");
            return;
        }

        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();

        Log.i(TAG, "ba is '" + ba + "' pairedDevices.size " + pairedDevices.size() + " ba.isEnabled() " + ba.isEnabled());

        BluetoothDevice hiper = null;

        if (pairedDevices.size() <= 0)
        {
            Log.i(TAG, "no paired devices");
            return;
        }

        // There are paired devices. Get the name and address of each paired device.
        for (BluetoothDevice device : pairedDevices)
        {
            String deviceName = device.getName();
            String mac = device.getAddress(); // MAC address

            Log.i(TAG, "dev name " + deviceName + " mac " + mac);

            if (mac.equals("00:07:80:36:02:C6"))
            {
                Log.i(TAG, "found Hiper");
                hiper = device;
                break;
            }
        }

        Log.i(TAG, "hiper is " + hiper);

        BluetoothSocket hiperSock = null;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            // hardcoded UUID for Serial port service
            // https://stackoverflow.com/questions/13964342/android-how-do-bluetooth-uuids-work
            hiperSock = hiper.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
        } catch (IOException e) {
            Log.i(TAG, "Socket's create() method failed", e);
            return;
        }


        Log.i(TAG, "hiperSock " + hiperSock);

        try
        {
            hiperSock.connect();

            hiperIn = hiperSock.getInputStream();
            hiperOut = hiperSock.getOutputStream();
            br = new BufferedReader(new InputStreamReader(hiperIn));

            hiperOut.write("em,/cur/term,/msg/nmea/GGA:.05\n\r".getBytes());
            hiperOut.write("set,/par/cur/term/imode,rtcm3\n\r".getBytes());

        } catch (IOException e) {
            Log.i(TAG, "hiperSock connect exception " + e);
        }
    }

    public static String GetNMEA()
    {
        try
        {
            return br.readLine();
        }
        catch (IOException e)
        {
            Log.i(TAG, "IO exception reading line" + e);
        }

        return null;
    }

    public static void PushRTCM(byte[] data, int len)
    {
        String str = "PushRTCM : ";
        for (int i = 0; i < len; i += 1)
        {
            str += Integer.toHexString(data[i]) + " ";
        }
        Log.i(TAG, str);

        try
        {
            hiperOut.write(data, 0, len);
        }
        catch (IOException e)
        {
            Log.i(TAG, "exception pushing RTCM " + e);
        }

    }
}
