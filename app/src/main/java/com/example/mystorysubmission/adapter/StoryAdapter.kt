package com.example.mystorysubmission.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.mystorysubmission.databinding.ItemRowStoryBinding
import com.example.mystorysubmission.model.ListStoryItem
import com.example.mystorysubmission.ui.detail.DetailActivity

class StoryAdapter :
    PagingDataAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    class StoryViewHolder(var binding: ItemRowStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(200, 200)

            fun bind(data: ListStoryItem) {
                with(binding) {
                    val context = itemView.context

                    Glide.with(context)
                        .load(data.photoUrl)
                        .apply(requestOptions)
                        .into(ivStory)
                    tvName.text = data.name
                    tvDescription.text = data.description
                }

                itemView.setOnClickListener {
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            it.context as Activity,
                            Pair(binding.ivStory, "photo"),
                            Pair(binding.tvName, "name"),
                            Pair(binding.tvDescription, "description")
                        )

                    val intent = Intent(it.context, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.STORY, data)
                    it.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding =
            ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {

        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }

    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem)=
                oldItem == newItem

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem)=
                oldItem.id == newItem.id
        }
    }
}