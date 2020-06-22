package com.bevis.pai

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class PaiAccessService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        val mmIntent = packageManager?.getLaunchIntentForPackage("com.bevis.pai")
        if(mmIntent != null) {
            mmIntent.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            applicationContext?.startActivity(mmIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent?.getBooleanExtra("close", false) == true) {
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onInterrupt() {
        GlobalModel.reset()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = if(event != null) event.packageName else ""
        if("com.tencent.mm" == packageName) {
            val eventType = event?.eventType
            val clsName = event?.className
            val rootWindow = rootInActiveWindow
            when {
                GlobalModel.hasStart
                        && AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType
                        && "com.tencent.mm.ui.LauncherUI" == clsName
                        && rootWindow.findAccessibilityNodeInfosByText("通讯录").isNotEmpty()
                        && rootWindow.findAccessibilityNodeInfosByText("发现").isNotEmpty()
                        && rootWindow.findAccessibilityNodeInfosByText("我").isNotEmpty()
                        && GlobalModel.stepCounter.compareAndSet(GlobalModel.STEP_0_INIT, GlobalModel.STEP_1_LAUNCH_WX)-> {
                    Toast.makeText(this, "请点击进入任意一个需要生效的微信群", Toast.LENGTH_LONG).show()
                }
                GlobalModel.hasStart &&
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType &&
                        "android.widget.LinearLayout" == clsName &&
                        rootWindow.findWidgetByDesc("聊天信息").isNotEmpty() &&
                        GlobalModel.stepCounter.compareAndSet(GlobalModel.STEP_1_LAUNCH_WX, GlobalModel.STEP_2_GO_SETTING)-> {
                    rootWindow.clickWidgetByContainDesc("聊天信息")
                }
                GlobalModel.hasStart &&
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType &&
                        "com.tencent.mm.chatroom.ui.ChatroomInfoUI" == clsName &&
                        rootWindow.findAccessibilityNodeInfosByViewId("android:id/list").isNotEmpty() &&
                        GlobalModel.stepCounter.compareAndSet(GlobalModel.STEP_2_GO_SETTING, GlobalModel.STEP_3_GO_FIX_NAME_SETTING)-> {
                    val targetWidgets = rootWindow.findAccessibilityNodeInfosByText("我在群里的昵称")
                    if(targetWidgets.isEmpty()) {
                        val contentList = rootWindow.findAccessibilityNodeInfosByViewId("android:id/list")[0]
                        var tryCounter = 0
                        val maxTryCounter = 500
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            var position =  0
                            while (tryCounter ++ < maxTryCounter && rootWindow.findAccessibilityNodeInfosByText("我在群里的昵称").isEmpty()) {

                                position += 10
                                contentList.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.id,
                                    Bundle().apply {
                                        putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, position)
                                    })
                            }
                        } else {
                            while (tryCounter ++ < maxTryCounter &&  rootWindow.findAccessibilityNodeInfosByText("我在群里的昵称").isEmpty()) {
                                performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                            }
                        }

                    }

                    if(rootWindow.findAccessibilityNodeInfosByText("我在群里的昵称").isNotEmpty()) {
                        rootWindow.clickWidgetByText("我在群里的昵称")
                    }
//                    Toast.makeText(this, "请点击进入任意一个需要生效的微信群", Toast.LENGTH_LONG).show()
                }
                GlobalModel.hasStart &&
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType &&
                        "com.tencent.mm.chatroom.ui.ModRemarkRoomNameUI" == clsName &&
                        rootWindow.findAccessibilityNodeInfosByText("我在群里的昵称").isNotEmpty() &&
                        GlobalModel.stepCounter.compareAndSet(GlobalModel.STEP_3_GO_FIX_NAME_SETTING, GlobalModel.STEP_4_FIX_NAME_SETTING)-> {


                    val offsetWidget = rootWindow.findAccessibilityNodeInfosByText("我在群里的昵称")[0]

                    var childCount = offsetWidget.parent.childCount
                    var targetInputNode: AccessibilityNodeInfo? = null
                    var targetCompleteNode: AccessibilityNodeInfo? = null
                    for(index in 0 until childCount) {
                        val childWidget = offsetWidget.parent.getChild(index)
                        if(targetInputNode == null && childWidget.className == ViewGroup::class.java.name) {
                            childCount = childWidget.getChildCount()
                            for(index in 0 until childCount) {
                                val childWidget = offsetWidget.parent.getChild(index)
                                if(childWidget.className == EditText::class.java.name) {
                                    targetInputNode = childWidget
                                    break
                                }
                            }
                        } else if(targetInputNode == null && childWidget.className == EditText::class.java.name) {
                            targetInputNode = childWidget
                        } else if(targetCompleteNode == null && childWidget.className == Button::class.java.name) {
                            targetCompleteNode = childWidget
                        }
                        if(targetInputNode != null && targetCompleteNode != null) {
                            break
                        }
                    }

                    if(targetInputNode != null && targetCompleteNode != null) {
                        targetInputNode.performAction(
                            AccessibilityNodeInfo.ACTION_SET_TEXT,
                            Bundle().apply {
                                putString(
                                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                    GlobalModel.fixName
                                )
                            })
                        targetCompleteNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }

                GlobalModel.hasStart &&
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType &&
                        "com.tencent.mm.chatroom.ui.ChatroomInfoUI" == clsName &&
                        GlobalModel.stepCounter.compareAndSet(GlobalModel.STEP_4_FIX_NAME_SETTING, GlobalModel.STEP_5_BACK)-> {
                    rootWindow.clickWidgetByContainDesc("返回")


                }

                GlobalModel.hasStart &&
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == eventType &&
                        "com.tencent.mm.ui.LauncherUI" == clsName &&
                        rootWindow.findWidgetByDesc("切换到按住说话").isNotEmpty() &&
                        GlobalModel.stepCounter.compareAndSet(GlobalModel.STEP_5_BACK, GlobalModel.STEP_6_END)-> {
                    Toast.makeText(this, "已完成设置，现在可以试一试拍一拍了", Toast.LENGTH_LONG).show()
                    GlobalModel.reset()
                    stopSelf()
                }



            }
            Log.i("bevisLog", "event : ${event}")
        } else {
            GlobalModel.reset()
        }
    }

}