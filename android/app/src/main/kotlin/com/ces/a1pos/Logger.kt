package com.ces.a1pos

import java.io.File
import java.time.LocalDateTime
import java.util.*

object Logger {

    var LOGFILE: File? = null

    var LOGDIRECTORY = ""

    fun debug(contents: String){
        val str = "-----------DEBUG ${Date()}----------- \n" +
                contents +
                "\n" +
                "------------END - KT DEBUG---------------\n"

        LOGFILE!!.appendText(str)
    }

    fun error(contents: String){
        val str = "-----------ERROR - KT ${Date()}----------- \n" +
                contents +
                "\n" +
                "------------END - KT ERROR---------------\n"

        LOGFILE!!.appendText(str)
    }

    fun info(contents: String){
        val str = "-----------INFO - KT ${Date()}----------- \n" +
                contents +
                "\n" +
                "------------END - KT INFO---------------\n"

        LOGFILE!!.appendText(str)
    }
}