package org.pragyan.dalal18.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import org.pragyan.dalal18.databinding.FragmentSponsorBinding
import org.pragyan.dalal18.utils.viewLifecycle


class SponsorFragment : Fragment() {

    private var binding by viewLifecycle<FragmentSponsorBinding>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSponsorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            Picasso.get().load("https://avikumar15.github.io/companies/DSIJ_Logo.png").into(partner1)
            Picasso.get().load("https://avikumar15.github.io/companies/MyCapt_Logo.png").into(partner2)
            Picasso.get().load("https://avikumar15.github.io/companies/CEO_Logo.png").into(partner3)
        }
    }


}