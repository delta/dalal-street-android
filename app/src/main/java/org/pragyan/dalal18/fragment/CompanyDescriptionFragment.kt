package org.pragyan.dalal18.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_company_description.*
import org.pragyan.dalal18.R
import org.pragyan.dalal18.data.DalalViewModel

class CompanyDescriptionFragment : Fragment() {

    private lateinit var model: DalalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_company_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")

        Picasso.get().load(model.getImageUrlFromCompanyName(arguments?.getString(COMPANY_NAME_KEY))).into(companyImageView)
        companyNameTextView.text = arguments?.getString(COMPANY_NAME_KEY)
        companyDescriptionTextView.text = model.getDescriptionFromCompanyName(arguments?.getString(COMPANY_NAME_KEY))
    }

    companion object {
        const val COMPANY_NAME_KEY = "company-name-key"
    }
}