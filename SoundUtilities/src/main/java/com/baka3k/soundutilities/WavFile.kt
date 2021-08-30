/*
 *
 *  * Created by LeQuangHiep on 08/2021.
 *  * Copyright © 2021 baka3k@gmail.com. All rights reserved.
 *  * Licensed under the MIT license. See LICENSE file in the project root for details
 *
 */

package com.baka3k.soundutilities

import android.util.Log
import com.baka3k.soundutilities.utils.WavUtils
import java.io.DataOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class WavFile {
    companion object {
        private val TAG = "WavFile"
        private val BUFFER_SIZE = 1024
        private val VOLUME_REDUCE = 0.3f
    }

    @Throws(IOException::class)
    fun mix(path1: String, path2: String, outputFile: String, duration: Int) {
        Log.d(TAG,"path1 $path1")
        Log.d(TAG,"path2 $path2")
        var fis1: FileInputStream? = null
        var fis2: FileInputStream? = null
        try {
            fis1 = FileInputStream(path1)
            fis2 = FileInputStream(path2)
            val channels = 2
            val longSampleRate: Long = WavUtils.SAMPLERATE
            val byteRate = WavUtils.RECORDER_BPP * longSampleRate * channels / 8
            val totalAudioLen = duration * byteRate / 1000
            var total = 0
            val totalDataLen: Long = totalAudioLen + WavUtils.LENGTH_EXTENDED
            var out: DataOutputStream? = null
            var read: Int
            val fc1 = fis1.channel
            val fc2 = fis2.channel
            val length1 = fc1.size()
            val length2 = fc2.size()
            try {
                out = DataOutputStream(FileOutputStream(outputFile))
                WavUtils.writeWaveFileHeader(
                    out,
                    totalAudioLen,
                    totalDataLen,
                    longSampleRate,
                    channels,
                    byteRate
                )
                val buff1 = ByteBuffer.allocate(BUFFER_SIZE)
                val buff2 = ByteBuffer.allocate(BUFFER_SIZE)
                var mixedBuffer: ByteBuffer? = null
                if (length1 == length2) {
                    while (fc1.read(buff1) > 0) {
                        read = fc2.read(buff2)
                        total += read
                        mixedBuffer = mixByteBuffer(buff1, buff2, total)
                        out.write(mixedBuffer!!.array())
                        buff1.clear()
                        buff2.clear()
                        mixedBuffer.clear()
                    }
                    mixedBuffer?.clear()
                } else {
                    if (length2 > length1) {
                        while (fc1.read(buff1) > 0) {
                            read = fc2.read(buff2)
                            total += read
                            mixedBuffer = mixByteBuffer(buff1, buff2, total)
                            out.write(mixedBuffer!!.array())
                            buff1.clear()
                            buff2.clear()
                            mixedBuffer.clear()
                        }
                        while (fc2.read(buff2) > 0) {
                            out.write(buff2.array())
                            buff2.clear()
                        }
                    } else {
                        while (fc2.read(buff2) > 0) {
                            read = fc1.read(buff1)
                            total += read
                            mixedBuffer = mixByteBuffer(buff1, buff2, total)
                            out.write(mixedBuffer!!.array())
                            buff1.clear()
                            buff2.clear()
                            mixedBuffer.clear()
                        }
                        while (fc1.read(buff1) > 0) {
                            out.write(buff1.array())
                            buff1.clear()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "#createWaveMixing():" + e.message, e)
            } finally {
                out?.close()
                fc1.close()
                fc2.close()
            }
        } finally {
            fis1?.close()
            fis2?.close()
        }
    }

    @Throws(InterruptedException::class)
    private fun mixByteBuffer(buff1: ByteBuffer, buff2: ByteBuffer, progress: Int): ByteBuffer? {
        val bytes1 = buff1.array()
        val bytes2 = buff2.array()
        val size1 = bytes1.size
        val output = ByteArray(size1)
        for (i in output.indices) {
            val samplef1 = bytes1[i] / 128.0f
            val samplef2 = bytes2[i] / 128.0f
            var mixed: Float
            mixed = samplef1 + samplef2
            if (mixed > 1.0f) mixed = 1.0f
            if (mixed < -1.0f) mixed = -1.0f
            val outputSample = (mixed * 128.0f).toInt().toByte()
            output[i] = outputSample
        }
        return ByteBuffer.wrap(output)
    }
}