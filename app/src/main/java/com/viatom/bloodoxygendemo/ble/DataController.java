package com.viatom.bloodoxygendemo.ble;

import android.util.Log;


import java.util.Arrays;

public class DataController {
    public static int index = 0;
    public static int indexC = 0;

    public static int[] amp = {5, 10 ,20};
    public static int ampKey = 0;

    public static int maxIndex;
    public static float mm2px;

    public static BatteryInfo batteryInfo;

    public static int hr = 0;
    public static int hrGATT = 0;

    // for wave
    public static float[] dataSrc;
    public static float[] dataSrcCollect; //用于显示采集时的高亮
    // received from device
    public static float[] dataRec = new float[0];

    public static void feed(float[] fs, boolean collecting) {
        if (fs == null || fs.length == 0) {
            fs = new float[5];
        }

        if (dataSrc == null) {
            dataSrc = new float[maxIndex];
        } else if(dataSrc.length == 0) {
            dataSrc = new float[maxIndex];
        }

        if (dataSrcCollect == null) {
            dataSrcCollect = new float[maxIndex];

        } else if (dataSrcCollect.length == 0) {
            dataSrcCollect = new float[maxIndex];
        }

        if(maxIndex == 0) {
            return;
        }

        for (int i = 0; i<fs.length; i++) {
            if(dataSrc.length != 0) {
                int tempIndex = (index + i) % dataSrc.length;
                dataSrc[tempIndex] = fs[i];
            }
        }

        for (int i = 0; i < fs.length; i++) {
            if (dataSrcCollect.length != 0) {
                int tempIndex = (index + i) % dataSrcCollect.length;
                if (collecting) {
                    dataSrcCollect[tempIndex] = 900;
                }else {
                    dataSrcCollect[tempIndex] = 0;
                }
            }
        }

        if(dataSrc.length != 0) {
            index = (index + fs.length) % dataSrc.length;
        }
        Log.d("dashboard", "DataController feed index: " + index+"maxIndex :" + maxIndex);

    }

    synchronized public static void receive(float[] fs) {
        Log.d("dashboard", "DataController receive: " + fs.length);

        if (fs == null || fs.length == 0) {
            return;
        }


        float[] temp = new float[dataRec.length + fs.length];
        System.arraycopy(dataRec, 0, temp, 0, dataRec.length);
        System.arraycopy(fs, 0, temp, dataRec.length, fs.length);

        dataRec = temp;

    }

    synchronized public static float[] draw(int n) {

        Log.d("dashboard", "DataController draw " + n);

        if (n == 0 || n > dataRec.length) {
            return null;
        }

        float[] res = new float[n];
        float[] temp = new float[dataRec.length - n];
        System.arraycopy(dataRec, 0, res, 0, n);
        System.arraycopy(dataRec, n, temp, 0, dataRec.length-n);


        dataRec = temp;

        return res;
    }

    synchronized public static void clear() {
//        float[] res = Arrays.copyOfRange(dataRec, 0, n-1);
        if(dataRec.length > 15) {
            float[] temp = Arrays.copyOfRange(dataRec, dataRec.length - 10, dataRec.length);
            dataRec = temp;
        }
    }
    public static void releaseAll(){
        dataRec = new float[0];
        dataSrc = null;
        dataSrcCollect = null;
    }
}
