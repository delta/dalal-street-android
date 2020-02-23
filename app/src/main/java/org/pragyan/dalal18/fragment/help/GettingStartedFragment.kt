package org.pragyan.dalal18.fragment.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_getting_started.*
import org.pragyan.dalal18.R

@Suppress("PLUGIN_WARNING")
class GettingStartedFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_getting_started,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expand_text_view1.text = resources.getText(R.string.getting_started_1a)
        expand_text_view2.text = resources.getText(R.string.getting_started_2a)
        expand_text_view3.text = resources.getText(R.string.getting_started_3a)
        expand_text_view4.text  = resources.getText(R.string.getting_started_5a)
        expand_text_view5.text = resources.getText(R.string.getting_started_4a)
    }

}
