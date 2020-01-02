package com.example.a1pos

import com.pax.poslink.CommSetting

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException


object Settings {

    val FILENAME = "setting.ini"


    private val Deft = ""

    private val SectionComm = "COMMUNICATE"
    private val TagComm = "CommType"
    private val TagIp = "IP"
    private val TagPortnum = "SERIALPORT"
    private val TagBaudrate = "BAUDRATE"
    private val TagPort = "PORT"
    private val TagTimeout = "TIMEOUT_M"
    private val TagMacAddr = "MACADDR"
    private val TAG_ENABLE_PROXY = "ENABLE_PROXY"

    fun saveCommSettingToFile(fileName: String, commsetting: CommSetting): Boolean {
        val ini = IniFile(fileName)
        ini.setSection(SectionComm)

        var bDone = ini.write(TagComm, commsetting.type)
        bDone = bDone and ini.write(TagTimeout, commsetting.timeOut)
        //        bDone &= ini.write(TagPortnum, commsetting.getSerialPort());
        //        bDone &= ini.write(TagBaudrate, commsetting.getBaudRate());
        //        bDone &= ini.write(TagIp, commsetting.getDestIP());
        //        bDone &= ini.write(TagPort, commsetting.getDestPort());
        //        bDone &= ini.write(TagMacAddr, commsetting.getMacAddr());
        //        bDone &= ini.write(TAG_ENABLE_PROXY, String.valueOf(commsetting.isEnableProxy()));
        return bDone
    }

    fun getCommSettingFromFile(fileName: String): CommSetting {
        val ini = IniFile(fileName)
        ini.setSection(SectionComm)

        val commsetting = CommSetting()
        commsetting.timeOut = ini.read(TagTimeout, Deft)
        commsetting.type = ini.read(TagComm, Deft)
        //        commsetting.setSerialPort(ini.read(TagPortnum, Deft));
        //        commsetting.setBaudRate(ini.read(TagBaudrate, Deft));
        //        commsetting.setDestIP(ini.read(TagIp, Deft));
        //        commsetting.setDestPort(ini.read(TagPort, Deft));
        //        commsetting.setMacAddr(ini.read(TagMacAddr, Deft));
        //        String enableProxy = ini.read(TAG_ENABLE_PROXY, Deft);
        //        if (!TextUtils.isEmpty(enableProxy)) {
        //            commsetting.setEnableProxy(Boolean.parseBoolean(enableProxy));
        //        }
        return commsetting
    }

}


internal class IniFile(private val m_fileName: String) {
    private var m_section: String? = null


    init {

        val fconfig = File(m_fileName)
        if (fconfig.exists()) {
            try {
                val command = "chmod 666 $m_fileName"
                val runtime = Runtime.getRuntime()
                runtime.exec(command)
            } catch (e: IOException) {
                println("chmod 666 failed!")
            }

        } else {
            try {
                if (fconfig.createNewFile()) {
                    try {
                        val command = "chmod 666 $m_fileName"
                        val runtime = Runtime.getRuntime()
                        runtime.exec(command)
                    } catch (e: IOException) {
                        println("chmod 666 failed!")
                    }

                }
            } catch (e: IOException) {
                //e.printStackTrace();
            }

        }
    }

    fun setSection(section: String) {
        m_section = section
    }

    fun write(key: String, value: String): Boolean {
        return write_profile_string(m_section, key, value, m_fileName) == 1
    }

    fun read(key: String, default_value: String): String {
        val buf = StringBuffer(4096)
        read_profile_string(m_section, key, buf, buf.capacity(), default_value, m_fileName)
        return buf.toString()
    }

    companion object {

        val MAX_INI_FILE_SIZE = 1024 * 16

        private fun load_ini_file(file: String, buf: StringBuffer, file_size: IntArray): Int {

            var `in`: FileReader? = null
            try {
                val fconfig = File(file)
                if (!fconfig.exists()) {
                    return 0
                }
                `in` = FileReader(file)
                file_size[0] = 0

                val data = CharArray(MAX_INI_FILE_SIZE)

                val num = `in`.read(data)
                if (num > 0) {
                    val str = String(data, 0, num)
                    buf.delete(0, buf.capacity())
                    buf.append(str)
                    file_size[0] = num
                }
                `in`.close()
                return 1
            } catch (e: IOException) {
                //Do nothing
            } finally {
                try {
                    `in`?.close()
                } catch (e: IOException) {
                    //Do nothing
                }

            }
            return 0
        }

        private fun newline(c: Char): Int {
            return if ('\n' == c || '\r' == c) 1 else 0
        }

        private fun left_barce(c: Char): Int {
            return if ('[' == c) 1 else 0
        }

        private fun right_brace(c: Char): Int {
            return if (']' == c) 1 else 0
        }

        private fun parse_file(section: String?, key: String, buf: String, sec_s: IntArray, sec_e: IntArray,
                               key_s: IntArray, key_e: IntArray, value_s: IntArray, value_e: IntArray): Int {
            var i = 0

            value_e[0] = -1
            value_s[0] = value_e[0]
            key_s[0] = value_s[0]
            key_e[0] = key_s[0]
            sec_e[0] = key_e[0]
            sec_s[0] = sec_e[0]

            while (i < buf.length) {
                //find the section

                if ((0 == i || newline(buf[i - 1]) == 1) && left_barce(buf[i]) == 1) {
                    val section_start = i + 1

                    //find the ']'
                    do {
                        i++
                    } while (right_brace(buf[i]) == 0 && i < buf.length)

                    if (section == buf.substring(section_start, i)) {
                        var newline_start = 0

                        i++

                        //Skip over space char after ']'
                        while (buf[i] == ' ') {
                            i++
                        }

                        //find the section
                        sec_s[0] = section_start
                        sec_e[0] = i

                        while (i < buf.length && (newline(buf[i - 1]) == 0 || left_barce(buf[i]) == 0)) {
                            //get a new line
                            newline_start = i

                            while (newline(buf[i]) == 0 && i < buf.length) {
                                i++
                            }

                            //now i  is equal to end of the line
                            var j = newline_start

                            if (';' != buf[j])
                            //skip over comment
                            {
                                while (j < i && buf[j] != '=') {
                                    j++
                                    if ('=' == buf[j]) {
                                        if (key == buf.substring(newline_start, j)) {
                                            //find the key ok
                                            key_s[0] = newline_start
                                            key_e[0] = j - 1

                                            value_s[0] = j + 1
                                            value_e[0] = i
                                            return 1
                                        }
                                    }
                                }
                            }

                            i++
                        }
                    }
                } else {
                    i++
                }
            }
            return 0
        }

        fun read_profile_string(section: String?, key: String, value: StringBuffer,
                                size: Int, default_value: String?, file: String): Int {
            val buf = StringBuffer(MAX_INI_FILE_SIZE)

            val file_size = IntArray(1)
            val sec_s = IntArray(1)
            val sec_e = IntArray(1)
            val key_s = IntArray(1)
            val key_e = IntArray(1)
            val value_s = IntArray(1)
            val value_e = IntArray(1)

            value_e[0] = 0
            value_s[0] = value_e[0]
            key_e[0] = value_s[0]
            key_s[0] = key_e[0]
            sec_e[0] = key_s[0]
            sec_s[0] = sec_e[0]
            file_size[0] = sec_s[0]
            //check parameters


            if (load_ini_file(file, buf, file_size) == 0) {
                if (default_value != null) {
                    value.delete(0, value.length)
                    value.append(default_value)
                }
                return 0
            }

            if (parse_file(section, key, buf.toString(), sec_s, sec_e, key_s, key_e, value_s, value_e) == 0) {
                if (default_value != null) {
                    value.delete(0, value.length)
                    value.append(default_value)
                }
                return 0 //not find the key
            } else {
                var cpcount = value_e[0] - value_s[0]

                if (size - 1 < cpcount) {
                    cpcount = size - 1
                }

                value.delete(0, value.length)
                value.append(buf.toString().substring(value_s[0], value_s[0] + cpcount))

                return 1
            }
        }

        /**
         * write a profile string to a ini file
         *
         * @param section [in] name of the section,can't be NULL and empty string
         * @param key     [in] name of the key pairs to value, can't be NULL and empty string
         * @param value   [in] profile string value
         * @param file    [in] path of ini file
         * @return 1 : success\n 0 : failure
         */
        fun write_profile_string(section: String?, key: String,
                                 value: String, file: String): Int {
            val buf = StringBuffer(MAX_INI_FILE_SIZE)
            val w_buf = StringBuffer(MAX_INI_FILE_SIZE)
            val file_size = IntArray(1)
            val sec_s = IntArray(1)
            val sec_e = IntArray(1)
            val key_s = IntArray(1)
            val key_e = IntArray(1)
            val value_s = IntArray(1)
            val value_e = IntArray(1)
            value_e[0] = 0
            value_s[0] = value_e[0]
            key_e[0] = value_s[0]
            key_s[0] = key_e[0]
            sec_e[0] = key_s[0]
            sec_s[0] = sec_e[0]
            file_size[0] = sec_s[0]


            //check parameters

            if (load_ini_file(file, buf, file_size) == 0) {
                sec_s[0] = -1
            } else {
                parse_file(section, key, buf.toString(), sec_s, sec_e, key_s, key_e, value_s, value_e)
            }
            if (-1 == sec_s[0]) {

                if (0 == file_size[0]) {
                    w_buf.insert(file_size[0], "[$section]\n$key=$value\n")

                } else {
                    //not find the section, then add the new section at end of the file
                    w_buf.delete(0, w_buf.capacity())
                    w_buf.append(buf.toString().substring(0, file_size[0]))
                    w_buf.insert(file_size[0], "\n[$section]\n$key=$value\n")
                }
            } else if (-1 == key_s[0]) {
                //not find the key, then add the new key=value at end of the section

                w_buf.delete(0, w_buf.capacity())
                w_buf.append(buf.toString().substring(0, sec_e[0] + 1))
                w_buf.append("$key=$value\n")
                w_buf.append(buf.toString().substring(sec_e[0] + 1))
            } else {
                //update value with new value
                w_buf.delete(0, w_buf.capacity())

                w_buf.append(buf.toString().substring(0, value_s[0]))

                w_buf.append(value)
                if (value_e[0] < file_size[0]) {
                    w_buf.append(buf.toString().substring(value_e[0]))
                }

            }

            var out: FileWriter? = null
            try {
                out = FileWriter(file)

                out.write(w_buf.toString())
                out.flush()
                out.close()
                return 1
            } catch (e: Exception) {
                // Do nothing
            } finally {
                try {
                    out?.close()
                } catch (e: IOException) {
                    // Do nothing
                }

            }

            return 0
        }
    }
}
