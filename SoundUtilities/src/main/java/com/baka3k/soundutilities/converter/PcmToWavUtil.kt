/*
 *
 *  * Created by LeQuangHiep on 08/2021.
 *  * Copyright © 2021 baka3k@gmail.com. All rights reserved.
 *  * Licensed under the MIT license. See LICENSE file in the project root for details
 *
 */

package com.baka3k.soundutilities.converter

import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class PcmToWavUtil {
    private var mBufferSize = 0//Cached audio size
    private var mSampleRate: Int = 8000 // 8000|16000
    private var mChannelConfig = AudioFormat.CHANNEL_IN_STEREO //stereo
    private var mChannelCount = 2
    private var mEncoding = AudioFormat.ENCODING_PCM_16BIT

    constructor() {
        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mEncoding)
    }

    constructor(sampleRate: Int, channelConfig: Int, channelCount: Int, encoding: Int):this() {
        mSampleRate = sampleRate
        mChannelConfig = channelConfig
        mChannelCount = channelCount
        mEncoding = encoding
    }

    /**
     * convert pcm to wav
     *
     * @param inFilename
     * @param outFilename
     */
    fun pcmToWav(inFilename: String?, outFilename: String?) {
        val fileInputStream: FileInputStream
        val fileOutputStream: FileOutputStream
        val totalAudioLen: Long
        val totalDataLen: Long
        val longSampleRate = mSampleRate.toLong()
        val channels = mChannelCount
        val byteRate = (16 * mSampleRate * channels / 8).toLong()
        val data = ByteArray(mBufferSize)
        try {
            fileInputStream = FileInputStream(inFilename)
            fileOutputStream = FileOutputStream(outFilename)
            totalAudioLen = fileInputStream.channel.size()
            totalDataLen = totalAudioLen + 36
            writeWaveFileHeader(
                fileOutputStream, totalAudioLen, totalDataLen,
                longSampleRate, channels, byteRate
            )
            while (fileInputStream.read(data) != -1) {
                fileOutputStream.write(data)
            }
            fileInputStream.close()
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * add header to wav
     */
    @Throws(IOException::class)
    private fun writeWaveFileHeader(
        out: FileOutputStream, totalAudioLen: Long,
        totalDataLen: Long, longSampleRate: Long, channels: Int, byteRate: Long
    ) {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF/WAVE header
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte() //WAVE
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte() //data
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }
}