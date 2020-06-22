package com.bevis.pai

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.go_btn).setOnClickListener {
            val mmIntent = context?.packageManager?.getLaunchIntentForPackage("com.tencent.mm")
            if(mmIntent != null) {
                mmIntent.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context?.applicationContext?.startActivity(mmIntent)

                val nameEt = view.findViewById<EditText>(R.id.name_et)
                val name = if(nameEt.text.toString().isEmpty()) "群主" else nameEt.text.toString()

                val suffixEt = view.findViewById<EditText>(R.id.suffix_et)
                val suffix = if(suffixEt.text.toString().isEmpty()) "的后脑勺" else suffixEt.text.toString()

                GlobalModel.start(context, name, suffix)
                if(!isAccessibilitySettingsOn()) {
                    Toast.makeText(context, "已将结果添加到粘贴板中", Toast.LENGTH_LONG).show()
                }
            }

        }

        view.findViewById<RadioButton>(R.id.go_accessibility_btn)?.setOnClickListener {view ->
            if(isAccessibilitySettingsOn()) {
                startAccessibilitySettings()
                Toast.makeText(context,  "在无障碍中找到\"拍一拍小尾巴\"选项，关闭该功能", Toast.LENGTH_LONG).show()
            } else {
                startAccessibilitySettings()
                Toast.makeText(context,  "在无障碍中找到\"拍一拍小尾巴\"选项，开启该功能后返回本软件点击生成结果", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<RadioButton>(R.id.go_accessibility_btn)?.run {
            this.isChecked = isAccessibilitySettingsOn()
        }
    }




    private fun isAccessibilitySettingsOn(): Boolean {
        val application = context?.applicationContext
        if(application != null) {
            var accessibilityEnabled = 0
            val service =
                application.packageName + "/" + PaiAccessService::class.java!!.canonicalName
            try {
                accessibilityEnabled = Settings.Secure.getInt(
                    application.contentResolver,
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED
                )
            } catch (e: Settings.SettingNotFoundException) {
                // do nothing
            }

            val stringColonSplitter = TextUtils.SimpleStringSplitter(':')

            if (accessibilityEnabled == 1) {
                val settingValue = Settings.Secure.getString(
                    application.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                if (settingValue != null) {
                    stringColonSplitter.setString(settingValue)
                    while (stringColonSplitter.hasNext()) {
                        val accessibilityService = stringColonSplitter.next()
                        if (accessibilityService.equals(service, ignoreCase = true)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun startAccessibilitySettings() {
        if(context != null) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            context?.applicationContext?.startActivity(intent)
        }
    }
}