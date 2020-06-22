package com.bevis.pai

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import android.webkit.WebView
import android.widget.EditText


fun AccessibilityNodeInfo.findClickableWidget(): AccessibilityNodeInfo? {
    var clickWidget = this
    while (!clickWidget.isClickable && clickWidget.parent != null) {
        clickWidget = clickWidget.parent
    }
    return if (clickWidget.isClickable) clickWidget else null
}

fun AccessibilityNodeInfo.findWidgetByDesc(desc: String): List<AccessibilityNodeInfo> {
    val list = mutableListOf<AccessibilityNodeInfo>()
    val curNode = this
    if(desc == curNode.contentDescription) {
        list.add(curNode)
    }
    if(curNode.childCount > 0) {
        for(i in 0 until curNode.childCount) {
            curNode.getChild(i)?.findWidgetByDesc(desc)?.let {
                list.addAll(it)
            }
        }
    }
    return list
}


fun AccessibilityNodeInfo.findWidgetByClass(type: Class<*>): List<AccessibilityNodeInfo> {
    val list = mutableListOf<AccessibilityNodeInfo>()
    val curNode = this
    if(type.name == curNode.className) {
        list.add(curNode)
    }
    if(curNode.childCount > 0) {
        for(i in 0 until curNode.childCount) {
            curNode.getChild(i)?.findWidgetByClass(type)?.let {
                list.addAll(it)
            }
        }
    }
    return list
}

fun AccessibilityNodeInfo.findWidgetByContainDesc(desc: String): List<AccessibilityNodeInfo> {
    val list = mutableListOf<AccessibilityNodeInfo>()
    val curNode = this
    if(curNode.contentDescription?.contains(desc) == true) {
        list.add(curNode)
    }
    if(curNode.childCount > 0) {
        for(i in 0 until curNode.childCount) {
            curNode.getChild(i)?.findWidgetByContainDesc(desc)?.let {
                list.addAll(it)
            }
        }
    }
    return list
}

fun AccessibilityNodeInfo.clickWidgetByDesc(desc: String) {
    findWidgetByDesc( desc).let {
        if(it.isEmpty()) {
//            throw IllegalStateException("未找到控件 $desc")
        } else {
            it[0].findClickableWidget()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)?:throw java.lang.IllegalStateException("未找到可点击控件 $desc")
        }
    }
}

fun AccessibilityNodeInfo.clickWidgetByContainDesc(desc: String) {

    findWidgetByContainDesc(desc).let {
        if (!it.isEmpty()) {
            it[0].findClickableWidget()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                ?: throw java.lang.IllegalStateException("未找到可点击控件 $desc")
        }
    }
}

fun AccessibilityNodeInfo.clickWidgetById(id: String): Boolean {
    findAccessibilityNodeInfosByViewId(id).let {
        if(!it.isEmpty()) {
            val view = it[0].findClickableWidget() ?: return@let
            if (view.isEnabled) {
                view.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }
    }

    return false
}


fun AccessibilityNodeInfo.setTextWidgetById(
    id: String,
    setText: String,
    type: Class<*>? = EditText::class.java,
    errorCb: ((AccessibilityNodeInfo, Exception) -> Unit)? = null)  {

    val arguments = Bundle()
    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, setText)
    findAccessibilityNodeInfosByViewId(id)?.let { findNodes ->
        if (findNodes.isEmpty()) {
            errorCb?.invoke(this, IllegalStateException("not find widget, id is \"$id\""))
            return@let
        }
        else {
            findNodes.let { findNodes ->
                findNodes.forEach { findNode ->
                    if (type != null) {
                        if (findNode.className == type.name) {
                            return@let findNode
                        }
                    } else {
                        return@let findNode
                    }
                }

                return@let null
            }?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)?:errorCb?.invoke(this, IllegalStateException("not find widget, id is \"$id\""))
        }
    }

}

fun AccessibilityNodeInfo.performClick() {
    if(isClickable) {
        performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}

fun AccessibilityNodeInfo.clickWidgetByText(
    text: String,
    type: Class<*>? = null,
    errorCb: ((Exception) -> Unit)? = null
)  {

    findAccessibilityNodeInfosByText(text)?.let { findNodes ->
        if (findNodes.isEmpty())
            errorCb?.invoke(IllegalStateException("not find widget, text is \"$text\""))
        else
            findNodes.let { findNodes ->
                findNodes.forEach { findNode ->
                    if (type != null) {
                        if (findNode.className == type.name) {
                            return@let findNode
                        }
                    } else {
                        return@let findNode
                    }
                }
                return@let findNodes[0]
            }.findClickableWidget()?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: errorCb?.invoke(
                IllegalStateException("not find widget, text is \"$text\"")
            )

    } ?: errorCb?.invoke(java.lang.IllegalStateException("not find widget, text is \"$text\""))
}

fun AccessibilityNodeInfo.clickWebViewNodeByText(text: String) {
    val nodes = findWebViewNodeByText(text)
    if(nodes.isNotEmpty()) {
        nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}


fun AccessibilityNodeInfo.findWebViewNodeByText(text: String, isContain: Boolean = false):List<AccessibilityNodeInfo> {
    val list = mutableListOf<AccessibilityNodeInfo>()
    val webviews = findWidgetByClass(WebView::class.java)
    webviews.forEach {webview ->
        list.addAll(webview.findWidgetByText(text, isContain))
    }
    return list
}

private fun AccessibilityNodeInfo.findWidgetByText(text: String, isContain: Boolean = false): List<AccessibilityNodeInfo> {
    val list = mutableListOf<AccessibilityNodeInfo>()
    val curNode = this
    if(isContain) {
        if(curNode.text.contains(text)){
            list.add(curNode)
        }
    } else if(text == curNode.text) {
        list.add(curNode)
    }
    if(curNode.childCount > 0) {
        for(i in 0 until curNode.childCount) {
            curNode.getChild(i)?.findWidgetByText(text, isContain)?.let {
                list.addAll(it)
            }
        }
    }
    return list
}


//
//private void topGestureClick() {
//
//        GestureDescription.Builder builder = new GestureDescription.Builder();
//
//        Path path = new Path();
//
//        int y = 1200;
//
//        int x = 360;
//
//        path.moveTo(360, y);
//
//     
//
//        path.lineTo(x += 3, y -= 1000);
//
//     
//
//        GestureDescription gestureDescription = builder
//
//                .addStroke(new GestureDescription.StrokeDescription(path, 200L, 800L,false))
//
//                .build();
//
//        dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
//
//                @Override
//
//                public void onCompleted(GestureDescription gestureDescription) {
//
//                    super.onCompleted(gestureDescription);
//
//                    Log.e(TAG, "123===onCompleted" );
//
//                    handler.postDelayed(new Runnable() {
//
//                        @Override
//
//                        public void run() {
//
//                            leftGestureClick();
//
//                        }
//
//                    },3000);
//
//                }
//
//         
//
//                @Override
//
//                public void onCancelled(GestureDescription gestureDescription) {
//
//                    Log.e(TAG, "123===onCancelled" );
//
//         
//
//                }
//
//            }, new Handler(Looper.getMainLooper()));
//
//}
