package com.example.apitest

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.ebest.api.*
import com.google.android.material.tabs.TabLayout
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

class s1002 : Fragment() {

    internal val RECEIVE_INITECHERROR = -6               // initech 핸드세이킹 에러
    internal val RECEIVE_PERMISSIONERROR = -5               // 퍼미션취소
    internal val RECEIVE_ERROR = -4               // 일반적인 에러
    internal val RECEIVE_SYSTEMERROR = -2               // 서버에서 내려주는 시스템에러
    internal val RECEIVE_DATA = 1                // TR데이타 수신
    internal val RECEIVE_REALDATA = 2                // 실시간데이타 수신
    internal val RECEIVE_MSG = 3                // TR메세지 수신

    internal var m_nHandle = -1
    internal var handler: ProcMessageHandler? = null
    lateinit internal var manager: SocketManager
    internal inner class ProcMessageHandler : Handler() {

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.what) {

                // 퍼미션 에러
                RECEIVE_PERMISSIONERROR -> {
                    activity?.finishAffinity();                       // 해당앱의 루트 액티비티를 종료시킨다.
                    System.runFinalization();               // 현재 작업중인 쓰레드가 종료되면 종료 시키라는 명령어
                    System.exit(0);                 // 현재 액티비티를 종료시킨다.
                }

                // INITECH 핸드세이킹 에러
                RECEIVE_INITECHERROR -> {
                    val strMsg = msg.obj as String
                    Toast.makeText(activity?.applicationContext, strMsg, Toast.LENGTH_SHORT).show()
                }


                // 일반적인 에러
                RECEIVE_ERROR -> {
                    val strMsg = msg.obj as String
                    Toast.makeText(activity?.applicationContext, strMsg, Toast.LENGTH_SHORT).show()
                }

                // 서버에서 보내는 시스템 ERROR
                RECEIVE_SYSTEMERROR -> {
                    val pMsg = msg.obj as MsgPacket
                    Toast.makeText(
                        activity?.applicationContext,
                        pMsg.strMessageData,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // TR데이타
                RECEIVE_DATA -> {
                    val lpDp = msg.obj as DataPacket

                    Log.e(">>",lpDp.strTRCode)
                    if (lpDp.strTRCode == "t1305") {
                        processT1305(lpDp.strBlockName!!, lpDp.pData!!)

                    }
                }

                // REAL데이타
                RECEIVE_REALDATA -> {
                    val lpRp = msg.obj as RealPacket
                    if (lpRp.strBCCode == "") {

                    }
                }


                // TR메세지
                RECEIVE_MSG -> {
                    val lpMp = msg.obj as MsgPacket
                    Toast.makeText(
                        activity?.applicationContext,
                        lpMp.strTRCode + " " + lpMp.strMsgCode + lpMp.strMessageData,
                        Toast.LENGTH_SHORT
                    ).show()
               }
                // 접속종료 또는 재연결
                mainView.RECEIVE_DISCONNECT ,  mainView.RECEIVE_RECONNECT->{
                    mainView.onMessage(msg);
                }
            }
        }
    }

    lateinit var mainView: MainView
    lateinit var root: View

    internal var m_adapter = TableGrid().DataAdapter(R.layout.s1002_item01)
    lateinit var m_gridView: GridView
    //lateinit var m_gridView: TableGrid2

    private var m_dwm = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.activity_s1002, container, false)
        mainView = (activity as MainView)


        m_gridView = root.findViewById<View>(R.id.grid_view) as GridView
        m_gridView.adapter = m_adapter

        //m_gridView = TableGrid2(mainView)
        //val grid_layout = root.findViewById<LinearLayout>(R.id.grid_view)
        //m_gridView.InitTableGrid(7,grid_layout,null,null,null,null)

        root.findViewById<Button>(R.id.button).setOnClickListener {
            requestT1305()
        }

        val arraylst : ArrayList<String> = ArrayList()
        arraylst.add("일")
        arraylst.add("주")
        arraylst.add("월")
        root.findViewById<Spinner>(R.id.spinner).adapter = ArrayAdapter<String>(mainView,R.layout.support_simple_spinner_dropdown_item,arraylst)

        root.findViewById<Spinner>(R.id.spinner).setOnItemSelectedListener( object  : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                m_dwm = p2+1
            }
        })
        root.findViewById<Spinner>(R.id.spinner).setSelection(0)

        val instance = Calendar.getInstance()
        val year = instance.get(Calendar.YEAR).toString()
        var month = (instance.get(Calendar.MONTH)+1).toString()
        var date = instance.get(Calendar.DATE).toString()
        if (month.toInt() < 10) {
            month = "0$month"
        }
        if (date.toInt() < 10) {
            date = "0$date"
        }
        root.findViewById<EditText>(R.id.date_edit).setText(year+month+date)
        root.findViewById<EditText>(R.id.cnt_edit).setText("20")

        // get SocketManager instance
        handler = ProcMessageHandler()
        manager = (activity?.application as ApplicationManager).getSockInstance()

        return root
    }
    override fun onResume() {
        super.onResume()
        /* 화면 갱신시 핸들 재연결 */
        m_nHandle = manager.setHandler(mainView, handler as Handler)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        /* 해당 화면을 사용하지 않을떄 핸들값 삭제 */
        manager.deleteHandler(m_nHandle)
    }

    private fun requestT1305()
    {
        m_adapter.items.clear()
        m_adapter.notifyDataSetChanged()

        val edit = root.findViewById<EditText>(R.id.editText)
        /* size 6 */
        val shcode = edit.text.toString()
        if(shcode.length < 6)
        {
            Toast.makeText(
                activity?.applicationContext,
                "종목코드를 확인해 주십시오.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        /* size 1*/
        val dwmcode = m_dwm.toString()

        val dateed = root.findViewById<EditText>(R.id.date_edit)
        /* size 8*/
        val date = dateed.text.toString()
        if(date.length < 8)
        {
            Toast.makeText(
                activity?.applicationContext,
                "날짜를 확인해 주십시오.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        /* size 4 */
        val idx = "    "

        /* size 4 */
        val cnt = root.findViewById<EditText>(R.id.cnt_edit).text.toString().toInt()

        if( (cnt < 0) or (cnt > 10000))
        {
            Toast.makeText(
                activity?.applicationContext,
                "조회수량을 확인해 주십시오.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val cnt_string = String.format("%04d",cnt)

        /* inblock 데이터 필드값 사이 빈칸(" ") 필요 */
        val inblock = shcode + " " + dwmcode + " "+ date + " " + idx + " " + cnt_string

        /* TR 요청 */
        manager.requestData(m_nHandle, "t1305", inblock, false, "", 200)
    }
    private fun processT1305(strBlockName: String, pData: ByteArray) {
        if (strBlockName == "t1305OutBlock" == true) {
            val strNextKey = String(pData)
            //Toast.makeText(getApplicationContext(), strBlockName + " " + new String(pData), Toast.LENGTH_SHORT).show();
        } else if (strBlockName == "t1305OutBlock1" == true) {
            processT1305OutBlock1(pData)
        }

    }

    private fun processT1305OutBlock1(pData: ByteArray){

        // 방법 1. res 폴더에 TR정보가 담긴 *.res 파일이 있는 경우
        var map = manager!!.getOutBlockDataFromByte("t1305", "t1305OutBlock1", pData!!)
        var pArray = manager.getAttributeFromByte("t1305", "t1305OutBlock1", pData) // attribute


        /*
        // 방법2. 프로젝트의 TR정보가 담긴 소스(.kt, .java ... *현재 프로젝트의 TRCODE.kt )를 사용하는 경우
        val bAttributeInData = true
        var map = manager.getDataFromByte(pData, TRCODE.n1305col, bAttributeInData)
        var pArray = manager.getAttributeFromByte(pData, TRCODE.n1305col) // attribute
        */

        if( map != null) {
            for (i in 0..map.size - 1) {

                /*
                 실데이터는 getOutBlockDataFromByte 에서 불러온 정보를 이용
               대비구분을 위한 데이터 getAttributeFromByte 에서 불러온 정보를 이용
               map의 index번호는 (TR 구조체의 인덱스넘버를 직접입력 또는 TRCODE.kt에서 선언된 enum class의 ordinal 값 사용 )               
               */

                val date = map[i]?.get(TRCODE.T1305.DATE.ordinal)
                val open = manager.getCommaValue(map[i]?.get(TRCODE.T1305.OPEN.ordinal)!!)
                val open_ = pArray?.get(i)?.get(TRCODE.T1305.OPEN.ordinal)!!
                val high = manager.getCommaValue(map[i]?.get(TRCODE.T1305.HIGH.ordinal)!!)
                val high_ = pArray?.get(i)?.get(TRCODE.T1305.HIGH.ordinal)!!
                val low = manager.getCommaValue(map[i]?.get(TRCODE.T1305.LOW.ordinal)!!)
                val low_ = pArray?.get(i)?.get(TRCODE.T1305.LOW.ordinal)!!
                val close = manager.getCommaValue(map[i]?.get(TRCODE.T1305.CLOSE.ordinal)!!)
                val close_ = pArray?.get(i)?.get(TRCODE.T1305.CLOSE.ordinal)!!

                val sign = map[i]?.get(TRCODE.T1305.SIGN.ordinal)
                val change = map[i]?.get(TRCODE.T1305.CHANGE.ordinal)

                val chdegree = map[i]?.get(TRCODE.T1305.CHDEGREE.ordinal)
                val chdegree_ = pArray[i]?.get(TRCODE.T1305.CHDEGREE.ordinal)


                val data_record: List<Triple<TableGrid.TYPE, Any?, Int>> = listOf(
                    Triple(TableGrid.TYPE.STRING,date,R.id.view1),
                    Triple(TableGrid.TYPE.STRING,open,R.id.view2),
                    Triple(TableGrid.TYPE.DAEBI,open_,R.id.view2),
                    Triple(TableGrid.TYPE.STRING,high,R.id.view3),
                    Triple(TableGrid.TYPE.DAEBI,high_,R.id.view3),
                    Triple(TableGrid.TYPE.STRING,low,R.id.view4),
                    Triple(TableGrid.TYPE.DAEBI,low_,R.id.view4),
                    Triple(TableGrid.TYPE.STRING,close,R.id.view5),
                    Triple(TableGrid.TYPE.DAEBI,close_,R.id.view5),
                    Triple(TableGrid.TYPE.DOUBLE,change,R.id.view6),
                    Triple(TableGrid.TYPE.DOUBLE,chdegree,R.id.view7),
                    Triple(TableGrid.TYPE.DAEBI,chdegree_,R.id.view7)
                )
                m_adapter.addItem(data_record)

            }
            m_adapter.notifyDataSetChanged()
        }
    }


}
