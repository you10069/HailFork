package com.aistra.hail.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.aistra.hail.app.AppInfo
import com.aistra.hail.app.HailData
import com.aistra.hail.app.HailData.tags
import com.aistra.hail.databinding.FragmentHomeBinding
import com.aistra.hail.extensions.applyDefaultInsetter
import com.aistra.hail.extensions.isLandscape
import com.aistra.hail.extensions.isRtl
import com.aistra.hail.extensions.paddingRelative
import com.aistra.hail.ui.main.MainFragment
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : MainFragment() {
    var multiselect: Boolean = false
    val selectedList: MutableList<AppInfo> = mutableListOf()
    private var _binding: FragmentHomeBinding? = null
    private var tabLayoutMediator: TabLayoutMediator? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupPager()
        binding.tabs.applyDefaultInsetter { paddingRelative(isRtl, start = !activity.isLandscape, end = true) }
        return binding.root
    }


    private fun setupPager(selectedTagId: Int? = null) {
        tabLayoutMediator?.detach()
        binding.pager.adapter = HomeAdapter(this)
        tabLayoutMediator = TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text = tags[position].first
        }.also { it.attach() }
        binding.tabs.isVisible = tags.size > 1
        selectedTagId?.let { tagId ->
            tags.indexOfFirst { it.second == tagId }.takeIf { it >= 0 }?.let { position ->
                binding.pager.setCurrentItem(position, false)
            }
        }
    }

    fun reorderTags(orderedTags: List<Pair<String, Int>>, selectedTagId: Int) {
        binding.pager.post {
            if (_binding != null && HailData.reorderTags(orderedTags)) setupPager(selectedTagId)
        }
    }

    override fun onDestroyView() {
        multiselect = false
        selectedList.clear()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        super.onDestroyView()
        _binding = null
    }
}