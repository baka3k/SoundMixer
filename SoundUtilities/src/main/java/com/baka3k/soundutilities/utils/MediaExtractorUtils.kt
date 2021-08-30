/*
 * Created by LeQuangHiep on 02/21.
 * Copyright © 2021 baka3k@gmail.com. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details
 */
package com.baka3k.soundutilities.utils

import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.IOException

object MediaExtractorUtils {
    private const val TAG = "MediaExtractorUtils"
    private const val DEFAULT_AAC_BITRATE = 192 * 1000
    fun getSampleRate(originalAudioFormat: MediaFormat): Int {
        return originalAudioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
    }

    fun getDuration(source: String?): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(source)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMillisec = time!!.toLong()
        retriever.release()
        return timeInMillisec
    }

    fun selectTrack(extractor: MediaExtractor, audio: Boolean): Int {
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (audio) {
                if (mime!!.startsWith("audio/")) {
                    return i
                }
            } else {
                if (mime!!.startsWith("video/")) {
                    return i
                }
            }
        }
        return -100
    }

    fun getAudioBitrate(format: MediaFormat): Int {
        return if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
            format.getInteger(MediaFormat.KEY_BIT_RATE)
        } else {
            DEFAULT_AAC_BITRATE
        }
    }

    @Throws(IOException::class)
    fun getSampleRate(source: String?): Int {
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(source!!)
        val audioTrackIndex: Int =
            selectTrack(mediaExtractor, true)
        mediaExtractor.selectTrack(audioTrackIndex)
        val originalAudioFormat = mediaExtractor.getTrackFormat(audioTrackIndex)
        mediaExtractor.release()
        return originalAudioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
    }

    fun isRawDataFile(path: String?): Boolean {
        val mediaExtractor = MediaExtractor()
        try {
            mediaExtractor.setDataSource(path!!)
            val audioLine: Int = selectTrack(mediaExtractor, true)
            val mediaFormat = mediaExtractor.getTrackFormat(audioLine)
            val mimeType = mediaFormat.getString(MediaFormat.KEY_MIME)
            mediaExtractor.release()
            return MediaFormat.MIMETYPE_AUDIO_RAW == mimeType || MediaFormat.MIMETYPE_AUDIO_AAC == mimeType
        } catch (e: IOException) {
            Log.e(
                TAG,
                "isRawDataFile err:" + e.message
            )
        }
        return false
    }

    fun getAudioMaxBufferSize(format: MediaFormat): Int {
        return if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        } else {
            100 * 1000
        }
    }
}