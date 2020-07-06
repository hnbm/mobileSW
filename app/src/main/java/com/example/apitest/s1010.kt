package com.example.apitest

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ebest.api.DataPacket
import com.ebest.api.MsgPacket
import com.ebest.api.RealPacket
import com.ebest.api.SocketManager
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream

class s1010 : Fragment() {

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

    // 그리드뷰, 그리드뷰어댑터, 메인뷰 선언
    lateinit var m_gridView: GridView
    internal var m_adapter = TableGrid().DataAdapter(R.layout.s1004_item02)
    lateinit var mainView: MainView

    // 리사이클러뷰, 텍스트뷰 3개 선언
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataText: TextView
    lateinit var root: View
    var jangoSet = ArrayList<Jango>()
    var cnt: Int = 0        // processT1101를 수행한 횟수. internal storage에 저장된 파일 내의 주식 종목 수만큼 수행하면 0으로 초기화됨. 즉, 본인이 갖고 있는 주식의 종목 수만큼 수행하면 0으로 초기화됨.
    var recnt: Int = 0      // processT1101를 수행한 횟수. 0으로 초기화되지 않음. 종목을 클릭하면 processT1101가 수행되어 jangoSet에 Jango가 add되는데 jangoSet에 갖고 있는 종목 수 이상으로 Jango가 추가되는 것을 방지하기 위한 Int.

    internal inner class ProcMessageHandler : Handler() {

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.d("핸들러가받은메시지", msg.what.toString() + " 그리고 " + msg.toString())
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
                    Log.d("갖고온데이터", lpDp.pData.toString())
                    if (lpDp.strTRCode == "t1101") {
                        processT1101(lpDp.pData!!)
//                        Toast.makeText(
//                            activity?.applicationContext,
//                            lpDp.strTRCode + " " + lpDp.pData.toString() + lpDp.strUserData,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }

                // REAL데이타
                RECEIVE_REALDATA -> {
                    val lpRp = msg.obj as RealPacket
                    if (lpRp.strBCCode == "H1_") {
                        processH1_(lpRp.strKeyCode, lpRp.pData)
                    }
                }


                // TR메세지
                RECEIVE_MSG -> {
                    val lpMp = msg.obj as MsgPacket
//                    Toast.makeText(
//                        activity?.applicationContext,
//                        lpMp.strTRCode + " " + lpMp.strMsgCode + lpMp.strMessageData,
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
                // 접속종료 또는 재연결
                mainView.RECEIVE_DISCONNECT ,  mainView.RECEIVE_RECONNECT->{
                    mainView.onMessage(msg);
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_s1010, container, false)
        mainView = (activity as MainView)                                           // 메인뷰
        handler = ProcMessageHandler()                                              // 메세지 핸들러
        manager = (activity?.application as ApplicationManager).getSockInstance()   // 소켓 인스턴스

        // 조회 버튼 클릭하면 requestT1101() 수행
        root.findViewById<Button>(R.id.btn_jango).setOnClickListener {
            cnt = 0
            recnt = 0
            requestT1101()
        }

        // 그리드뷰 find, 그리드뷰 어댑터에 어댑터 부착
        m_gridView = root.findViewById<View>(R.id.grid_view2) as GridView
        m_gridView.adapter = m_adapter

        // 텍스트뷰 find
//        dataText = root!!.findViewById(R.id.getDateText)

//        Log.d("잔고셋", jangoSet[0].jongmok)
//        Log.d("잔고셋", jangoSet[1].jongmok)
//        recyclerView = root!!.findViewById(R.id.recycler_view) as RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        recyclerView.adapter = MyAdapter(jangoSet)
//        recyclerView.setHasFixedSize(true)

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

    // 조회 버튼 클릭 시 수행하는 함수
    // requestData() 수행해서 processT1101() 수행하게 함. internal storage에서 파일 가져와서 internalTxtArray에 JSONArray로 변환.
    // 리사이클러뷰 내용 채워야 함.
    private fun requestT1101() {
        jangoSet.clear()
        val path = context!!.filesDir
        val letDirectory = File(path, "LET")
        letDirectory.mkdirs()
        val file = File(letDirectory, "internal9.txt")
        if (!file.exists()) {
            Toast.makeText(activity?.applicationContext, "보유 자산이 없습니다", Toast.LENGTH_SHORT).show()
        } else {
            var internalTxt = FileInputStream(file).bufferedReader().use {
                it.readText()
            }
            var internalTxtArray: JSONArray = JSONArray(internalTxt)

            for (i in 0 until internalTxtArray.length()) {
                var bOK = manager.deleteRealData(m_nHandle, "H1_", "", 6)

                // interanl8.txt의 "jongmok"
                var jongmokCode: String = internalTxtArray.getJSONObject(i).getString("jongmok")     // 종목 코드
                Log.d("종목코드", jongmokCode)

                var aa = manager.requestData(m_nHandle, "t1101", jongmokCode, false, "", 30)
//                dataText.text = aa.toString()
            }
        }
    }

    private fun processT1101(pData: ByteArray) {
        /* 방법 1. res 폴더에 TR정보가 담긴 *.res 파일이 있는 경우
         var map = manager!!.getOutBlockDataFromByte("t1101", "t1101OutBlock", pData!!)
        var pArray = manager.getAttributeFromByte("t1101", "t1101OutBlock", pData) // attribute
         */

        /* 방법2. 프로젝트의 TR정보가 담긴 소스(.kt, .java ... *현재 프로젝트의 TRCODE.kt )를 사용하는 경우  */
        val bAttributeInData = true
        var map = manager.getDataFromByte(pData, TRCODE.n1101col, bAttributeInData)
        Log.d("10map값", map!![0]!![0].toString())
        var pArray = manager.getAttributeFromByte(pData, TRCODE.n1101col) // attribute
//        dataText.text = map.toString()

        // s1004에서 갖고 온 코드에서 현재가만 추출해 저장하는 전역변수 : 현재가
        var hyunjae: String = ""


        /* 데이터 처리 */
        m_adapter.resetItems()
        m_adapter.notifyDataSetChanged()

        if (map != null) {
            for (i in 0..map.size - 1) {
                /*
                실데이터는 getOutBlockDataFromByte 에서 불러온 정보를 이용
                대비구분을 위한 데이터 getAttributeFromByte 에서 불러온 정보를 이용
                map의 index번호는 (TR 구조체의 인덱스넘버를 직접입력 또는 TRCODE.kt에서 선언된 enum class의 ordinal 값 사용 )
                */
                val arrayData: ArrayList<String> = ArrayList()
                TRCODE.T1101.values().forEach {
                    val temp = map[i]?.get(it.ordinal).toString()
                    arrayData.add(temp)
                }

                val price = manager.getCommaValue(arrayData.get(TRCODE.T1101.PRICE.ordinal))

                var _priceidx = 0
                val templist: ArrayList<Pair<String, String>> = ArrayList()
                for (idx in 0..9) {
                    //매수호가
                    val sub_bidho = (7 + 1) + ((9 - idx) * 6)
                    val sub_bidho_data = manager.getCommaValue(arrayData.get(sub_bidho))

                    //매수호가 수량
                    val sub_bidrem = (7 + 3) + ((9 - idx) * 6)
                    val sub_bidrem_data = manager.getCommaValue(arrayData.get(sub_bidrem))

                    val _pair: Pair<String, String> = Pair(sub_bidho_data, sub_bidrem_data)
                    templist.add(_pair)
                    if (idx == 9) {
                        Log.d("매수호가", sub_bidho_data)
                        Log.d("매수호가 수량", sub_bidrem_data)
                    }
                    if (idx == 9) {
                        hyunjae = arrayData.get(sub_bidho)
                    }
                }

                for (idx in 0..9) {
                    //매도호가
                    val sub_offerho = (7 + 0) + (idx * 6)
                    val sub_offerho_data = manager.getCommaValue(arrayData.get(sub_offerho))

                    //매도호가 수량
                    val sub_offerrem = (7 + 2) + (idx * 6)
                    val sub_offerrem_data = manager.getCommaValue(arrayData.get(sub_offerrem))

                    val _pair: Pair<String, String> = Pair(sub_offerho_data, sub_offerrem_data)
                    templist.add(_pair)
                    if (idx == 0) {
                        Log.d("매도호가", sub_offerho_data)
                        Log.d("매도호가 수량", sub_offerrem_data)
                    }
                }
                templist.reverse()
                _priceidx = templist.size - _priceidx

                for (idx in 0..templist.size - 1) {

                    val style = Bundle()
                    style.putInt(TableGrid.GRAVITY, Gravity.CENTER)

                    val _value = templist.get(idx).first

                    var amount_l = templist.get(idx).second
                    var amount_r = amount_l
                    if ((idx - 9) <= 0) {
                        style.putInt(TableGrid.BACKGROUND, Color.rgb(230, 241, 255))
                        amount_r = "" //String.format("매도호가 %d",(idx+1))
                    } else {
                        style.putInt(TableGrid.BACKGROUND, Color.rgb(252, 232, 232))
                        amount_l = "" //String.format("매수호가 %d",(idx-9))
                    }

                    val data_record: List<Triple<TableGrid.TYPE, Any?, Int>> = listOf(
                        Triple(TableGrid.TYPE.STRING, amount_l, R.id.txt_left),
                        Triple(TableGrid.TYPE.STRING, _value, R.id.txt_center),
                        Triple(TableGrid.TYPE.STRING, amount_r, R.id.txt_right)
                    )

                    m_adapter.addItem(data_record)


                    if (price.equals(_value)) {
                        style.putInt(TableGrid.BACKGROUND, Color.rgb(255, 235, 60))
                    }

                    m_adapter.setCellStyle(idx, 0, null)
                    m_adapter.setCellStyle(idx, 1, style)
                    m_adapter.setCellStyle(idx, 2, null)

                }
                m_adapter.notifyDataSetChanged()


                val shcode = arrayData.get(TRCODE.T1101.SHCODE.ordinal)
                var bOK = manager.addRealData(m_nHandle, "H1_", shcode, 6)
                val name = arrayData.get(TRCODE.T1101.HNAME.ordinal)
            }
        }

        val path = context!!.filesDir
        val letDirectory = File(path, "LET")
        letDirectory.mkdirs()
        val file = File(letDirectory, "internal9.txt")
        var internalTxt = FileInputStream(file).bufferedReader().use {
            it.readText()
        }
        var internalTxtArray: JSONArray = JSONArray(internalTxt)

        var jongmokCode: String = internalTxtArray.getJSONObject(cnt).getString("jongmok")     // 종목 코드

        var jongmokName: String = internalTxtArray.getJSONObject(cnt).getString("name").trim()     // 종목명
//        Log.d("종목이름", jongmokName)
        var boyou: String = internalTxtArray.getJSONObject(cnt).getString("qty") + "주"         // 보유 수량
//        Log.d("보유수량", boyou)
        var maeipga: String = internalTxtArray.getJSONObject(cnt).getString("danga").toInt().toString()         // 매입가
//        Log.d("매입가", maeipga)
        var hyunjaega: String = hyunjae.toInt().toString()                                   // 현재가
//        Log.d("현재가", hyunjaega)
        var maeip_amount: String = (internalTxtArray.getJSONObject(cnt).getString("qty").toInt() * maeipga.toInt()).toString() // 매입금액
//        Log.d("매입금액", maeip_amount)
        var pyeongga_amount: String = (internalTxtArray.getJSONObject(cnt).getString("qty").toInt() * hyunjaega.toInt()).toString()    // 평가금액
//        Log.d("평가금액", pyeongga_amount)
        var sonik: String = (pyeongga_amount.toInt() - maeip_amount.toInt()).toString() // 손익
//        Log.d("손익", sonik)
        var suik_ryul = String.format("%.2f", (sonik.toDouble() / maeip_amount.toDouble()) * 100) + "%"  // 수익률
//        Log.d("수익률", suik_ryul)

        if (recnt < internalTxtArray.length()) {
            jangoSet.add(
                Jango(
                    jongmokCode,
                    jongmokName,
                    maeipga,
                    sonik,
                    maeip_amount,
                    boyou,
                    suik_ryul,
                    hyunjaega,
                    pyeongga_amount
                )
            )
        }
        recnt++
        cnt++

        // 리사이클러뷰 find, 리사이클러뷰 LayoutManager, adapter 부착, FixSize
        if (cnt == internalTxtArray.length()) {
            val adapter = MyAdapter(jangoSet)
            recyclerView = root!!.findViewById(R.id.recycler_view) as RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            // 특정 주식을 클릭하면 그 주식의 호가창을 보여줌
            adapter.setItemClickListener(object: MyAdapter.ItemClickListener{
                override fun onClick(view: View, position: Int) {
                    Log.d("SSS", "${position}번 리스트 선택")
                    Log.d("AAA", adapter.getJongmokCode())
//                    Toast.makeText(
//                            activity?.applicationContext,
//                            "${position}번 리스트 선택",
//                            Toast.LENGTH_SHORT
//                        ).show()
                    manager.requestData(m_nHandle, "t1101", adapter.getJongmokCode(), false, "", 30)
                }
            })
            cnt = 0
        }
    }

    private fun processH1_(strKeyCode: String?, pData: ByteArray?) {
        /*
        방법 1. res 폴더에 TR정보가 담긴 *.res 파일이 있는 경우
        var strArray = manager!!.getOutBlockDataFromByte("H1_", "OutBlock", pData!!)
        var pArray = manager.getAttributeFromByte("H1_", "OutBlock", pData) // attribute
         */


        /* 방법2. 프로젝트의 TR정보가 담긴 소스(.kt, .java ... *현재 프로젝트의 TRCODE.kt )를 사용하는 경우  */
        val bAttributeInData = true
        var strArray = manager.getDataFromByte(pData!!, TRCODE.nH1_col, bAttributeInData)
        var pArray = manager.getAttributeFromByte(pData!!, TRCODE.nH1_col) // attribute


        /* 데이터 처리 */
        if (strArray != null) {
            for (i in 0..strArray.size - 1) {

                val arrayData: ArrayList<String> = ArrayList()
                TRCODE.H1_.values().forEach {
                    val temp = strArray[i]?.get(it.ordinal).toString()
                    arrayData.add(temp)
                }


            }
        }

    }

}