package org.pragyan.dalal18.fragment.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_faq.*
import org.pragyan.dalal18.R

@Suppress("PLUGIN_WARNING")
class FaqFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_faq, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expand_text_view_faq1.text = resources.getText(R.string.faq_1a)
        expand_text_view_faq2.text = resources.getText(R.string.faq_2a)
        expand_text_view_faq3.text = resources.getText(R.string.faq_3a)
        expand_text_view_faq4.text = resources.getText(R.string.faq_4a)
        expand_text_view_faq5.text = resources.getText(R.string.faq_5a)
        expand_text_view_faq6.text = resources.getText(R.string.faq_6a)
        expand_text_view_faq7.text = resources.getText(R.string.faq_7a)
        expand_text_view_faq8.text = resources.getText(R.string.faq_8a)
        expand_text_view_faq9.text = resources.getText(R.string.faq_9a)
        expand_text_view_faq11.text = resources.getText(R.string.faq_11a)
        expand_text_view_faq12.text = resources.getText(R.string.faq_12a)


        val rewardClaimedSpan = ImageSpan(context!!, R.drawable.blue_thumb)
        val challengeLostSpan = ImageSpan(context!!, R.drawable.clear_icon)
        val spannableText = SpannableString(resources.getString(R.string.faq_10a));

        spannableText.setSpan(rewardClaimedSpan, spannableText.indexOf("symbols") + 9, spannableText.indexOf("symbols") + 11, 0)
        spannableText.setSpan(challengeLostSpan, spannableText.indexOf("claimed.") + 10, spannableText.indexOf("claimed.") + 12, 0)
        expand_text_view_faq10.text = spannableText

        setClickableSpanForForum()
    }

    private fun setClickableSpanForForum() {
        val text = "Reach out to us on Dalal Street Forum"
        val ss = SpannableString(text)
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openForumWebPage()
            }
        }
        ss.setSpan(clickableSpan1, 19, 37, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        forumTextView.text = ss
        forumTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openForumWebPage() {
        val forumIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/jrfEXT5M"))
        startActivity(forumIntent)
    }
}
