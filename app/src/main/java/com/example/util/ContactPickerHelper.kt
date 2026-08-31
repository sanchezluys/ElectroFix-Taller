package com.example.util

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

data class ContactInfo(
    val name: String,
    val phone: String,
    val email: String = ""
)

object ContactPickerHelper {

    fun createContactPickerIntent(): Intent {
        return Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
    }

    fun createSaveContactIntent(name: String, phone: String, email: String = ""): Intent {
        return Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            if (name.isNotBlank()) putExtra(ContactsContract.Intents.Insert.NAME, name)
            if (phone.isNotBlank()) putExtra(ContactsContract.Intents.Insert.PHONE, phone)
            if (email.isNotBlank()) putExtra(ContactsContract.Intents.Insert.EMAIL, email)
        }
    }

    fun extractContactInfo(context: Context, contactUri: Uri?): ContactInfo? {
        if (contactUri == null) return null
        return try {
            var contactName = ""
            var contactPhone = ""
            var contactId: String? = null

            val cursor: Cursor? = context.contentResolver.query(
                contactUri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    if (idIndex >= 0) contactId = it.getString(idIndex)
                    contactName = if (nameIndex >= 0) it.getString(nameIndex).orEmpty() else ""
                    contactPhone = if (numberIndex >= 0) it.getString(numberIndex).orEmpty() else ""
                }
            }

            var contactEmail = ""
            if (!contactId.isNullOrBlank()) {
                val emailCursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                    "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )
                emailCursor?.use {
                    if (it.moveToFirst()) {
                        val emailIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                        if (emailIndex >= 0) {
                            contactEmail = it.getString(emailIndex).orEmpty()
                        }
                    }
                }
            }

            if (contactName.isNotBlank() || contactPhone.isNotBlank()) {
                ContactInfo(name = contactName, phone = contactPhone, email = contactEmail)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

