package com.anzen.android.examenandroid.view.bikes.ui

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.anzen.android.examenandroid.R
import com.anzen.android.examenandroid.databinding.ListBikesFragmentBinding
import com.anzen.android.examenandroid.domain.PickUpBike
import com.anzen.android.examenandroid.utils.GPSTracker
import com.anzen.android.examenandroid.utils.GenericAdapter
import com.anzen.android.examenandroid.view.bikes.vm.BaseFragment
import com.anzen.android.examenandroid.view.bikes.vm.ListBikesViewModel
import kotlinx.android.synthetic.main.list_bikes_fragment.*
import java.util.*


class ListBikesFragment : BaseFragment() {

    private var latGps = 19.4235714
    private var lonGps = -99.1578191
    private var radiusLimit = 1500
    private var isFabOpen = false
    private var fabOpen: Animation? = null
    private var fabClose:Animation? = null
    private var rotateForward:Animation? = null
    private var rotateBackward:Animation? = null

    private var inside: MutableList<PickUpBike> = ArrayList()

    private val _viewModel:  ListBikesViewModel by lazy {
        ViewModelProvider(this).get(ListBikesViewModel::class.java)
    }

    private val bikesAdapter = GenericAdapter<PickUpBike>(R.layout.item_location_bikes)

    override fun getLayout(): Int = R.layout.list_bikes_fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: ListBikesFragmentBinding = DataBindingUtil.inflate(LayoutInflater.from(context), getLayout(), container, false)
        binding.bikeViewModel = _viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewFragmentCreated(view: View, savedInstanceState: Bundle?) {
        initComponents()
    }

    private fun initComponents() {
        rvBikes.adapter = bikesAdapter

        _viewModel.refreshBypassCache()

        _parentActivity?.startGPSTrack(object : GPSTracker.LocationChangeListener{
            override fun onChange(latitude: Double, longitude: Double) {
                latGps = latitude
                lonGps = longitude
            }
        })

        startAnimationFab()
        setListenersButtonFilter()
        observeViewModel()
    }

    private fun setListenersButtonFilter() {

        fab.setOnClickListener{
            animateFab()
        }

        filterBikes.setOnClickListener{
            _viewModel.getBikesAsc().observe(this, Observer {
                it.let {
                    _viewModel.empty.value = false
                    bikesAdapter.addItems(it)
                }
            })
        }

        filterGps.setOnClickListener{
            _viewModel.getAllBikes().observe(this, Observer {
                it.forEach(){ bikes ->
                    val lat = bikes.lat
                    val lon = bikes.lon
                    val result = FloatArray(3)
                    Location.distanceBetween(lat!!,lon!!,latGps,lonGps,result)
                    if(result[0] <= radiusLimit) {
                        inside.add(bikes)
                    }
                    _viewModel.empty.value = inside.size == 0
                }
                bikesAdapter.addItems(inside)
                inside.clear()
            })
        }

        filterSlots.setOnClickListener{
            _viewModel.getSlotsDesc().observe(this, Observer {
                it.let {
                    _viewModel.empty.value = false
                    bikesAdapter.addItems(it)

                }
            })
        }

        setListenerAdapter(bikesAdapter)
    }

    private fun setListenerAdapter(bikesAdapter: GenericAdapter<PickUpBike>) {
        bikesAdapter.setOnListItemViewClickListener(object : GenericAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                showMapBike(bikesAdapter.getItem(position))
            }
        })
    }

    private fun showMapBike(bikeList: PickUpBike?) {
        bikeList?.let {
            val args = bundleOf(GMapBikesFragment.UID to it.id)
            navigate(R.id.action_listBikesFragment_to_gMapBikesFragment, args)
        }
    }

    private fun observeViewModel() {
        cleanObservables()

        _viewModel.getAllBikes().observe(this, Observer {
            bikesAdapter.addItems(it)
        })

    }

    private fun animateFab(){
        isFabOpen =
            if(isFabOpen){
            fab.startAnimation(rotateBackward)
            filterBikes.startAnimation(fabClose)
            filterGps.startAnimation(fabClose)
            filterSlots.startAnimation(fabClose)
            false
        } else {
            fab.startAnimation(rotateForward)
            filterBikes.startAnimation(fabOpen)
            filterGps.startAnimation(fabOpen)
            filterSlots.startAnimation(fabOpen)
            true
        }
    }

    private fun startAnimationFab(){
        fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        rotateForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
        rotateBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)
    }

    private fun cleanObservables() {
        _viewModel.getAllBikes().removeObservers(this)
        _viewModel.getSlotsDesc().removeObservers(this)
        _viewModel.getBikesAsc().removeObservers(this)
    }
}
