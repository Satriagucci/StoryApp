package com.example.mystorysubmission.utils

import com.example.mystorysubmission.model.ListStoryItem

object Dummy {
    fun generateDummyStory(): List<ListStoryItem> {
        val storyList = ArrayList<ListStoryItem>()
        for (i in 1..20) {
            val stories = ListStoryItem(
                photoUrl =  "0",
                createdAt = "",
                name = "Name $i",
                description = "Description $i",
                id = "id_$i",
                lat = i.toDouble() * 10,
                lon = i.toDouble() * 10
            )
            storyList.add(stories)
        }
        return storyList
    }
}