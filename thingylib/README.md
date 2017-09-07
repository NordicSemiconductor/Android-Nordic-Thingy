# Thingy Library for Android

### About Project

Thingy Library for android aims to ease managing, exploring, programming and developing applications on the Thingy platform. This library can be included in your Android project to develop your own applications with the Thingy:52 device.

### Requirements

* An Android 4.3+ device.

* Support for the Bluetooth 4.0 technology is required. Android introduced Bluetooth Smart in version 4.3. Android 4.4.4 or newer is recommended for better user experience.

* Android Studio IDE or Eclipse ADT

* Projects are compatible with Android Studio and the Gradle build engine. It is possible to convert them to Eclipse ADT projects. See Integration for more details.

* Thingy:52 device.

### Integration

The Thingy Library is compatible as such with Android Studio IDE. If you are using Eclipse ADT, you will have to convert the project to match the Eclipse project structure.

#### Android Studio

Follow the steps below to include the library to your project.

1. Clone the project into your projects root, for example to *AndroidstudioProjects*.
2. Add the **thingylib** module to your project:
    1. Add **include ':thingylib'** to the *settings.gradle* file: 
    ```
    include ':thingylib'
    ```
    2. Open Project Structure -> Modules -> app -> Dependencies tab and add **thingylib** module dependency.

#### Eclipse

1. Clone the project into a temporary location.
2. Create an empty *ThingyLibrary* project in Eclipse. Make it a library.
3. Copy the content of *java* code folder to the *src*.
4. Copy the content of the *res* folder to the *res* in your Eclipse project.
5. Make sure that *android support library v4* is available in *libs* folders. It should have been added automatically when creating the project.
6. In your application project, open Properties->Android and add ThingyLibrary as a library.

### Usage

Follow the steps below to start using the Thingy Library in your own project

1. Extend the **BaseThingyService** abstract class defined in the Thingy Library in your project. In addition extend the **BaseThingyBinder** in your **ThingyService** so that you can create your own functionality related to the application in side this Binder. 
Don't forget to define the **ThingyService** in your *AndroidManifest.xml*.

```
package com.example.coolthingyproject;

import no.nordicsemi.android.dfu.ThingyService;
import android.app.Activity;

public class ThingyService extends BaseThingyService {
    
    public class ThingyBinder extends BaseThingyBinder {         
        @Override
        public ThingyConnection getThingyConnection(BluetoothDevice device) {
            return mThingyConnections.get(device);
        }
         
         //You can write your own functionality here         
         
    }

}
```

By extending the existing binder from the **BaseThingyService** as shown above you should be able to write your own functionality within the **ThingyBinder** and call these functionality by using the same binder. 
 
2. In order to start using the ThingyLibrary make sure to create an instance of it by adding this line ```ThingySdkManager.getInstance()``` inside your activities' ```onCreate()```. 
This is an API interface to the Library and provides all the required helper methods to create your own Android application for Thingy:52.
Just call this line ```ThingySdkManager.getInstance()``` inside ```onCreate()``` as shown below.

```
package com.example.coolthingyproject;

import com.example.coolthingyproject.MyActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThingySdkManager thingySdkManager = ThingySdkManager.getInstance();
    }
}
```

3. Next is to make sure you bind to your service inside ```onStart()``` and ```onStop()``` methods in your activities by adding the following lines.
Also make sure to implement the ```ThingySdkManager.ServiceConnectionListener``` in your activity to get the service connection callbacks.
The service binding is handled within the ThingySdkManager and ```onServiceConnected``` will be called when the service is bound.
Also as mentioned in step 1. you can get the service binder inside ```onServiceConnected()``` as shown below to access your own functionality declared in the binder in ThingyService.

```
package com.example.coolproject;

import com.example.coolthingyproject.MyActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import no.nordicsemi.android.thingylib.ThingySdkManager.ServiceConnectionListener;
import com.example.coolthingyproject.ThingyService;

public class MyActivity extends AppCompatActivity implements ServiceConnectionListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThingySdkManager thingySdkManager = ThingySdkManager.getInstance();
    }
              
    @Override
    protected void onStart() {
        super.onStart();
        mThingySdkManager.bindService(this, ThingyService.class);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mThingySdkManager.unbindService(this);
    }
    
    @Override
    public void onServiceConnected() {
        //Use this binder to access you own API methods declared in the binder inside ThingyService
        mBinder = (ThingyService.ThingyBinder) mThingySdkManager.getThingyBinder();
    }
}
```

4. The library will handle the connection, service discovery and reading all the characteristics for you. After binding to the service as mentioned in step 3 you can connect to the preferred Thingy:52 device.
 All you have to do is just call the following line ``` mThingySdkManager.connectToThingy(this, device, ThingyService.class);``` as shown on the 5th step once you receive the ```onServiceDiscoveryCompleted(BluetoothDevice device)```
you can start to enable the notifications. 

Please note that you will have to scan for your Thingy:52 device before connecting and you can follow the example app on how to scan for a Thingy:52 device.

5. In order to get updates on from sensors you will have to implement ThingyListener. The service will send local broadcasts using LocalBroadcastManager. 
You can register and unregister the broadcast receivers as shown in the ```onStart()``` and ```onStop()``` methods. 

```
private final ThingyListener mThingyListener = new ThingyListener() {
    @Override
    public void onDeviceConnected(BluetoothDevice device, int connectionState) {
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
    }

    @Override
    public void onServiceDiscoveryCompleted(BluetoothDevice device) {
        //After the service discovery is completed you may enable notifications for each and every sensor you wish
        mThingySdkManager.enableTemperatureNotifications(mDevice, true);
        mThingySdkManager.enablePressureNotifications(mDevice, true);
        mThingySdkManager.enableHumidityNotifications(mDevice, true);
        mThingySdkManager.enableAirQualityNotifications(mDevice, true);
        mThingySdkManager.enableColorNotifications(mDevice, true);
        mThingySdkManager.enableButtonStateNotification(mDevice, true);
        //...
    }

    @Override
    public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {

    }
    ///...
};

@Override
protected void onStart() {
    super.onStart();
    mThingySdkManager.bindService(this, ThingyService.class);
    
    ThingyListenerHelper.registerThingyListener(this, mThingyListener);
}

@Override
protected void onStop() {
    super.onStop();
    mThingySdkManager.unbindService(this);
    
    ThingyListenerHelper.unregisterThingyListener(this, mThingyListener);
}

@Override
public void onServiceConnected() {
    //Use this binder to access you own API methods declared in the binder inside ThingyService
    mBinder = (ThingyService.ThingyBinder) mThingySdkManager.getThingyBinder();
    
    //Connect to the Thingy:52 Bluetooth device you wish to connect to after binding 
    mThingySdkManager.connectToThingy(this, mDevice, ThingyService.class);    
}

```


Now its all setup and good to start writing your own application for thingy. Check out the example app provided to start building your own application.

### Example

Check the Android project: Nordic Thingy ([here](https://github.com/NordicSemiconductor/Android-Nordic-Thingy "Nordic Thingy")).