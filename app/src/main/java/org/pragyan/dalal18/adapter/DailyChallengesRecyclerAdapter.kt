package org.pragyan.dalal18.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dalalstreet.api.models.DailyChallengeOuterClass
import org.pragyan.dalal18.databinding.ChallengeListItemBinding

class DailyChallengesRecyclerAdapter(
        private val dailyChallenges: MutableList<DailyChallengeOuterClass.DailyChallenge>,
        private val checkUserStateListener: CheckUserStateListener
) : RecyclerView.Adapter<DailyChallengesRecyclerAdapter.DailyChallengeViewHolder>() {
    inner class DailyChallengeViewHolder(val binding: ChallengeListItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyChallengeViewHolder {
        return DailyChallengeViewHolder(ChallengeListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int = dailyChallenges.size

    override fun onBindViewHolder(holder: DailyChallengeViewHolder, position: Int) {
//        val dailyChallenge = dailyChallenges.get(position)
      Log.i("daily",dailyChallenges[position].toString());
        holder.binding.challengeText.text = "Increase stocks of Amazon to 30000"

    }
    interface CheckUserStateListener{
        fun checkChallengeState(challengeId:Int)
    }

}