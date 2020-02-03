package io.horizontalsystems.tor.core.utils

import java.io.File

class FileUtils{
    companion object{

        fun setExecutable(fileBin: File) {
            fileBin.setReadable(true)
            fileBin.setExecutable(true)
            fileBin.setWritable(false)
            fileBin.setWritable(true, true)
        }
    }
}