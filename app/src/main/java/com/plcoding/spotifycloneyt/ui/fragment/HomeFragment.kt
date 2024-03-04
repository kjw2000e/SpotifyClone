package com.plcoding.spotifycloneyt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.SongAdapter
import com.plcoding.spotifycloneyt.other.Status
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.allSongsProgressBar
import kotlinx.android.synthetic.main.fragment_home.rvAllSongs
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel
    //아래와 같은 방법도 가능하지만 activity의 라이프 사이클에 바인딩 하기 위해 provider로 만듦
//    val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var songAdapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        setUpRecyclerView()
        subscribeToObservers()

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
    }

    private fun setUpRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    allSongsProgressBar.isVisible = false
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> allSongsProgressBar.isVisible = true
            }
        }
    }
}