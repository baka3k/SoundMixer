/*
 *
 *  * Created by LeQuangHiep on 08/2021.
 *  * Copyright © 2021 baka3k@gmail.com. All rights reserved.
 *  * Licensed under the MIT license. See LICENSE file in the project root for details
 *
 */

package com.baka3k.soundutilities.concat

import android.util.Log
import com.baka3k.soundutilities.SoundInfo
import com.baka3k.soundutilities.converter.SoundConverter
import com.baka3k.soundutilities.utils.MediaExtractorUtils
import com.baka3k.soundutilities.utils.WavUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Arrays

class SoundConcat(private val cacheFile: File) {
    companion object {
        private const val BUFFER_SIZE = 1024
        private const val TAG = "SoundConcat"
    }

    private val soundConverter = SoundConverter(cacheFile)
    private fun fill(buf: ByteBuffer, b: Byte) {
        if (buf.hasArray()) {
            val offset = buf.arrayOffset()
            Arrays.fill(buf.array(), offset + buf.position(), offset + buf.limit(), b)
            buf.position(buf.limit())
        } else {
            var remaining = buf.remaining()
            while (remaining-- > 0) {
                buf.put(b)
            }
        }
    }

    suspend fun concatWaveFile(filesMerge: ArrayList<SoundInfo>, outputPath: String) =
        withContext(Dispatchers.IO) {

            val out: DataOutputStream
            var totalAudioLen: Long = 0
            var totalDataLen: Long = 0
            val longSampleRate: Long = WavUtils.SAMPLERATE
            val channels = 2
            val byteRate: Long = WavUtils.RECORDER_BPP * WavUtils.SAMPLERATE * channels / 8
            try {
                out = DataOutputStream(FileOutputStream(outputPath))
                filesMerge.forEach {
                    totalAudioLen += it.endTime * byteRate / 1000
                }
                totalDataLen = totalAudioLen + WavUtils.LENGTH_EXTENDED
                WavUtils.writeWaveFileHeader(
                    out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate
                )
                val data = ByteBuffer.allocate(BUFFER_SIZE)
                for (i in filesMerge.indices) {
                    val sound: SoundInfo = filesMerge[i]
                    val currentProcessingFile = FileInputStream(sound.path)
                    val duration: Long = MediaExtractorUtils.getDuration(sound.path)
                    if (duration > 0) {
                        currentProcessingFile.skip(44) //Skip header wav
                        while (currentProcessingFile.channel.read(data) > 0) {
                            out.write(data.array())
                            data.clear()
                        }
                    }
                    currentProcessingFile.close()
                }
                out.close()
            } catch (e: IOException) {
                Log.e(TAG, "#combineWaveFile() error $e")
            }
        }

    suspend fun concatMP3File(inputFile1: String, inputFile2: String, outputPath: String) =
        withContext(Dispatchers.IO) {
            var tempFile1 = ""
            var tempFile2 = ""
            val defer1 = async {
                if (!inputFile1.endsWith(".wav")) {
                    tempFile1 = cacheFile.absolutePath + "/temp11.wav"
                    soundConverter.convertMp3ToWav(inputFile1, tempFile1)
                    tempFile1
                } else {
                    inputFile1
                }
            }
            val defer2 = async {
                if (!inputFile2.endsWith(".wav")) {
                    tempFile2 = cacheFile.absolutePath + "/temp21.wav"
                    soundConverter.convertMp3ToWav(inputFile2, tempFile2)
                    tempFile2
                } else {
                    inputFile2
                }
            }
            val fileToConcat1 = defer1.await()
            val fileToConcat2 = defer2.await()
            val listSoundInfo = arrayListOf<SoundInfo>()
            listSoundInfo.add(SoundInfo(fileToConcat1, 0))
            listSoundInfo.add(SoundInfo(fileToConcat2, 0))
            concatWaveFile(listSoundInfo, outputPath)
            deleteCacheFile(fileToConcat1)
            deleteCacheFile(fileToConcat2)
        }

    private fun deleteCacheFile(path: String) {
        if (path.isNotEmpty()) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}