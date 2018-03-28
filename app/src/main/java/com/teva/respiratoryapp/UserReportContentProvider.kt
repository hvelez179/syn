//
// UserReportContentProvider.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns

import java.io.File
import java.io.FileNotFoundException

class UserReportContentProvider : ContentProvider() {

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Operation not supported.")
    }

    override fun getType(uri: Uri): String? {
        return USER_REPORT_MIME_TYPE
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Operation not supported.")
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        if (uri.toString() != USER_REPORT_URI || projection == null) {
            return null
        }

        val cursor = MatrixCursor(projection, 1)
        val b = cursor.newRow()

        for (col in projection) {
            if (OpenableColumns.DISPLAY_NAME == col) {
                b.add(USER_REPORT_FILE_NAME)
            } else if (OpenableColumns.SIZE == col) {
                val reportFile = File(context!!.filesDir.absolutePath, USER_REPORT_FILE_NAME)
                if (reportFile.exists()) {
                    b.add(reportFile.length())
                } else {
                    b.add(AssetFileDescriptor.UNKNOWN_LENGTH)
                }
            } else if (col == "_data") {
                b.add(USER_REPORT_FILE_NAME)
            } else {
                b.add(null)
            }
        }

        return cursor
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Operation not supported.")
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        if (uri.toString() == USER_REPORT_URI) {
            val reportFile = File(context!!.filesDir.absolutePath, USER_REPORT_FILE_NAME)
            parcelFileDescriptor = ParcelFileDescriptor.open(reportFile, ParcelFileDescriptor.MODE_READ_ONLY)
        }
        return parcelFileDescriptor
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri,
                          mode: String,
                          signal: CancellationSignal?): ParcelFileDescriptor? {
        return super.openFile(uri, mode, signal)
    }

    companion object {
        val USER_REPORT_MIME_TYPE = "application/pdf"
        val USER_REPORT_URI = "content://com.teva.respiratoryapp/userreport"
        val USER_REPORT_FILE_NAME = "UserReport.pdf"
    }
}
