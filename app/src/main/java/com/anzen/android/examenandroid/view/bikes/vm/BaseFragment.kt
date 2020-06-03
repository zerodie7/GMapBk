package com.anzen.android.examenandroid.view.bikes.vm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anzen.android.examenandroid.view.general.ui.MainActivity


abstract class BaseFragment : Fragment() {

    private lateinit var _parentView: View
    val _parentActivity: MainActivity? by lazy{
        activity?.let {
            it as MainActivity
        } ?: kotlin.run { null }
    }

    abstract fun getLayout(): Int

    abstract fun onViewFragmentCreated(view: View, savedInstanceState: Bundle?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _parentView = inflater.inflate(getLayout(), container, false)
        return _parentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewFragmentCreated(view, savedInstanceState)
    }

    fun navigate(des: Int, args: Bundle = Bundle.EMPTY){
        _parentActivity?.navigate(des, args)
    }

}
