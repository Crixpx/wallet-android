package com.mycelium.wallet.activity.export

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.mycelium.wallet.R
import com.mycelium.wallet.Utils
import com.mycelium.wapi.wallet.ExportableAccount


class ExportAsQrViewModel(val context: Application) : AndroidViewModel(context) {
    private lateinit var model: ExportAsQrModel
    val accountDataString: MutableLiveData<String> = MutableLiveData()
    val privateDataSelected: MutableLiveData<Boolean> = MutableLiveData()  // whether user switched to private
    val showRedWarning: MutableLiveData<Boolean> = MutableLiveData()       // show warning instead of qr for private
    private var hasWarningAccepted = false
    var isBtcHdAccount = false
        private set

    fun init(accountData: ExportableAccount.Data, isBtcHdAccount: Boolean) {
        if (::model.isInitialized) {
            throw IllegalStateException("This method should be called only once.")
        }
        model = ExportAsQrModel(context, accountData)
        this.isBtcHdAccount = isBtcHdAccount
        accountDataString.value = accountData.publicData.get()

        updateData(false)
    }

    fun hasPrivateData(): Boolean = model.hasPrivateData()

    fun updateData(privateDataSelected: Boolean) {
        this.privateDataSelected.value = privateDataSelected
        if (privateDataSelected) {
            showRedWarning.value = !hasWarningAccepted
            accountDataString.value = model.accountData.privateData.get()
        } else {
            showRedWarning.value = false
            accountDataString.value = model.accountData.publicData.get()
        }
    }

    // must return false to bind with onLongClick in xml
    fun acceptWarning(): Boolean {
        hasWarningAccepted = true
        showRedWarning.value = false
        return false
    }

    /**
     * Updates account data based on extra toggles
     */
    fun getToggledData(toggleNum: Int): String {
        // todo
        var data = "todo Sergey"
        when (toggleNum) {
            1 -> data = when (privateDataSelected.value!!) {
                true -> "todo xpriv"
                false -> "todo xpub"
            }

            2 -> data = when (privateDataSelected.value!!) {
                true -> "todo ypriv"
                false -> "todo ypub"
            }

            3 -> data = when (privateDataSelected.value!!) {
                true -> "todo zpriv"
                false -> "todo zpub"
            }
        }

        return data
    }

    /**
     * Returns private/public key as string with spaces for readability
     */
    fun getReadableData(data: String): String {
        // split the date in fragments with 8chars and a newline after three parts
        val builder = StringBuilder()
        var cnt = 0
        for (part in Utils.stringChopper(data, 8)) {
            cnt++
            builder.append(part)
            builder.append(if (cnt % 3 == 0) "\n" else " ")
        }

        return builder.toString()
    }

    fun exportDataToClipboard(activity: AppCompatActivity) {
        if (privateDataSelected.value!!) {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.export_to_clipboard_warning).setCancelable(false)
                    .setPositiveButton(R.string.yes) { dialog, id ->
                        Utils.setClipboardString(accountDataString.value, context)
                        Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }.setNegativeButton(R.string.no) { dialog, id -> }
            val alertDialog = builder.create()
            alertDialog.show()
        } else {
            Utils.setClipboardString(accountDataString.value, context)
            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }
    }

    fun shareData(activity: AppCompatActivity) {
        if (privateDataSelected.value!!) {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.export_share_warning).setCancelable(false)
                    .setPositiveButton(R.string.yes) { dialog, id ->
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.type = "text/plain"
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.xpriv_title))
                        sendIntent.putExtra(Intent.EXTRA_TEXT, accountDataString.value)
                        context.startActivity(Intent.createChooser(sendIntent, context.resources.getString(R.string.share_xpriv)))
                        dialog.dismiss()
                    }.setNegativeButton(R.string.no) { dialog, id -> }
            val alertDialog = builder.create()
            alertDialog.show()
        } else {
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.xpub_title))
            sendIntent.putExtra(Intent.EXTRA_TEXT, accountDataString.value)
            context.startActivity(Intent.createChooser(sendIntent, context.resources.getString(R.string.share_xpub)))
        }
    }

    fun isInitialized() = ::model.isInitialized
}