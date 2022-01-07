//
// Created by gongguopei on 2019/5/31.
//

//
// Created by wangjiang on 2019/4/8.
//

#include <jni.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <stdlib.h>
#include <fcntl.h>

#include <string>
#include <deque>
#include <cstdlib>
#include <string.h>
// #include "filter_16_v2.cpp"
#include "streamswtqua.h"
#include "streamswtqua.cpp"
#include "commalgorithm.h"
#include "commalgorithm.cpp"
#include "swt.h"
#include "swt.cpp"
#include <android/log.h>
#include <assert.h>

#define APPNAME "MyApp"
extern "C"

JNIEXPORT jshortArray JNICALL
Java_com_viatom_lpble_ble_WaveFilter_shortfilter(JNIEnv *env, jobject thiz, jshortArray inShorts) {

    short *shortArray;
    jsize arraySize;
    arraySize = (*env).GetArrayLength(inShorts);

    deque <double > inputt;
    inputt.clear();

    jboolean *isCopy = (jboolean *)malloc(sizeof(jboolean));
    shortArray =(*env).GetShortArrayElements(inShorts, isCopy);
    for(int j = 0; j < arraySize; j++ )
    {
        inputt.push_back((jdouble) shortArray[j]);
    }
    int inputLength = (int) inputt.size();
//    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "doubleArraySize == %d", inputLength);

    int i;
    StreamSwtQua streamSwtQua;
    deque <double> outputPoints;
    deque <double> allSig;
    deque <double> outputsize;
    int ReduntLength;
    int MultipleSize;
    MultipleSize = inputLength/256;
    ReduntLength = inputLength - 256 * MultipleSize;

    if(ReduntLength != 0)
    {
        for(i = inputLength; i < (MultipleSize+1)*256; i++)
        {
            inputt.push_back(0);
        }
    }



    if(ReduntLength == 0)
    {
        for (i = 0; i < 256 * MultipleSize; ++i)
        {
            streamSwtQua.GetEcgData(inputt[i], outputPoints);

            for (int j = 0; j < outputPoints.size(); ++j)
            {
                allSig.push_back(outputPoints[j]);
            }
        }

    }
    else{
        for(i = 0; i < inputt.size(); i++)
        {
            streamSwtQua.GetEcgData(inputt[i], outputPoints);

            for (int j = 0; j < outputPoints.size(); ++j)
            {
                allSig.push_back(outputPoints[j]);
            }
        }
        if(ReduntLength < 192)
        {
            for(i = 0; i < 192 -ReduntLength; i++)
            {
                allSig.pop_back();
            }
        }
    }

    long length = allSig.size();
//    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "length == %ld", length);
    short array[length];
    for(i = 0; i < length; i++)
    {
        array[i] = (short) allSig[i];
    }

    jsize size = (jsize) allSig.size();
    jshortArray result = (*env).NewShortArray(size);
    (*env).SetShortArrayRegion(result, 0, size, array);

    return result;
}

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_viatom_lpble_ble_WaveFilter_offlineFilter(JNIEnv *env, jobject thiz, jdoubleArray doubles) {

    double *doubleArray;
    jsize doubleArraySize;
    doubleArraySize = (*env).GetArrayLength(doubles);

    deque <double > inputt;
    inputt.clear();

    jboolean *isCopy = (jboolean *)malloc(sizeof(jboolean));
    doubleArray =(*env).GetDoubleArrayElements(doubles, isCopy);
    for(int j = 0; j < doubleArraySize; j++ )
    {
        inputt.push_back(doubleArray[j]);
    }
    int inputLength = (int) inputt.size();
//    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "doubleArraySize == %d", inputLength);

    int i;
    StreamSwtQua streamSwtQua;
    deque <double> outputPoints;
    deque <double> allSig;
    deque <double> outputsize;
    int ReduntLength;
    int MultipleSize;
    MultipleSize = inputLength/256;
    ReduntLength = inputLength - 256 * MultipleSize;

    if(ReduntLength != 0)
    {
        for(i = inputLength; i < (MultipleSize+1)*256; i++)
        {
            inputt.push_back(0);
        }
    }



    if(ReduntLength == 0)
    {
        for (i = 0; i < 256 * MultipleSize; ++i)
        {
            streamSwtQua.GetEcgData(inputt[i], outputPoints);

            for (int j = 0; j < outputPoints.size(); ++j)
            {
                allSig.push_back(outputPoints[j]);
            }
        }

    }
    else{
        for(i = 0; i < inputt.size(); i++)
        {
            streamSwtQua.GetEcgData(inputt[i], outputPoints);

            for (int j = 0; j < outputPoints.size(); ++j)
            {
                allSig.push_back(outputPoints[j]);
            }
        }
        if(ReduntLength < 192)
        {
            for(i = 0; i < 192 -ReduntLength; i++)
            {
                allSig.pop_back();
            }
        }
    }

    long length = allSig.size();
//    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "length == %ld", length);
    double array[length];
    for(i = 0; i < length; i++)
    {
        array[i] = allSig[i];
    }

    jsize size = (jsize) allSig.size();
    jdoubleArray result = (*env).NewDoubleArray(size);
    (*env).SetDoubleArrayRegion(result, 0, size, array);

    return result;
}