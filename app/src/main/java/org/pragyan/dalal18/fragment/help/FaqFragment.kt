package org.pragyan.dalal18.fragment.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_faq.*
import org.pragyan.dalal18.R
import android.content.Intent
import android.net.Uri

@Suppress("PLUGIN_WARNING")
class FaqFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_faq,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expand_text_view_faq1.text = resources.getText(R.string.faq_1a)
        expand_text_view_faq2.text = resources.getText(R.string.faq_2a)
        expand_text_view_faq3.text = resources.getText(R.string.faq_8a)
        expand_text_view_faq4.text = resources.getText(R.string.faq_4a)
        expand_text_view_faq5.text = resources.getText(R.string.faq_5a)
        expand_text_view_faq6.text = resources.getText(R.string.faq_6a)
        expand_text_view_faq8.text = resources.getText(R.string.faq_7a)
        expand_text_view_faq9.text = resources.getText(R.string.faq_9a)
        expand_text_view_faq10.text = resources.getText(R.string.faq_10a)
        expand_text_view_faq7.text = resources.getText(R.string.faq_11a)

        forumTextView.setOnClickListener { openForumWebPage() }
    }

    private fun openForumWebPage() {
        val forumIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forum.pragyan.org/"))
        startActivity(forumIntent)
    }
}