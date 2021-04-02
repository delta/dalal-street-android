package org.pragyan.dalal18.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.squareup.picasso.Picasso
import org.pragyan.dalal18.R
import org.pragyan.dalal18.databinding.FragmentNewsDetailsBinding
import org.pragyan.dalal18.utils.Constants
import org.pragyan.dalal18.utils.MiscellaneousUtils.parseDate
import org.pragyan.dalal18.utils.viewLifecycle

class NewsDetailsFragment : Fragment() {

    private var binding by viewLifecycle<FragmentNewsDetailsBinding>()

    private lateinit var loadingDialog: AlertDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNewsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            newsDetailsHead.text = arguments?.getString(Constants.NEWS_HEAD_KEY)
            newsDetailsContent.text = arguments?.getString(Constants.NEWS_CONTENT_KEY)
            newsDetailsCreatedAt.text = parseDate(arguments?.getString(Constants.NEWS_CREATED_AT_KEY))
            Picasso.get().load("https://delta.nitt.edu/~vivekr/dalal/news/" + arguments?.getString(Constants.NEWS_IMAGE_PATH_KEY)).into(newsDetailsImage)
            ViewCompat.setTransitionName(newsDetailsHead, arguments?.getString(Constants.HEAD_TRANSITION_KEY))
            ViewCompat.setTransitionName(newsDetailsContent, arguments?.getString(Constants.CONTENT_TRANSITION_KEY))
            ViewCompat.setTransitionName(newsDetailsCreatedAt, arguments?.getString(Constants.CREATED_AT_TRANSITION_KEY))
        }

        loadingDialog.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(activity).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(activity).inflateTransition(android.R.transition.move)

        val dialogBox = LayoutInflater.from(activity).inflate(R.layout.progress_dialog, null)
        dialogBox.findViewById<TextView>(R.id.progressDialog_textView).setText(R.string.getting_latest_news)
        loadingDialog = AlertDialog.Builder(activity).setView(dialogBox).setCancelable(false).create()
        loadingDialog.show()
    }
}