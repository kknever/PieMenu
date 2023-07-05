package org.kk.piemenu

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import org.kk.lib.piemenu.PieMenuItem
import org.kk.lib.piemenu.PieMenuWidget

class MainActivity : ComponentActivity() {

    private lateinit var pieMenu: PieMenuWidget
    private var menuItem: PieMenuItem? = null
    private var centerItem: PieMenuItem? = null
    private var menuExpandItem: PieMenuItem? = null
    private var firstChildItem: PieMenuItem? = null
    private var secondChildItem: PieMenuItem? = null
    private var thirdChildItem: PieMenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        pieMenu = PieMenuWidget(this)

        centerItem = PieMenuItem("CLOSE")
        centerItem!!.setDisplayIcon(android.R.drawable.ic_menu_close_clear_cancel)

        menuItem = PieMenuItem("Normal")
        menuItem!!.setOnMenuItemPressed {}

        firstChildItem = PieMenuItem("111")
        firstChildItem!!.setOnMenuItemPressed {
            Toast.makeText(
                this,
                "click 111",
                Toast.LENGTH_SHORT
            ).show()
        }

        secondChildItem = PieMenuItem("222")
        secondChildItem!!.setDisplayIcon(R.drawable.ic_action_camera)
        secondChildItem!!.setOnMenuItemPressed {
            Toast.makeText(
                this,
                "click 222",
                Toast.LENGTH_SHORT
            ).show()
        }

        thirdChildItem = PieMenuItem("333")
        thirdChildItem!!.setDisplayIcon(R.drawable.ic_action_camera)
        thirdChildItem!!.setOnMenuItemPressed {
            Toast.makeText(
                this,
                "click 333",
                Toast.LENGTH_SHORT
            ).show()
        }


        menuExpandItem = PieMenuItem("Expandable")
        menuExpandItem!!.setDisplayIcon(R.drawable.ic_about)
        val children: MutableList<PieMenuItem> = ArrayList()
        children.add(firstChildItem!!)
        children.add(secondChildItem!!)
        children.add(thirdChildItem!!)
        menuExpandItem!!.setMenuChildren(children)

        centerItem!!.setOnMenuItemPressed {
            Toast.makeText(this, "close menu", Toast.LENGTH_SHORT).show()
            pieMenu.dismiss()
        }

        val tv = findViewById<TextView>(R.id.tv)
        val globalRect = Rect()
        tv.getGlobalVisibleRect(globalRect)
        println(globalRect.left.toString() + ", " + globalRect.right)

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

        val testButton = findViewById<View>(R.id.menu_btn) as Button
        testButton.setOnClickListener { v: View? ->
//            pieMenu.setCenterLocationWithView(tv)
            pieMenu.show(v)
        }
    }
}