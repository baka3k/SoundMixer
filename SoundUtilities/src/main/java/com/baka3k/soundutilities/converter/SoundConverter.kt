/*
 *
 *  * Created by LeQuangHiep on 08/2021.
 *  * Copyright © 2021 baka3k@gmail.com. All rights reserved.
 *  * Licensed under the MIT license. See LICENSE file in the project root for details
 *
 */

package com.baka3k.soundutilities.converter

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.util.Log
import com.baka3k.soundutilities.utils.MediaExtractorUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class SoundConverter(private val cacheFile: File) {
    companion object {
        private const val TIMEOUT_US = 200 // timeout to read buffer
        private const val TAG = "SoundConverter"
        private val freqIdxMap = hashMapOf(
            96000 to 0,
            88200 to 1,
            64000 to 2,
            48000 to 3,
            44100 to 4,
            32000 to 5,
            24000 to 6,
            22050 to 7,
            16000 to 8,
            12000 to 9,
            11025 to 10,
            8000 to 11,
            7350 to 12,
        )

    }

    @Throws(IOException::class)
    fun convertMp3ToWav(mp3Input: String, wavOutPut: String) {
        decodeMp3ToPCM(mp3Input, wavOutPut)
    }

    @SuppressLint("WrongConstant")
    @Throws(IOException::class)
    private fun decodeMp3ToPCM( mp3Input: String, wavOutput: String) {
        // MP3 => PCM => WAV
        // create file to cache PCM
        val pcmFile = File(cacheFile, "pcm_" + System.currentTimeMillis() + ".pcm")
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(mp3Input)
        val audioTrackIndex: Int = MediaExtractorUtils.selectTrack(mediaExtractor, true)
        mediaExtractor.selectTrack(audioTrackIndex)
        val originalAudioFormat = mediaExtractor.getTrackFormat(audioTrackIndex)
        val mediaCodec =
            MediaCodec.createDecoderByType(originalAudioFormat.getString(MediaFormat.KEY_MIME)!!)
        val maxBufferSize: Int = MediaExtractorUtils.getAudioMaxBufferSize(originalAudioFormat)
        val sampleRate = originalAudioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val oriChannelCount = originalAudioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        var channelConfig = AudioFormat.CHANNEL_IN_MONO
        if (oriChannelCount == 2) {
            channelConfig = AudioFormat.CHANNEL_IN_STEREO
        }
        val encodeMediaFormat = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            sampleRate,
            oriChannelCount
        )
        encodeMediaFormat.setInteger(
            MediaFormat.KEY_BIT_RATE,
            MediaExtractorUtils.getAudioBitrate(originalAudioFormat)
        )
        encodeMediaFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        checkCsd(
            encodeMediaFormat,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC,
            sampleRate,
            oriChannelCount
        )

        // start decode
        val buffer = ByteBuffer.allocateDirect(maxBufferSize)
        val info = MediaCodec.BufferInfo()
        mediaCodec.configure(originalAudioFormat, null, null, 0)
        mediaCodec.start()
        var decodeDone = false
        var decodeInputDone = false
        val writeChannel = FileOutputStream(pcmFile).channel
        val outputBuffers: Array<ByteBuffer>? = null
        try {
            while (!decodeDone) {
                if (!decodeInputDone) {
                    var eof = false
                    val decodeInputIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_US.toLong())
                    if (decodeInputIndex >= 0) {
                        val sampleTimeUs = mediaExtractor.sampleTime
                        if (sampleTimeUs == -1L) {
                            eof = true
                        }
                        if (eof) {
                            decodeInputDone = true
                            mediaCodec.queueInputBuffer(
                                decodeInputIndex,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                        } else {
                            info.size = mediaExtractor.readSampleData(buffer, 0)
                            info.presentationTimeUs = sampleTimeUs
                            info.flags = mediaExtractor.sampleFlags
                            var inputBuffer: ByteBuffer? =
                                mediaCodec.getInputBuffer(decodeInputIndex)
                            if (inputBuffer != null) {
                                inputBuffer.put(buffer)
                            } else {
                                Log.i(TAG, "#decodeMp3ToPCM() err inputbuffer null ")
                            }
                            mediaCodec.queueInputBuffer(
                                decodeInputIndex,
                                0,
                                info.size,
                                info.presentationTimeUs,
                                info.flags
                            )
                            mediaExtractor.advance()
                        }
                    }
                }
                while (!decodeDone) {
                    val outputBufferIndex =
                        mediaCodec.dequeueOutputBuffer(info, TIMEOUT_US.toLong())
                    if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (outputBufferIndex < 0) {
                        //ignore
                        Log.e(
                            TAG,
                            "unexpected result from audio decoder.dequeueOutputBuffer: $outputBufferIndex"
                        )
                    } else {
                        if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            decodeDone = true
                        } else {
                            var decodeOutputBuffer: ByteBuffer? =
                                mediaCodec.getOutputBuffer(outputBufferIndex)
                            writeChannel.write(decodeOutputBuffer)
                        }
                        mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                    }
                }
            }
        } finally {
            writeChannel.close()
            mediaExtractor.release()
            mediaCodec.stop()
            mediaCodec.release()
        }
        PcmToWavUtil(
            sampleRate,
            channelConfig,
            oriChannelCount,
            AudioFormat.ENCODING_PCM_16BIT
        ).pcmToWav(pcmFile.absolutePath, wavOutput)
        if (pcmFile.exists()) {
            pcmFile.delete()
        }
    }

    private fun checkCsd(
        audioMediaFormat: MediaFormat,
        profile: Int,
        sampleRate: Int,
        channel: Int
    ) {

        var freqIdx: Int = if (freqIdxMap.containsKey(sampleRate)) {
            freqIdxMap[sampleRate]!!
        } else {
            4
        }
        val csd = ByteBuffer.allocate(2)
        csd.put(0, (profile shl 3 or freqIdx shr 1).toByte())
        csd.put(1, (freqIdx and 0x01 shl 7 or channel shl 3).toByte())
        audioMediaFormat.setByteBuffer("csd-0", csd)
    }
}