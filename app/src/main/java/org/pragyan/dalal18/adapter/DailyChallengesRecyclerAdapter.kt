package org.pragyan.dalal18.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dalalstreet.api.models.DailyChallengeOuterClass
import dalalstreet.api.models.UserStateOuterClass
import org.pragyan.dalal18.R
import org.pragyan.dalal18.databinding.ChallengeListItemBinding

class DailyChallengesRecyclerAdapter(
        private val dailyChallenges: MutableList<DailyChallengeOuterClass.DailyChallenge>,
        private val userStates: MutableList<UserStateOuterClass.UserState>,
        private val checkUserStateListener: CheckUserStateListener
) : RecyclerView.Adapter<DailyChallengesRecyclerAdapter.DailyChallengeViewHolder>() {
    inner class DailyChallengeViewHolder(val binding: ChallengeListItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyChallengeViewHolder {
        return DailyChallengeViewHolder(ChallengeListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int = dailyChallenges.size

    override fun onBindViewHolder(holder: DailyChallengeViewHolder, position: Int) {
//
        Log.i("daily",dailyChallenges[position].toString())
        val challengeId = dailyChallenges[position].challengeId
        val challengeType = dailyChallenges[position].challengeType
        val value = dailyChallenges[position].value
        if(challengeType.equals("Cash")||challengeType.equals("NetWorth")||challengeType.equals("StockWorth"))
            holder.binding.challengeText.text = "Increase your ${challengeType} to ${value}"
        else{
            val stockId = dailyChallenges[position].stockId
            val companyName = checkUserStateListener.getCompanyNameFromStockId(stockId)
            holder.binding.challengeText.text = "Increase the number of stocks of ${companyName} to ${value}"
        }
        if(checkUserStateListener.isDailyChallengeOpen()){
            holder.binding.hintProgressBar.visibility = View.VISIBLE
            holder.binding.hintImage.visibility=View.GONE
            holder.binding.claimRewardButton.visibility=View.GONE
        }else{
            if(userStates[position].isCompleted && userStates[position].isRewardClamied){
                holder.binding.hintProgressBar.visibility = View.GONE
                holder.binding.hintImage.visibility=View.VISIBLE
                holder.binding.hintImage.setImageResource(R.drawable.blue_thumb)
                holder.binding.claimRewardButton.visibility=View.GONE
            }else if(userStates[position].isCompleted && !userStates[position].isRewardClamied){
                holder.binding.hintProgressBar.visibility = View.GONE
                holder.binding.hintImage.visibility=View.GONE
                holder.binding.claimRewardButton.visibility=View.VISIBLE
                holder.binding.claimRewardButton.setOnClickListener {
                    checkUserStateListener.claimReward(userStates[position].id)
                }
            }else{
                holder.binding.hintProgressBar.visibility = View.GONE
                holder.binding.hintImage.visibility=View.VISIBLE
                holder.binding.hintImage.setImageResource(R.drawable.clear_icon)
                holder.binding.claimRewardButton.visibility=View.GONE
            }

        }

    }
    interface CheckUserStateListener{
        fun claimReward(Id:Int)
        fun getCompanyNameFromStockId(stockId:Int) : String
        fun isDailyChallengeOpen():Boolean
    }

}