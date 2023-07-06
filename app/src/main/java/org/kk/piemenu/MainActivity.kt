package org.kk.piemenu

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import org.kk.lib.piemenu.PieMenuItem
import org.kk.lib.piemenu.PieMenuWidget
import org.kk.piemenu.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var pieMenu: PieMenuWidget
    private var menuItem: PieMenuItem? = null
    private var centerItem: PieMenuItem? = null
    private var menuExpandItem: PieMenuItem? = null
    private var child1: PieMenuItem? = null
    private var child2: PieMenuItem? = null
    private var child3: PieMenuItem? = null
    private var child4: PieMenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        pieMenu = PieMenuWidget(this)

        centerItem = PieMenuItem("CLOSE")
        centerItem!!.setDisplayIcon(android.R.drawable.ic_menu_close_clear_cancel)

        menuItem = PieMenuItem("Normal")
        menuItem!!.setOnMenuItemPressed {}

        child1 = PieMenuItem("111")
        child1!!.setOnMenuItemPressed {
            Toast.makeText(
                this,
                "click 111",
                Toast.LENGTH_SHORT
            ).show()
        }

        child2 = PieMenuItem("222")
        child2!!.setDisplayIcon(R.drawable.ic_action_camera)
        child2!!.setOnMenuItemPressed {
            Toast.makeText(
                this,
                "click 222",
                Toast.LENGTH_SHORT
            ).show()
        }

        child3 = PieMenuItem("333")
        child3!!.setDisplayIcon(R.drawable.ic_action_camera)
        child3!!.setOnMenuItemPressed {
            Toast.makeText(
                this,
                "click 333",
                Toast.LENGTH_SHORT
            ).show()
        }

        child4 = PieMenuItem("444")
        child4!!.setDisplayIcon(R.drawable.ic_action_camera)
        child4!!.setOnMenuItemPressed {
            Toast.makeText(
                this,
                "click 444",
                Toast.LENGTH_SHORT
            ).show()
        }

        menuExpandItem = PieMenuItem("Expandable")
        menuExpandItem!!.setDisplayIcon(R.drawable.ic_about)
        val children: MutableList<PieMenuItem> = ArrayList()
        children.add(child1!!)
        children.add(child2!!)
        children.add(child3!!)
        children.add(child4!!)
        menuExpandItem!!.setMenuChildren(children)

        centerItem!!.setOnMenuItemPressed {
            Toast.makeText(this, "close menu", Toast.LENGTH_SHORT).show()
            pieMenu.dismiss()
        }

        pieMenu.setDismissOnOutsideClick(false)
        pieMenu.setAnimationSpeed(0)
        pieMenu.setIconSize(15, 30)
        pieMenu.setTextSize(12)
        pieMenu.setOutlineColor(Color.RED, 225)
        pieMenu.setInnerRingColor(Color.BLUE, 180)
        pieMenu.setOuterRingColor(Color.GREEN, 180)
        pieMenu.setHeader("Test Menu", 20)
        pieMenu.setCenterCircle(centerItem)
        pieMenu.setDisabledColor(Color.BLACK, 80)
        pieMenu.setOuterSelectedColor(Color.RED, 255)
        pieMenu.setTextColor(Color.parseColor("#FF00AA"), 255)

        pieMenu.addMenuEntry(object : ArrayList<PieMenuItem?>() {
            init {
//				add(menuItem);
                add(menuExpandItem)
                add(menuExpandItem)
                add(menuExpandItem)
                add(menuExpandItem)
            }
        })

        binding.menuBtn.setOnClickListener { v: View? ->
//            pieMenu.setCenterLocationWithView(tv)
            pieMenu.show(v)
        }
    }
}