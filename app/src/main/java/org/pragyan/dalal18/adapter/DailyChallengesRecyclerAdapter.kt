package org.pragyan.dalal18.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dalalstreet.api.models.DailyChallengeOuterClass
import dalalstreet.api.models.UserStateOuterClass
import org.jetbrains.anko.textColor
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
        val rewardAmount = dailyChallenges[position].reward


        if(challengeType.equals("Cash")||challengeType.equals("NetWorth")||challengeType.equals("StockWorth"))
            holder.binding.challengeText.text = "Increase your ${challengeType} by ${value}"
        else{
            val stockId = dailyChallenges[position].stockId
            val companyName = checkUserStateListener.getCompanyNameFromStockId(stockId)
            holder.binding.challengeText.text = "Increase the number of stocks of ${companyName} by ${value}"
        }
        holder.binding.rewardText.text = "\uD83D\uDCB5 $rewardAmount"
        if(checkUserStateListener.isDailyChallengeOpen()){
            val initialValue = userStates[position].initialValue
            holder.binding.progressTextView.visibility = View.VISIBLE

            holder.binding.progressImage.visibility=View.GONE
            holder.binding.claimRewardButton.visibility=View.GONE
            var progress:Long=0
            when(challengeType){
                "Cash"->{
                    val currentCash = checkUserStateListener.getCashWorth()
                    progress = currentCash - initialValue

                }
                "NetWorth"->{
                    val netWorth = checkUserStateListener.getNetWorth()
                     progress = netWorth - initialValue
                }
                "StockWorth"->{
                    val stockWorth = checkUserStateListener.getStockWorth()
                    progress = stockWorth - initialValue
                }
                else->{
                    val stockId = dailyChallenges[position].stockId
                    val stocksOwned = checkUserStateListener.getCurrentStocks(stockId)
                    progress = stocksOwned-initialValue
                }
            }
            if(progress< value){
                holder.binding.progressTextView.textColor = Color.RED
            }else{
                holder.binding.progressTextView.textColor = Color.GREEN
            }
            holder.binding.progressTextView.text = "${progress}/${value}"
        }else{
            if(!userStates.isEmpty()) {
                if (userStates[position].isCompleted && userStates[position].isRewardClamied) {
                    holder.binding.progressTextView.visibility = View.GONE
                    holder.binding.progressImage.visibility = View.VISIBLE
                    holder.binding.progressImage.setImageResource(R.drawable.blue_thumb)
                    holder.binding.claimRewardButton.visibility = View.GONE
                } else if (userStates[position].isCompleted && !userStates[position].isRewardClamied) {
                    holder.binding.progressTextView.visibility = View.GONE
                    holder.binding.progressImage.visibility = View.GONE
                    holder.binding.claimRewardButton.visibility = View.VISIBLE
                    holder.binding.claimRewardButton.setOnClickListener {
                        checkUserStateListener.claimReward(userStates[position].id, holder.binding.claimRewardButton,holder.binding.progressImage)
                    }
                } else {
                    holder.binding.progressTextView.visibility = View.GONE
                    holder.binding.progressImage.visibility = View.VISIBLE
                    holder.binding.progressImage.setImageResource(R.drawable.clear_icon)
                    holder.binding.claimRewardButton.visibility = View.GONE
                }
            }else{
                holder.binding.rewardText.visibility = View.INVISIBLE
                holder.binding.claimRewardButton.visibility = View.INVISIBLE
                holder.binding.progressTextView.visibility=View.INVISIBLE
                holder.binding.progressImage.visibility = View.INVISIBLE
            }

        }

    }


    interface CheckUserStateListener{
        fun claimReward(Id: Int, button: Button, progressImage: ImageView)
        fun getCompanyNameFromStockId(stockId:Int) : String
        fun isDailyChallengeOpen():Boolean
        fun getCashWorth():Long
        fun getStockWorth():Long
        fun getNetWorth():Long
        fun getCurrentStocks(stockId: Int):Long
    }

}