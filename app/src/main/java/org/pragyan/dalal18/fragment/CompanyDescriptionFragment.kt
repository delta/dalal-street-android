package org.pragyan.dalal18.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import org.pragyan.dalal18.data.DalalViewModel
import org.pragyan.dalal18.databinding.FragmentCompanyDescriptionBinding
import org.pragyan.dalal18.utils.viewLifecycle

class CompanyDescriptionFragment : Fragment() {

    private var binding by viewLifecycle<FragmentCompanyDescriptionBinding>()

    private lateinit var model: DalalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCompanyDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = activity?.run { ViewModelProvider(this).get(DalalViewModel::class.java) }
                ?: throw Exception("Invalid activity")

        val currentStockId = arguments?.getInt(COMPANY_STOCK_ID_KEY) ?: return

        binding.apply {
            Picasso.get().load(model.getImageUrlFromStockId(currentStockId)).into(companyImageView)
            companyNameTextView.text = model.getCompanyNameFromStockId(currentStockId)
            companyDescriptionTextView.text = model.getDescriptionFromStockId(currentStockId)
        }
    }

    companion object {
        const val COMPANY_STOCK_ID_KEY = "company-stock-id-key"
    }
}
