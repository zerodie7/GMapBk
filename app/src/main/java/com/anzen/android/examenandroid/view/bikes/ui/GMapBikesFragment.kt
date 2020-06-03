package com.anzen.android.examenandroid.view.bikes.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.maps.android.ui.IconGenerator

import com.anzen.android.examenandroid.R
import com.anzen.android.examenandroid.databinding.GMapBikesFragmentBinding
import com.anzen.android.examenandroid.domain.PickUpBike
import com.anzen.android.examenandroid.utils.GPSTracker
import com.anzen.android.examenandroid.utils.GenericAdapter
import com.anzen.android.examenandroid.view.bikes.vm.BaseFragment
import com.anzen.android.examenandroid.view.bikes.vm.GMapBikesViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.yarolegovich.discretescrollview.DSVOrientation
import com.yarolegovich.discretescrollview.DiscreteScrollView
import kotlinx.android.synthetic.main.g_map_bikes_fragment.*
import java.util.ArrayList

class GMapBikesFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private var latGps = 19.4235714
    private var lonGps = -98.1578191
    private var radiusLimit = 1500
    private var isFabOpen = false
    private var fabOpen: Animation? = null
    private var fabClose: Animation? = null
    private var rotateForward: Animation? = null
    private var rotateBackward: Animation? = null

    private var inside: MutableList<PickUpBike> = ArrayList()

    private val _viewModel: GMapBikesViewModel by lazy {
        ViewModelProvider(this).get(GMapBikesViewModel::class.java)
    }

    private val iconGenerator: IconGenerator by lazy {
        IconGenerator(context)
    }

    private var inflater: LayoutInflater? = null

    private var gMap: GoogleMap? = null

    private val bikesAdapter = GenericAdapter<PickUpBike>(R.layout.item_location_bikes)

    override fun getLayout(): Int = R.layout.g_map_bikes_fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: GMapBikesFragmentBinding = DataBindingUtil.inflate(LayoutInflater.from(context), getLayout(), container, false)
        binding.gMapViewModel = _viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewFragmentCreated(view: View, savedInstanceState: Bundle?) {
        initComponents()
    }

    private fun initComponents() {

        _viewModel.bikeUID.value = arguments?.getInt(UID, 0)

        bikesMarkers.adapter = bikesAdapter

        _parentActivity?.startGPSTrack(object : GPSTracker.LocationChangeListener{
            override fun onChange(latitude: Double, longitude: Double) {
                latGps = latitude
                lonGps = longitude
            }
        })

        initGoogleMap()
        setSingleObserver()
        setListenersButtonFilter()

        startAnimationFab()

        //setup discrete scrolll
        setupDiscreteScroll(bikesMarkers)

    }

    private fun setupDiscreteScroll(discrete: DiscreteScrollView){
        discrete.setOverScrollEnabled(true)
        discrete.setSlideOnFling(true)
        discrete.setOrientation(DSVOrientation.HORIZONTAL)
    }

    private fun initGoogleMap(){
        var mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        if(mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.map, mapFragment!!).commit()
            mapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        this.gMap = p0
        setupMap()

        _viewModel.selectedPosition.observe(this, Observer {
            val sizeList = bikesAdapter.getItems().size
            if(it in 0 until sizeList) {
                toggleSelectMarker(true, it)
            }
        })

        _viewModel.oldSelectedPosition.observe(this, Observer {
            val sizeList = bikesAdapter.getItems().size
            if(it in 0 until sizeList){
                toggleSelectMarker( false, it)
            }
        })

    }

    private fun toggleSelectMarker(isSelected: Boolean, position: Int) {
        val marker = _viewModel.markersResult[position]
        if(!_viewModel.showService.value!!) {
            val activity = bikesAdapter.getItem(position)
            val color = resources.getColor(R.color.colorAccent)

            val c = if (ColorUtils.calculateLuminance(color) < 0.5) Color.WHITE else Color.BLACK
            val icon = createIcon(color, if(isSelected) c else null, if(isSelected) R.dimen.icon_size_map_select else R.dimen.icon_size_map)
            setupIconGenerator(color, icon, isSelected)
            marker?.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(activity?.id?.toString() ?: "")))
        }

        marker?.zIndex = 100f
        marker?.let { gMap?.animateCamera(CameraUpdateFactory.newLatLng(it.position)) }
    }

    private fun setObservers(){
        cleanObservers()
        _viewModel.selectedPosition.value = -1
        _viewModel.catBikes.observe(this, Observer {
            drawerLayout.closeDrawers()
            setBikesObserver()
        })
    }

    private fun setBikesObserver(){
        cleanObservers()
        _viewModel.getAllBikesFilter().observe(this, Observer {bikes ->
            bikesAdapter.addItems(bikes)
            cleanMarkers()

            bikes.forEach {
                createBikesMarker(it)
            }
            lyActivity.visibility = View.VISIBLE
            _viewModel.showService.value = false
            val curPos = if(_viewModel.selectedPosition.value == -1) 0 else _viewModel.selectedPosition.value
            _viewModel.selectedPosition.postValue(curPos)
        })
    }

    private fun setSingleObserver(){
        cleanObservers()
        _viewModel.selectedPosition.value = -1
        _viewModel.getBikesByUID().observe(this, Observer {bike ->
            val bikes = listOf(bike)
            bikesAdapter.addItems(bikes)
            cleanMarkers()

            bikes.forEach {
                createBikesMarker(it)
            }
            lyActivity.visibility = View.VISIBLE
            _viewModel.showService.value = false
            val curPos = if(_viewModel.selectedPosition.value == -1) 0 else _viewModel.selectedPosition.value
            _viewModel.selectedPosition.postValue(curPos)
        })
    }

    private fun cleanObservers() {
        _viewModel.getBikesByUID().removeObservers(this)
        _viewModel.getAllBikes().removeObservers(this)
    }

    private fun setListenersButtonFilter() {
        fabMap.setOnClickListener{
            animateFab()
        }

        filterBikesMap.setOnClickListener{
            _viewModel.catBikes.value = "DESC"
            setObservers()
        }

        filterGpsMap.setOnClickListener{
            _viewModel.getAllBikes().observe(this, Observer {
                it.forEach(){ bikes ->
                    val lat = bikes.lat
                    val lon = bikes.lon
                    val result = FloatArray(3)
                    Location.distanceBetween(lat!!,lon!!,latGps,lonGps,result)
                    if(result[0] <= radiusLimit) {
                        inside.add(bikes)
                    }
                }
                bikesAdapter.addItems(inside)
                cleanMarkers()

                inside.forEach {bk ->
                    createBikesMarker(bk)
                }

                inside.clear()

                lyActivity.visibility = View.VISIBLE
                _viewModel.showService.value = false
                val curPos = if(_viewModel.selectedPosition.value == -1) 0 else _viewModel.selectedPosition.value
                _viewModel.selectedPosition.postValue(curPos)
            })
        }

        filterSlotsMap.setOnClickListener{
            _viewModel.catBikes.value = "ASC"
            setObservers()
        }

    }

    private fun cleanMarkers() {
        _viewModel.markersResult.forEach {
            it?.remove()
        }
        _viewModel.markersResult.clear()
    }

    private fun createBikesMarker(pickUpBike: PickUpBike) {
        val latitude = pickUpBike.lat ?: 0.0
        val longitude = pickUpBike.lon ?: 0.0
        if(latitude <= 0.0 && longitude <= 0.0){
            _viewModel.markersResult.add(null)
            return
        }

        val latLng = LatLng(latitude, longitude)
        val title = pickUpBike.id?.toString() ?: ""
        val color = resources.getColor(R.color.colorAccent)

        val icon = createIcon(color)
        setupIconGenerator(color, icon)
        val markerOpt = createMarker(latLng, title)
        _viewModel.markersResult.add(gMap?.addMarker(markerOpt))
    }

    private fun createMarker(latLng: LatLng, title: String): MarkerOptions? {
        return MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(title))).position(latLng).title(title).anchor(iconGenerator.anchorU, iconGenerator.anchorV)
    }

    private fun setupIconGenerator(color: Int, icon: Drawable, isSelected: Boolean = false, isService: Boolean = false) {
        iconGenerator.setBackground(icon)
        context?.let { ctx ->
            if (inflater == null)
                inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater?.inflate(R.layout.map_view, null, false)
            view?.let { v ->
                val resMargin = if (isService) R.dimen.margin_dx else R.dimen.separator
                val margin = if (isSelected) resources.getDimensionPixelSize(resMargin) else resources.getDimensionPixelSize(resMargin)
                val size =
                    if (isSelected && isService) resources.getDimensionPixelSize(R.dimen.image_size_map_selected)
                    else if (isSelected && !isService) resources.getDimensionPixelSize(R.dimen.icon_size_map_select)
                    else if (isService) resources.getDimensionPixelSize(R.dimen.image_size_map)
                    else resources.getDimensionPixelSize(R.dimen.icon_size_map)
                val textSize =
                    if (isSelected) resources.getDimensionPixelSize(R.dimen.text_size_map_selected) else resources.getDimensionPixelSize(
                        R.dimen.text_size_map
                    )
                val imageView = v.findViewById<ImageView>(R.id.iconMarker)
                val textView = v.findViewById<TextView>(R.id.amu_text)
                val childView = if (isService) imageView else textView
                val params = childView.layoutParams as LinearLayout.LayoutParams
                params.height = size
                params.width = size
                //params.margin = margin
                childView.layoutParams = params
                childView.requestLayout()

                if (isService) {
                    textView.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                } else {
                    imageView.visibility = View.GONE
                    textView.visibility = View.VISIBLE
                    textView.textSize = textSize.toFloat()
                    val c = if(ColorUtils.calculateLuminance(color) < 0.5) android.R.color.white else android.R.color.black
                    textView.setTextColor(ContextCompat.getColor(ctx, c))
                }
                iconGenerator.setContentView(v)
            }
        }
    }

    private fun createIcon(color: Int, colorBorder: Int? = null, resSize: Int = R.dimen.icon_size_map): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(color)
        val size = resources.getDimensionPixelOffset(resSize).toInt()
        drawable.setSize(size, size)
        colorBorder?.let {
            drawable.setStroke(resources.getDimensionPixelSize(R.dimen.icon_stroke_map), it)
        }
        return drawable
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val pos = _viewModel.markersResult.indexOf(p0)
        if(pos >= 0 && pos < _viewModel.markersResult.size) {
            _viewModel.oldSelectedPosition.postValue(_viewModel.selectedPosition.value)
            _viewModel.selectedPosition.postValue(pos)
            bikesMarkers.smoothScrollToPosition(pos)
        }
        return true
    }

    private fun setupMap() {
        gMap?.setOnMarkerClickListener(this)
            gMap?.let { map ->
                // Restriccion de area en mapa para cordenadas especificas
                    //val northeastBound = LatLng(19.46642562, -99.17859716)
                    //val southwestBound = LatLng(19.356542, -99.073197)
                    //val mapBounds = LatLngBounds(southwestBound, northeastBound)
                    val centerMap = LatLng(19.408921, -99.130357)
                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    map.setMinZoomPreference(14F)
                    map.setMaxZoomPreference(16F)
                    map.isBuildingsEnabled = false
                    map.uiSettings.isCompassEnabled = true
                    map.uiSettings.isTiltGesturesEnabled = false
                    map.uiSettings.isRotateGesturesEnabled = false
                    map.uiSettings.isMyLocationButtonEnabled = false
                    //map.setLatLngBoundsForCameraTarget(mapBounds)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(centerMap, 15F))
            }
    }

    private fun animateFab(){
        isFabOpen =
            if(isFabOpen){
                fabMap.startAnimation(rotateBackward)
                filterBikesMap.startAnimation(fabClose)
                filterGpsMap.startAnimation(fabClose)
                filterSlotsMap.startAnimation(fabClose)
                false
            } else {
                fabMap.startAnimation(rotateForward)
                filterBikesMap.startAnimation(fabOpen)
                filterGpsMap.startAnimation(fabOpen)
                filterSlotsMap.startAnimation(fabOpen)
                true
            }
    }

    private fun startAnimationFab(){
        fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        rotateForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
        rotateBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)
    }

    companion object {
        const val UID = "UID"
    }
}
