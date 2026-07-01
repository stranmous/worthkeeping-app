package com.worthkeeping.app.scanner

import android.Manifest
import android.os.Build

object MediaPermissionHelper {
    fun requiredImagePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    fun hasFullImageAccess(grantedPermissions: Map<String, Boolean>): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                grantedPermissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            }
            else -> {
                grantedPermissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            }
        }
    }

    fun hasLimitedImageAccess(grantedPermissions: Map<String, Boolean>): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            grantedPermissions[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true &&
            grantedPermissions[Manifest.permission.READ_MEDIA_IMAGES] != true
    }
}
