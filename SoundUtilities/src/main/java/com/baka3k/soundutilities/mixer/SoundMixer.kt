/*
 *
 *  * Created by LeQuangHiep on 08/2021.
 *  * Copyright © 2021 baka3k@gmail.com. All rights reserved.
 *  * Licensed under the MIT license. See LICENSE file in the project root for details
 *
 */

package com.baka3k.soundutilities.mixer

import com.baka3k.soundutilities.WavFile
import com.baka3k.soundutilities.converter.SoundConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class SoundMixer(private val cacheFile: File) {
    @Throws(IOException::class)
    suspend fun mix(
        inputFile1: String,
        inputFile2: String,
        outPutMixing: String,
        duration: Int
    ) = withContext(Dispatchers.IO) {
        var tempFile1 = ""
        var tempFile2 = ""

        val soundConverter = SoundConverter(cacheFile)
        val defer1 = async {
            if (!inputFile1.endsWith(".wav")) {
                tempFile1 = cacheFile.absolutePath + "/temp1.wav"
                soundConverter.convertMp3ToWav(inputFile1, tempFile1)
                tempFile1
            } else {
                inputFile1
            }
        }
        val defer2 = async {
            if (!inputFile2.endsWith(".wav")) {
                tempFile2 = cacheFile.absolutePath + "/temp2.wav"
                soundConverter.convertMp3ToWav(inputFile2, tempFile2)
                tempFile2
            } else {
                inputFile2
            }
        }


        val wavFile = WavFile()
        wavFile.mix(defer1.await(), defer2.await(), outPutMixing, duration)
        // delete cache file *.wav
        deleteCacheFile(tempFile1)
        deleteCacheFile(tempFile2)
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