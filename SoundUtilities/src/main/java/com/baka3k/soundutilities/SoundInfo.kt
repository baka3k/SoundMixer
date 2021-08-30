/*
 *
 *  * Created by LeQuangHiep on 08/2021.
 *  * Copyright © 2021 baka3k@gmail.com. All rights reserved.
 *  * Licensed under the MIT license. See LICENSE file in the project root for details
 *
 */

package com.baka3k.soundutilities

import android.util.Log
import com.baka3k.soundutilities.utils.MediaExtractorUtils

class SoundInfo {
    var path: String
    var startTime: Long
    var endTime: Long

    private constructor(path: String, startTime: Long, endTime: Long) {
        this.path = path
        this.startTime = startTime
        this.endTime = endTime
    }

    constructor(path: String, startTime: Long) {
        this.path = path
        this.startTime = startTime

        endTime = (startTime + MediaExtractorUtils.getDuration(path))
    }

    override fun toString(): String {
        return ("path = " + path + ", startTime = " + startTime
                + ", endTime = " + endTime)
    }

    companion object {
        fun clone(info: SoundInfo): SoundInfo {
            return SoundInfo(info.path, info.startTime, info.endTime)
        }
    }
}
