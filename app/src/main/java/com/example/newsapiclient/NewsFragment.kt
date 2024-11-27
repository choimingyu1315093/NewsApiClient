package com.example.newsapiclient

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.Resource
import com.example.newsapiclient.databinding.FragmentNewsBinding
import com.example.newsapiclient.presentation.MainActivity
import com.example.newsapiclient.presentation.adapter.NewsAdapter
import com.example.newsapiclient.presentation.viewmodel.NewsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewsFragment : Fragment() {
    private lateinit var binding: FragmentNewsBinding
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    companion object {
        const val TAG = "NewsFragment"
    }

    private var country = "us"
    private var page = 1

    //Paging 라이브러리 수동으로 하는거라서 한 번 보기만 하면 된다.
    private var isScrolling = false
    private var isLoading = false
    private var isLastPage = false
    private var pages = 0
    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = binding.rvNews.layoutManager as LinearLayoutManager
            val sizeOfTheCurrentList = layoutManager.itemCount
            val visibleItems = layoutManager.childCount
            val topPosition = layoutManager.findFirstVisibleItemPosition()

            val hasReachedToEnd = topPosition+visibleItems >= sizeOfTheCurrentList
            val shouldPaginate = !isLoading && !isLastPage && hasReachedToEnd && isScrolling
            if(shouldPaginate){
                pages++
                viewModel.getNewsHeadlines(country, page)
                isScrolling = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewsBinding.bind(view)

        init()
        bindViews()
        observeViewModels()
    }

    private fun init() = with(binding){
        viewModel = (activity as MainActivity).viewModel
        newsAdapter = (activity as MainActivity).newsAdapter
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putParcelable("selected_article", it)
            }
            findNavController().navigate(R.id.action_newsFragment_to_infoFragment, bundle)
        }

//        newsAdapter = NewsAdapter()
        rvNews.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = newsAdapter
            addOnScrollListener(this@NewsFragment.onScrollListener)
        }

        viewModel.getNewsHeadlines(country, page)
    }

    private fun bindViews() = with(binding){
        svNews.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchNews(country, query.toString(), page)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                lifecycleScope.launch {
                    delay(2000)
                    viewModel.searchNews(country, newText.toString(), page)
                }
                return false
            }
        })
        
        svNews.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                rvNews.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = newsAdapter
                    addOnScrollListener(this@NewsFragment.onScrollListener)
                }

                viewModel.getNewsHeadlines(country, page)
                return false
            }
        })
    }

    private fun observeViewModels() = with(binding){
        viewModel.newsHeadlines.observe(viewLifecycleOwner){response ->
            when(response){
                is com.example.newsapiclient.data.util.Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles)
                        if(it.totalResults%20 == 0){
                            pages = it.totalResults/20
                        }else {
                            pages = it.totalResults/20 + 1
                        }
                        isLastPage = page == pages
                    }
                }
                is com.example.newsapiclient.data.util.Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(activity, "An error occurred: $it", Toast.LENGTH_LONG).show()
                    }
                }
                is com.example.newsapiclient.data.util.Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        viewModel.searchedNews.observe(viewLifecycleOwner){response ->
            when(response){
                is com.example.newsapiclient.data.util.Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles)
                        if(it.totalResults%20 == 0){
                            pages = it.totalResults/20
                        }else {
                            pages = it.totalResults/20 + 1
                        }
                        isLastPage = page == pages
                    }
                }
                is com.example.newsapiclient.data.util.Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(activity, "An error occurred: $it", Toast.LENGTH_LONG).show()
                    }
                }
                is com.example.newsapiclient.data.util.Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun showProgressBar() = with(binding){
        isLoading = true
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() = with(binding){
        isLoading = false
        progressBar.visibility = View.GONE
    }
}