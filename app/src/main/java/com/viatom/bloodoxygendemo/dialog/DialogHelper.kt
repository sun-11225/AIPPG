package com.viatom.lib.vihealth.update.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import com.viatom.bloodoxygendemo.R


class DialogHelper<T : View> private constructor(private val contentView: View, private val transparentBK: Boolean = false, private val mViews: SparseArray<T> = SparseArray()): DialogFragment() {
    private var gravity = Gravity.CENTER
    @StyleRes
    private var style: Int = R.style.O2CustomDialogTheme
    private var bottomMargin = 0

    companion object {
        fun <T : View> newInstance(context: Context, @LayoutRes layoutResId: Int, transparentBK: Boolean): DialogHelper<T> {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val contentView = inflater.inflate(layoutResId, null)
            val dialogHelper: DialogHelper<T> = DialogHelper<T>(contentView, transparentBK)
            return dialogHelper
        }

        fun <T : View> newInstance(context: Context, @LayoutRes layoutResId: Int): DialogHelper<T> {
            return newInstance(context, layoutResId, true)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity(), style)
        dialog.setContentView(contentView)
        val window = dialog.window
        if (window != null) {
            window.setGravity(gravity)
            if(gravity == Gravity.BOTTOM) {
                window.setWindowAnimations(R.style.main_menu_animstyle)
            }
            if(transparentBK) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            }
            val params = window.attributes
            params.y = bottomMargin
            window.attributes = params
        }
        return dialog
    }

    override fun onResume() {
        super.onResume()
        val window = dialog!!.window
        if(gravity == Gravity.BOTTOM) {
            window?.setWindowAnimations(R.style.main_menu_animstyle)
        }
        if(transparentBK) {
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    val isShowing: Boolean
        get() = if (dialog != null) {
            dialog!!.isShowing
        } else false

    fun closeDialog() {
        val dialog = dialog
        dialog?.dismiss()
    }

    fun setDialogCancelable(cancelable: Boolean): DialogHelper<*> {
        super.setCancelable(cancelable)
        return this
    }

    fun setGravity(gravity: Int): DialogHelper<*> {
        this.gravity = gravity
        return this
    }

    fun setStyle(@StyleRes style: Int): DialogHelper<*> {
        this.style = style
        return this
    }

    fun setBottomMargin(bottomMargin: Int): DialogHelper<*> {
        this.bottomMargin = bottomMargin
        return this
    }

    fun getView(@IdRes viewId: Int): T {
        var view = mViews[viewId]
        if (view == null) {
            view = contentView.findViewById(viewId)
            mViews.put(viewId, view)
        }
        return view
    }

    fun addListener(@IdRes viewId: Int, listener: View.OnClickListener): DialogHelper<T> {
        var tmpView = getView(viewId)
        tmpView.setOnClickListener(listener)
        return this
    }

    fun setVisible(@IdRes viewId: Int, visibility: Int): DialogHelper<T> {
        val view: View = getView(viewId)
        view.visibility = visibility
        return this
    }

    fun setText(@IdRes viewId: Int, text: CharSequence?): DialogHelper<T> {
        val view = getView(viewId) as TextView?
        view!!.text = text
        return this
    }

    fun setText(@IdRes viewId: Int, @StringRes stringId: Int): DialogHelper<T> {
        val view = getView(viewId) as TextView?
        view!!.setText(stringId)
        return this
    }


}
