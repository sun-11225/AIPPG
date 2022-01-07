package com.viatom.bloodoxygendemo.ext

import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import android.os.Environment.getExternalStorageState

/**
 * author: wujuan
 * created on: 2021/4/1 14:58
 * description:
 */



// Checks if a volume containing external storage is available
// for read and write.
fun Environment.isExternalStorageWritable(): Boolean {
    return getExternalStorageState() == MEDIA_MOUNTED
}


