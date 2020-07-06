package com.example.apitest

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.ebest.api.DataPacket
import com.ebest.api.MsgPacket
import com.ebest.api.RealPacket
import com.ebest.api.SocketManager

class s1003 : Fragment() {

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
                    if (lpDp.strTRCode == "t1302") {
                        processT1302(lpDp.strBlockName!!, lpDp.pData!!)
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

    internal var m_adapter = TableGrid().DataAdapter(R.layout.s1003_item01)
    lateinit var m_gridView: GridView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.activity_s1003, container, false)
        mainView = (activity as MainView)

        m_gridView = root.findViewById<View>(R.id.grid_view) as GridView
        m_gridView.adapter = m_adapter

        root.findViewById<Button>(R.id.button).setOnClickListener {
            requestT1302()
        }


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

    private fun requestT1302()
    {
        m_adapter.items.clear()
        m_adapter.notifyDataSetChanged()

        val edit = root.findViewById<EditText>(R.id.editText3)
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

        manager.requestData(m_nHandle, "t1302", shcode, false, "", 30)
    }


    private fun processT1302(strBlockName: String, pData: ByteArray) {
        if (strBlockName == "t1302OutBlock" == true) {
            val strNextKey = String(pData)
            //Toast.makeText(getApplicationContext(), strBlockName + " " + new String(pData), Toast.LENGTH_SHORT).show();
        } else if (strBlockName == "t1302OutBlock1" == true) {
            processT1302OutBlock1(pData)
        }
    }
    private fun processT1302OutBlock1(pData: ByteArray) {

        /* 방법 1. res 폴더에 TR정보가 담긴 *.res 파일이 있는 경우
        var map = manager!!.getOutBlockDataFromByte("t1302", "t1302OutBlock1", pData!!)
        var pArray = manager.getAttributeFromByte("t1302", "t1302OutBlock1", pData) // attribute
        */


        /* 방법2. 프로젝트의 TR정보가 담긴 소스(.kt, .java ... *현재 프로젝트의 TRCODE.kt )를 사용하는 경우  */
        val bAttributeInData = true
        var map = manager.getDataFromByte(pData, TRCODE.n1302col, bAttributeInData)
        var pArray = manager.getAttributeFromByte(pData, TRCODE.n1302col) // attribute


        if( map != null) {
            for (i in 0..map.size - 1) {

                val arrayData : ArrayList<String> = ArrayList()

                TRCODE.T1302.values().forEach{
                    val temp = map[i]?.get(it.ordinal).toString()
                    arrayData.add(temp)
                }

                /*
                실데이터는 getOutBlockDataFromByte 에서 불러온 정보를 이용
                대비구분을 위한 데이터 getAttributeFromByte 에서 불러온 정보를 이용
                map의 index번호는 (TR 구조체의 인덱스넘버를 직접입력 또는 TRCODE.kt에서 선언된 enum class의 ordinal 값 사용 )
                */
                val data_record: List<Triple<TableGrid.TYPE, Any?, Int>> = listOf(
                    Triple(TableGrid.TYPE.STRING,manager.getTimeFormat(arrayData.get(TRCODE.T1302.CHETIME.ordinal)),R.id.view1),
                    Triple(TableGrid.TYPE.STRING,manager.getCommaValue(arrayData.get(TRCODE.T1302.CLOSE.ordinal)),R.id.view2),
                    Triple(TableGrid.TYPE.DOUBLE,arrayData.get(TRCODE.T1302.CHANGE.ordinal),R.id.view3),
                    Triple(TableGrid.TYPE.STRING,manager.getCommaValue(arrayData.get(TRCODE.T1302.MDVOLUME.ordinal)),R.id.view4),
                    Triple(TableGrid.TYPE.STRING,manager.getCommaValue(arrayData.get(TRCODE.T1302.MSVOLUME.ordinal)),R.id.view5)
                )
                m_adapter.addItem(data_record)
            }

            m_adapter.notifyDataSetChanged()
        }
    }
}