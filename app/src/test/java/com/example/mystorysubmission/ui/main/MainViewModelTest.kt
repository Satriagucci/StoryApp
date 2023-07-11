package com.example.mystorysubmission.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.mystorysubmission.adapter.StoryAdapter
import com.example.mystorysubmission.data.StoryRepository
import com.example.mystorysubmission.model.ListStoryItem
import com.example.mystorysubmission.utils.Dummy
import com.example.mystorysubmission.utils.MainDispatcherRules
import com.example.mystorysubmission.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
internal class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRules()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Test
    fun `when Get Stories Successful Should Not Null and Return Data`() = runTest {
        val dummyStories = Dummy.generateDummyStory()
        val data: PagingData<ListStoryItem> = StoryPagingResource.snapshot(dummyStories)

        val expectedStories = MutableLiveData<PagingData<ListStoryItem>>()

        expectedStories.value = data
        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedStories)

        val mainViewModel = MainViewModel(storyRepository)
        val actualStories: PagingData<ListStoryItem> = mainViewModel.storyPaging().getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)

        Assert.assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Stories Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStories = MutableLiveData<PagingData<ListStoryItem>>()

        expectedStories.value = data

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedStories)

        val storyViewModel = MainViewModel(storyRepository)
        val actualStories: PagingData<ListStoryItem> = storyViewModel.storyPaging().getOrAwaitValue()

        differ.submitData(actualStories)
        // 				□ Memastikan jumlah data yang dikembalikan nol.
        Assert.assertEquals(0, differ.snapshot().size)
    }

}

class StoryPagingResource : PagingSource<Int, LiveData<List<ListStoryItem>>>() {
    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }

    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
