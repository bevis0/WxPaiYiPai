package com.bevis.pai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicInteger

object GlobalModel {
    const val STEP_0_INIT = 0
    const val STEP_1_LAUNCH_WX = 1
    const val STEP_2_GO_SETTING = 2
    const val STEP_3_GO_FIX_NAME_SETTING = 3
    const val STEP_4_FIX_NAME_SETTING = 4
    const val STEP_5_BACK = 5
    const val STEP_6_END = 6

    var hasStart : Boolean = false
    var fixName: String = String(byteArrayOf(65198.toByte()), Charset.defaultCharset()) + "的大屁股" + String(byteArrayOf(65198.toByte()), Charset.defaultCharset()) + "大爷"
    val stepCounter = AtomicInteger()

    fun reset() {
        hasStart = false
        fixName = ""
        stepCounter.set(STEP_0_INIT)
    }

    fun start(context: Context?, name: String? = null, suffix: String? = null) {
        reset()
        if(name != null && suffix != null) {
            fixName = buildFixName(name, suffix)
            copyToClipboard(context, fixName)
        }
        hasStart = true
    }

    private fun copyToClipboard(context: Context?, result: String) {
        if(context == null) return
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = ClipData.newPlainText("WXPaiYiPai", result);
        cm.setPrimaryClip(data);
    }

    private fun buildFixName(name: String, suffix: String): String {
        val builder = StringBuilder()
        builder.append(65198.toChar())
            .append(chinaToUnicode(suffix))
            .append(65198.toChar())
            .append(chinaToUnicode(name))
        return decodeUnicode(builder.toString())
    }

    private fun chinaToUnicode(str: String): String? {
        var result = ""
        for (i in str.indices) {
            val chr1: Int = str[i].toInt()
            if (chr1 in 19968..171941) { // 汉字范围 \u4e00-\u9fa5 (中文)
                result += "\\u" + Integer.toHexString(chr1)
            } else {
                result += str[i]
            }
        }
        return result
    }

    private fun isChinese(c: Char): Boolean {
        val ub = Character.UnicodeBlock.of(c)
        return ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub === Character.UnicodeBlock.GENERAL_PUNCTUATION || ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
    }

    private fun decodeUnicode(unicode: String): String {
        val string = StringBuffer()
        val hex = unicode.split("\\\\u".toRegex()).toTypedArray()
        for (i in hex.indices) {
            try {
                // 汉字范围 \u4e00-\u9fa5 (中文)
                if (hex[i].length >= 4) { //取前四个，判断是否是汉字
                    val chinese = hex[i].substring(0, 4)
                    try {
                        val chr = chinese.toInt(16)
                        val isChinese: Boolean = isChinese(chr.toChar())
                        //转化成功，判断是否在  汉字范围内
                        if (isChinese) { //在汉字范围内
                            // 追加成string
                            string.append(chr.toChar())
                            //并且追加  后面的字符
                            val behindString = hex[i].substring(4)
                            string.append(behindString)
                        } else {
                            string.append(hex[i])
                        }
                    } catch (e1: NumberFormatException) {
                        string.append(hex[i])
                    }
                } else {
                    string.append(hex[i])
                }
            } catch (e: NumberFormatException) {
                string.append(hex[i])
            }
        }
        return string.toString()
    }

}