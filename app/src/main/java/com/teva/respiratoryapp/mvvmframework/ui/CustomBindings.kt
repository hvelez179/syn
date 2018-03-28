//
// CustomBindings.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.databinding.InverseBindingListener
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.AsyncTask
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.LeadingMarginSpan
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.ERROR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.controls.DateValidator
import com.teva.respiratoryapp.activity.controls.InputField
import com.teva.respiratoryapp.activity.controls.InputValidator
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Custom binding adapters used by data bindings.
 */
object CustomBindings {
    private val logger = Logger(CustomBindings::class)

    /**
     * Listener used for hyperlink click bindings.
     */
    interface OnLinkClickedListener {
        /**
         * Called when a link is clicked.
         *
         * @param id The id of the link.
         */
        fun onLinkCLicked(id: Int)
    }

    /**
     * Represents link classes used by hyperlink bindings.
     *
     * @property id The id of the link.
     * @property start The start position of the link.
     * @property end The end position of the link.
     */
    private data class Link(var id: Int = 0, var start: Int = 0, var end: Int = 0)

    /**
     * Binding adapter used to setup date validation bindings.
     *
     * @param textView The view to bind to
     * @param date The local date to be set into the TextView.
     * @param dateFormat The format enforced for the entered dates.
     * @param minYear The minimum valid year.
     * @param maxYear The maximum valid year.
     * @param stateListener The validation state changed listener.
     * @param dateListener The date changed listener.
     */
    @BindingAdapter("date", "dateFormat", "minYear", "maxYear", "onStateChanged", "dateAttrChanged", requireAll = false)
    @JvmStatic
    fun setDate(textView: TextView,
                date: LocalDate?,
                dateFormat: DateValidator.DateFormat?,
                minYear: Int?,
                maxYear: Int?,
                stateListener: InputValidator.ValidationListener?,
                dateListener: InverseBindingListener?) {
        var validator = textView.getTag(R.id.date_validator_tag) as? DateValidator
        if (validator == null) {
            validator = DateValidator()
            textView.setTag(R.id.date_validator_tag, validator)
            textView.addTextChangedListener(validator)
            textView.keyListener = validator
        }

        validator.format = dateFormat ?: DateValidator.DateFormat.MONTH_DAY_YEAR
        if (minYear != null) {
            validator.minYear = minYear
        }

        if (maxYear != null) {
            validator.maxYear = maxYear
        }

        validator.dateListener = dateListener
        validator.validationListener = object : InputValidator.ValidationListener {
            override fun onValidationChanged(validationState: InputValidator.ValidationState) {
                stateListener?.onValidationChanged(validationState)

                if (textView is InputField) {
                    textView.isInError = validationState == InputValidator.ValidationState.IN_ERROR
                }
            }
        }

        val newText = validator.textFromDate(date)
        if (newText != null && (textView.text.toString() != newText)) {
            textView.text = newText
        }

        textView.hint = textView.context.getString(validator.hint)

        if (textView is InputField) {
            textView.showPatternHint = true
        }
    }

    /**
     * Inverse data binding of dates for TextView controls.
     *
     * @param textView The TextView to retrieve the date for.
     */
    @InverseBindingAdapter(attribute = "date")
    @JvmStatic
    fun getDate(textView: TextView): LocalDate? {
        val validator = textView.getTag(R.id.date_validator_tag) as? DateValidator
        return validator?.date
    }

    /**
     * Custom binding that finds links marked with "#" characters and binds them to the OnLinkClickedLister.
     *
     * @param textView The view to create the binding on.
     * @param text The text to assign to the TextView.
     * @param linkClicked The link click listener.
     */
    @BindingAdapter("hyperlink", "linkClicked", requireAll = false)
    @JvmStatic
    fun setHyperlinks(textView: TextView, text: String?, linkClicked: OnLinkClickedListener) {
        if (text != null) {
            var str: String = text
            val links = ArrayList<Link>()

            if (str.contains('#')) {
                var index = 0
                do {
                    index = str.indexOf("#", index)
                    if (index != -1) {
                        val endIndex = str.indexOf("#", index + 2)

                        val link = Link(
                                Integer.parseInt(str.substring(index + 1, index + 2)),
                                index,
                                endIndex - 2)
                        str = str.substring(0, index) + str.substring(index + 2, endIndex) + str.substring(endIndex + 1)

                        links.add(link)
                    }
                } while (index != -1)
            } else {
                links.add(Link(0, 0, str.length))
            }

            val spannableText = SpannableString(str)
            for (link in links) {
                val id = link.id
                spannableText.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        linkClicked.onLinkCLicked(id)
                    }
                }, link.start, link.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            textView.setText(spannableText, TextView.BufferType.SPANNABLE)

            // add touch listener for the links
            textView.setOnTouchListener(View.OnTouchListener { view, event ->
                val action = event.action

                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                    val textViewWidget = view as TextView
                    val spannable = textViewWidget.text as Spannable
                    val x = event.x.toInt() + textViewWidget.scrollX - textViewWidget.totalPaddingLeft
                    val y = event.y.toInt() + textViewWidget.scrollY - textViewWidget.totalPaddingTop

                    val layout = textViewWidget.layout
                    val line = layout.getLineForVertical(y)
                    val off = layout.getOffsetForHorizontal(line, x.toFloat())

                    val link = spannable.getSpans(off, off, ClickableSpan::class.java)

                    if (link.isNotEmpty()) {
                        if (action == MotionEvent.ACTION_UP) {
                            link[0].onClick(textViewWidget)
                            textViewWidget.playSoundEffect(SoundEffectConstants.CLICK)
                        }

                        return@OnTouchListener true
                    }
                }

                false
            })
        }
    }

    /**
     * Custom binding setting the hanging indent of a TextView
     *
     * @param textView The TextView to modify.
     * @param text The text to assign to the TextView.
     * @param indent The hanging indent in pixels.
     */
    @BindingAdapter("android:text", "hangingIndent")
    @JvmStatic
    fun setHangingIndent(textView: TextView, text: String, indent: Float) {
        val firstIndent = LeadingMarginSpan.Standard(0, indent.toInt())
        val secondIndent = LeadingMarginSpan.Standard(indent.toInt())

        val styledSource = SpannableString(text)

        val index = text.indexOf('\n', 0)
        if (index != -1) {
            styledSource.setSpan(firstIndent, 0, index,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            styledSource.setSpan(secondIndent, index, styledSource.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        } else {
            styledSource.setSpan(firstIndent, 0, styledSource.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        textView.setText(styledSource, TextView.BufferType.SPANNABLE)
    }

    /**
     * Custom binding for the "blurSrc" attribute of ImageViews.
     *
     * @param imageView The ImageView to modify.
     * @param resourceId The drawable resource.
     */
    @BindingAdapter("android:blurSrc")
    @JvmStatic
    fun setImageResource(imageView: ImageView, resourceId: Int) {
        imageView.setImageResource(resourceId)
    }

    /**
     * Custom binding for the "blurSrc" attribute of ImageViews.
     *
     * @param imageView The ImageView to modify.
     * @param resourceId The drawable resource.
     */
    @BindingAdapter("android:src")
    @JvmStatic
    fun setImageResourceInt(imageView: ImageView, resourceId: Int) {
        imageView.setImageResource(resourceId)
    }

    /**
     * Binding adapter for the "background" property of views.
     *
     * @param view The View to modify.
     * @param resourceId The resource id to convert.
     */
    @BindingAdapter("android:background")
    @JvmStatic
    fun setBackgroundResource(view: View, resourceId: Int) {
        view.setBackgroundResource(resourceId)
    }

    /**
     * Binding adapter for the text color of a TextView.
     *
     * @param view The View to modify.
     * @param colorId The color id to convert.
     */
    @BindingAdapter("textColorId")
    @JvmStatic
    fun setTextColorById(view: TextView, colorId: Int) {
        @Suppress("DEPRECATION") // required to support API 21
        val color = view.context.resources.getColor(colorId)
        view.setTextColor(color)
    }

    /**
     * Custom binding to convert a boolean value to a visibility.
     *
     * @param view The View to modify.
     * @param isVisible The boolean value to convert.
     */
    @BindingAdapter("android:visibility")
    @JvmStatic
    fun setVisibility(view: View, isVisible: Boolean) {
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Custom binding to enable or disable whether touch events propagate through a view.
     *
     * @param view The view to modify.
     * @param isTouchOpaque The boolean value to convert.
     */
    @BindingAdapter("touchOpaque")
    @JvmStatic
    fun setTouchOpaque(view: View, isTouchOpaque: Boolean) {
        if (isTouchOpaque) {
            view.setOnTouchListener { _, _ -> true }
        } else {
            view.setOnTouchListener(null)
        }
    }

    /**
     * Custom binding to set the layout_gravity of a panel.
     *
     * @param view The view to modify.
     * @param gravity The layout gravity value to convert.
     */
    @BindingAdapter("android:layout_gravity")
    @JvmStatic
    fun setLayoutGravity(view: View, gravity: Int) {
        val params = view.layoutParams
        if (params is FrameLayout.LayoutParams) {
            params.gravity = gravity
        } else {
            logger.log(ERROR, "setLayoutGravity: unsupported panel - " + view.javaClass.simpleName)
        }
    }

    /**
     * Custom binding to set the layout_marginEnd property.
     *
     * @param view The view to modify.
     * @param dimensionId The dimension id to convert.
     */
    @BindingAdapter("android:layout_marginEnd")
    @JvmStatic
    fun setMarginEnd(view: View, dimensionId: Int) {
        val params = view.layoutParams
        if (params is ViewGroup.MarginLayoutParams) {
            params.marginEnd = view.resources.getDimensionPixelOffset(dimensionId)
        }
    }

    /**
     * Custom binding to load an asset image into an ImageView resource.
     *
     * @param view The view to modify.
     * @param asset The name of the asset.
     */
    @BindingAdapter("asset")
    @JvmStatic
    fun bindAsset(view: ImageView, asset: String) {

        view.setImageBitmap(null)
        val assetManager = view.context.assets
        val task = object : AsyncTask<String, Void, Bitmap>() {
            override fun doInBackground(vararg params: String): Bitmap? {
                var bitmap: Bitmap? = null

                try {
                    val istr = assetManager.open(params[0])
                    bitmap = BitmapFactory.decodeStream(istr)
                } catch (ex: Exception) {
                    logger.logException(ERROR, "Exception while loading asset", ex)
                }

                return bitmap
            }

            override fun onPostExecute(bitmap: Bitmap?) {
                view.setImageBitmap(bitmap)
            }
        }

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, asset)
    }

    /**
     * Custom binding to set HTML text into a text view.
     * @param view The View to modify.
     * *
     * @param htmlResource The html text resource.
     */
    @BindingAdapter("html")
    @JvmStatic
    fun setHtml(view: TextView, htmlResource: Int) {
        val html = view.context.getString(htmlResource)
        view.setText(Html.fromHtml(html), TextView.BufferType.SPANNABLE)
    }


    /**
     * Custom binding to enable underlined text for a text view.
     * @param view The view to modify.
     * *
     * @param isUnderlined The boolean value to convert.
     */
    @BindingAdapter("underlined")
    @JvmStatic
    fun setUnderlined(view: TextView, isUnderlined: Boolean) {
        if (isUnderlined) {
            view.paintFlags = view.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        } else {
            view.paintFlags = view.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        }
    }
}
