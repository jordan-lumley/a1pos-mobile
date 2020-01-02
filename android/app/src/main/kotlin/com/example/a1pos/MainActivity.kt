package com.example.a1pos

import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.NonNull;
import com.google.gson.Gson
import com.pax.poslink.*
import com.pax.poslink.peripheries.MiscSettings
import com.pax.poslink.peripheries.POSLinkPrinter
import com.pax.poslink.peripheries.ProcessResult
import com.pax.poslink.poslink.POSLinkCreator
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import org.json.JSONObject
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.ArrayList

class MainActivity: FlutterActivity() {
    private val ECRREFNUM = "1"
    private val RUNNING_POSLINKS = ArrayList<PosLink>()
    private var SETTINGINIFILE: String? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)

    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        init()

        val CHANNEL = "flutter.a1pos.com.channel"
        val methodChannel = MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger, CHANNEL)
        methodChannel.setMethodCallHandler { methodCall, result ->
            val size = RUNNING_POSLINKS.size
            for (i in 0 until size) {
                val currentPosLink = RUNNING_POSLINKS[i]
                try {
                    currentPosLink.CancelTrans()
                    RUNNING_POSLINKS.remove(currentPosLink)
                } catch (e: Exception) {
                    val blah = e
                }

            }

            when (methodCall.method.toUpperCase()) {
                "SALE" -> {
                    var saleAmount = ""
                    if (methodCall.arguments != null) {
                        saleAmount = methodCall.arguments.toString()
                        if (saleAmount.contains(",")) {
                            saleAmount = saleAmount.replace(",", "")
                        }
                    }

                    val processTask = processTransactionAsync(saleAmount, result)
                    Thread(processTask).start()
                }
                "REFUND" -> {
                    var refundAmount = ""
                    if (methodCall.arguments != null) {
                        refundAmount = methodCall.arguments.toString()
                        if (refundAmount.contains(",")) {
                            refundAmount = refundAmount.replace(",", "")
                        }
                    }

                    val refundTask = refundTransactionAsync(refundAmount, result)
                    Thread(refundTask).start()
                }
                "TRANSACTIONS_DETAILS" -> if (methodCall.arguments != null) {
                    val argsArr = methodCall.arguments<ArrayList<*>>()
                    val pageIndex = Integer.parseInt(argsArr[0].toString())
                    val pageSize = Integer.parseInt(argsArr[1].toString())

                    val transactionsTask = getTransactionsDetailsAsync(pageIndex, pageSize, result)

                    Thread(transactionsTask).start()
                }
                "TRANSACTIONS_SUMMARY" -> {
                    val transactionsTask = getTransactionsSummaryAsync(result)
                    Thread(transactionsTask).start()
                }
                "BATCHCLOSE" -> {
                    val closeBatchTask = closeBatchAsync(result)
                    Thread(closeBatchTask).start()
                }
                "GET_COMM_SETTINGS" -> {
                    val getSettingsTask = getCommSettingsAsync(result)
                    Thread(getSettingsTask).start()
                }
                "GET_SYSTEM_SETTINGS" -> {
                    val getSystemSettingsTask = getSystemSettings(result)
                    Thread(getSystemSettingsTask).start()
                }
                "SET_NAV_SETTINGS" -> if (methodCall.arguments != null) {
                    val navBarStatus = methodCall.arguments.toString()
                    val navSettingsTask = setNavBarSettingsAsync(navBarStatus, result)
                    Thread(navSettingsTask).start()
                }
                "SAVE_COMM_SETTINGS" -> if (methodCall.arguments != null) {
                    val commSettingsObject = methodCall.arguments.toString()
                    val saveSettingsTask = saveCommSettingsAsync(commSettingsObject, result)
                    Thread(saveSettingsTask).start()
                }
                "ADJUST" -> {
                    var editAmount = ""
                    var transId = ""
                    if (methodCall.arguments != null) {
                        val argsArr = methodCall.arguments<ArrayList<*>>()
                        editAmount = argsArr[0].toString()
                        transId = argsArr[1].toString()
                        if (editAmount.contains(",")) {
                            editAmount = editAmount.replace(",", "")
                        }
                    }

                    val editTask = editTransactionAsync(editAmount, transId, result)
                    Thread(editTask).start()
                }
                "TEST_PRINT" -> testPrint()
            }
        }
    }

    private fun init() {
        SETTINGINIFILE = applicationContext.filesDir.absolutePath + "/" + Settings.FILENAME

        val commSetting = Settings.getCommSettingFromFile(SETTINGINIFILE.toString())

        commSetting.setType(CommSetting.AIDL)
        commSetting.setTimeOut("60000")
        //        commSetting.setSerialPort("COM1");
        //        commSetting.setBaudRate("9600");
        //        commSetting.setDestIP("172.16.20.15");
        //        commSetting.setDestPort("10009");
        //        commSetting.setMacAddr("");
        //        commSetting.setEnableProxy(false);

        Settings.saveCommSettingToFile(SETTINGINIFILE.toString(), commSetting)

        LogSetting.setLogMode(true)
    }

    private fun GetPosLink(): PosLink {
        val posLink = POSLinkCreator.createPoslink(applicationContext)
        posLink.appDataFolder = applicationContext.filesDir.absolutePath

        val cSet = Settings.getCommSettingFromFile(SETTINGINIFILE.toString())
        posLink.SetCommSetting(cSet)

        return posLink
    }

    private fun setNavBarSettingsAsync(status: String, RESULT: MethodChannel.Result): Runnable {
        return r{
            try {
                MiscSettings.setNavigationBarEnable(applicationContext, java.lang.Boolean.parseBoolean(status))

                runOnUiThread {
                    val GSON = Gson()
                    RESULT.success(GSON.toJson(ChannelReturnResponse("OK", "SUCCESS")))
                }
            } catch (e: Exception) {
                errorOnUiThread("FAILED TO LOAD SETTINGS FILE", RESULT)
            }
        }
    }

    private fun getSystemSettings(RESULT: MethodChannel.Result): Runnable {
        return r{
            try {
                val isNavigationBarEnabled = MiscSettings.isNavigationBarEnable(applicationContext)
                //                boolean isHomeKeyEnabled = MiscSettings.isHomeKeyEnable(getApplicationContext());
                //                boolean isRecentKeyEnabled = MiscSettings.isRecentKeyEnable(getApplicationContext());


                val cSetAsJson = JSONObject()
                cSetAsJson.put("isNavigationBarEnabled", isNavigationBarEnabled)


                runOnUiThread {
                    val GSON = Gson()
                    RESULT.success(GSON.toJson(ChannelReturnResponse("OK", cSetAsJson.toString())))
                }
            } catch (e: Exception) {
                errorOnUiThread("FAILED TO LOAD SETTINGS FILE", RESULT)
            }
        }
    }

    private fun getCommSettingsAsync(RESULT: MethodChannel.Result): Runnable {
        return r{
            try {
                val settingIniFile = applicationContext.filesDir.absolutePath + "/" + Settings.FILENAME

                val cSet = Settings.getCommSettingFromFile(settingIniFile)

                val cSetAsJson = JSONObject()
                cSetAsJson.put("timeOut", cSet.getTimeOut())
                cSetAsJson.put("commType", cSet.getType())
                //                cSetAsJson.put("serialPort", cSet.getSerialPort());
                //                cSetAsJson.put("baudRate", cSet.getBaudRate());
                //                cSetAsJson.put("ipAddr", cSet.getDestIP());
                //                cSetAsJson.put("port", cSet.getDestPort());
                //                cSetAsJson.put("macAddr", cSet.getMacAddr());

                runOnUiThread {
                    val GSON = Gson()
                    RESULT.success(GSON.toJson(ChannelReturnResponse("OK", cSetAsJson.toString())))
                }
            } catch (e: Exception) {
                errorOnUiThread("FAILED TO LOAD SETTINGS FILE", RESULT)
            }
        }
    }

    private fun saveCommSettingsAsync(commSettingsObject: String, RESULT: MethodChannel.Result): Runnable {
        return r{
            try {
                val jsonObject = JSONObject(commSettingsObject)
                //                String commType = jsonObject.getString("commType");
                val timeOut = jsonObject.getString("timeOut")
                //                String serialPort = jsonObject.getString("serialPort");
                //                String baudRate = jsonObject.getString("baudRate");
                //                String ipAddr = jsonObject.getString("ipAddr");
                //                String port = jsonObject.getString("port");
                //                String macAddr = jsonObject.getString("macAddr");

                val cSet = CommSetting()
                //                cSet.setType(commType);
                cSet.timeOut = timeOut
                //                cSet.setSerialPort(serialPort);
                //                cSet.setBaudRate(baudRate);
                //                cSet.setDestIP(ipAddr);
                //                cSet.setDestPort(port);
                //                cSet.setMacAddr(macAddr);

                Settings.saveCommSettingToFile(SETTINGINIFILE.toString(), cSet)

                runOnUiThread {
                    val GSON = Gson()
                    RESULT.success(GSON.toJson(ChannelReturnResponse("OK", GSON.toJson(cSet))))
                }
            } catch (e: Exception) {
                errorOnUiThread("FAILED TO LOAD SETTINGS FILE", RESULT)
            }
        }
    }

    private fun processTransactionAsync(amount: String, RESULT: MethodChannel.Result): Runnable {
        return r{
            try {
                val posLink = GetPosLink()

                val transType = "SALE"
                val paymentRequest = PaymentRequest()
                paymentRequest.Amount = amount.replace(".", "")
                paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT")
                paymentRequest.TransType = paymentRequest.ParseTransType(transType)
                paymentRequest.ECRRefNum = ECRREFNUM
                paymentRequest.ExtData = "<TipRequest>1</TipRequest> " +
                        "<CPMode>1</CPMode>" +
                        "<ReceiptPrint>3</ReceiptPrint>" +
                        "<SignatureCapture>1</SignatureCapture>"

                posLink.PaymentRequest = paymentRequest

                val result = posLink.ProcessTrans()

                if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                    if (posLink.PaymentResponse != null && posLink.PaymentResponse.ResultTxt == "OK") {
                        val paymentReceiptData = JSONObject()
                        paymentReceiptData.put("ApprovedAmount", posLink.PaymentResponse.ApprovedAmount)
                        paymentReceiptData.put("CardType", posLink.PaymentResponse.CardType)
                        paymentReceiptData.put("TransactionType", transType)

                        printTransactionAsync()

                        runOnUiThread {
                            val GSON = Gson()
                            RESULT.success(GSON.toJson(ChannelReturnResponse("OK", "Success")))
                        }
                    } else {
                        errorOnUiThread("TRANSACTION DECLINED", RESULT)
                    }
                } else {
                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                errorOnUiThread("TRANSACTION DECLINED", RESULT)
            }
        }
    }

    private fun refundTransactionAsync(amount: String, RESULT: MethodChannel.Result): Runnable {
        return r{
            try {
                val posLink = GetPosLink()

                val transType = "RETURN"
                val paymentRequest = PaymentRequest()
                val amountFmt = amount.replace(".", "")
                paymentRequest.Amount = amountFmt
                paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT")
                paymentRequest.TransType = paymentRequest.ParseTransType(transType)
                paymentRequest.ECRRefNum = ECRREFNUM
                paymentRequest.ExtData = "<CPMode>1</CPMode>"

                posLink.PaymentRequest = paymentRequest

                val result = posLink.ProcessTrans()

                if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                    if (posLink.PaymentResponse != null && posLink.PaymentResponse.ResultTxt == "OK") {
                        val paymentReceiptData = JSONObject()
                        paymentReceiptData.put("ApprovedAmount", posLink.PaymentResponse.ApprovedAmount)
                        paymentReceiptData.put("CardType", posLink.PaymentResponse.CardType)
                        paymentReceiptData.put("TransactionType", transType)

                        printTransactionAsync()

                        runOnUiThread {
                            val GSON = Gson()
                            RESULT.success(GSON.toJson(ChannelReturnResponse("OK", "Success")))
                        }
                    } else {
                        errorOnUiThread("TRANSACTION DECLINED", RESULT)
                    }
                } else {
                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                errorOnUiThread("TRANSACTION DECLINED", RESULT)
            }
        }
    }

    private fun getTransactionsSummaryAsync(RESULT: MethodChannel.Result): Runnable {
        return r{
            try {
                val posLink = GetPosLink()

                RUNNING_POSLINKS.add(posLink)

                val transType = "LOCALTOTALREPORT"
                val reportRequest = ReportRequest()
                reportRequest.ECRRefNum = ECRREFNUM
                reportRequest.TransType = reportRequest.ParseTransType(transType)
                reportRequest.EDCType = reportRequest.ParseEDCType("ALL")

                posLink.ReportRequest = reportRequest

                val result = posLink.ProcessTrans()

                if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                    runOnUiThread {
                        val GSON = Gson()
                        val returnMsg = GSON.toJson(posLink.ReportResponse)
                        RESULT.success(GSON.toJson(ChannelReturnResponse("OK", returnMsg)))
                    }
                    //                    printTransaction("", transType);
                } else {
                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                val blah = e
                errorOnUiThread("TRANSACTION DECLINED", RESULT)
            }
        }
    }

    private fun getTransactionsDetailsAsync(pageIndex: Int, pageSize: Int, RESULT: MethodChannel.Result): Runnable {
        return r{
            try {
                val posLink = GetPosLink()

                RUNNING_POSLINKS.add(posLink)

                val transType = "LOCALDETAILREPORT"
                val reportRequest = ReportRequest()
                reportRequest.ECRRefNum = ECRREFNUM
                reportRequest.TransType = reportRequest.ParseTransType(transType)
                reportRequest.EDCType = reportRequest.ParseEDCType("ALL")

                posLink.ReportRequest = reportRequest

                val result = posLink.ProcessTrans()

                if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                    val transList = ArrayList<ReportResponse>()

                    val totalTransactions = Integer.parseInt(if (posLink.ReportResponse.TotalRecord === "") "0" else posLink.ReportResponse.TotalRecord)
                    if (totalTransactions > 0) {
                        var i: Int
                        var nextPageSize = pageSize

                        if (pageIndex > 1) {
                            i = (pageIndex - 1) * pageSize
                            nextPageSize = pageSize * pageIndex
                        } else {
                            i = 0
                        }

                        while (i < totalTransactions && i < nextPageSize) {
                            reportRequest.RecordNum = i.toString()
                            val resultPer = posLink.ProcessTrans()
                            if (resultPer.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                                transList.add(posLink.ReportResponse)
                            }
                            i++
                        }
                    }

                    runOnUiThread {
                        val GSON = Gson()
                        val returnMsg = GSON.toJson(transList)
                        RESULT.success(GSON.toJson(ChannelReturnResponse("OK", returnMsg)))
                    }
                    //                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_LONG).show());
                    //                    //                    printTransaction("", transType);
                } else {
                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                val blah = e
                errorOnUiThread("TRANSACTION DECLINED", RESULT)
            }
        }
    }

    private fun closeBatchAsync(RESULT: MethodChannel.Result): Runnable {
        return r{
            try {
                val posLink = GetPosLink()

                val transType = "BATCHCLOSE"
                val batchRequest = BatchRequest()
                batchRequest.TransType = batchRequest.ParseTransType(transType)
                batchRequest.EDCType = batchRequest.ParseEDCType("ALL")

                posLink.BatchRequest = batchRequest

                val result = posLink.ProcessTrans()

                if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                    printBatchAsync(posLink.BatchResponse)

                    runOnUiThread {
                        val GSON = Gson()
                        val returnMsg = GSON.toJson(posLink.BatchResponse)
                        RESULT.success(GSON.toJson(ChannelReturnResponse("OK", returnMsg)))
                    }
                } else {
                    errorOnUiThread("FAILED TO CLOSE BATCH", RESULT)
                }
            } catch (e: Exception) {
                val blah = e
                errorOnUiThread("FAILED TO CLOSE BATCH", RESULT)
            }
        }
    }

    private fun printBatchAsync(batchResponse: BatchResponse) {
        try {
            val posLink = GetPosLink()

            val transType = "PRINTER"
            val manageRequest = ManageRequest()
            manageRequest.TransType = manageRequest.ParseTransType(transType)

            val fmtr = POSLinkPrinter.PrintDataFormatter()

            val l = ArrayList<String>()
            l.add(batchResponse.CashAmount)
            l.add(batchResponse.CreditAmount)
            l.add(batchResponse.DebitAmount)
            l.add(batchResponse.CHECKAmount)
            l.add(batchResponse.EBTAmount)
            l.add(batchResponse.GiftAmount)

            val totalAmount = arraySumToCurrency(l)

            val c = ArrayList<String>()
            c.add(batchResponse.CashCount)
            c.add(batchResponse.CreditCount)
            c.add(batchResponse.DebitCount)
            c.add(batchResponse.CHECKCount)
            c.add(batchResponse.EBTCount)
            c.add(batchResponse.GiftCount)

            val totalCount = arraySum(c)

            fmtr.addHeader()
                    .addCenterAlign()
                    .addBigFont().addContent("A1POS")
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign()
                    .addContent("Terminal:")
                    .addRightAlign()
                    .addSN()
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign()
                    .addDate()
                    .addRightAlign()
                    .addTime()
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign()
                    .addContent("BATCH SETTLE")
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign().addContent("Credit Count: ").addRightAlign().addContent(if (batchResponse.CreditCount.isEmpty()) "0" else batchResponse.CreditCount)
                    .addLineSeparator()
                    .addLeftAlign().addContent("Cash Count: ").addRightAlign().addContent(if (batchResponse.CashCount.isEmpty()) "0" else batchResponse.CashCount)
                    .addLineSeparator()
                    .addLeftAlign().addContent("Debit Count: ").addRightAlign().addContent(if (batchResponse.DebitCount.isEmpty()) "0" else batchResponse.DebitCount)
                    .addLineSeparator()
                    .addLeftAlign().addContent("Gift Count: ").addRightAlign().addContent(if (batchResponse.GiftCount.isEmpty()) "0" else batchResponse.GiftCount)
                    .addLineSeparator()
                    .addLeftAlign().addContent("EBT Count: ").addRightAlign().addContent(if (batchResponse.EBTCount.isEmpty()) "0" else batchResponse.EBTCount)
                    .addLineSeparator()
                    .addLeftAlign().addContent("CHECK Count: ").addRightAlign().addContent(if (batchResponse.CHECKCount.isEmpty()) "0" else batchResponse.CHECKCount)
                    .addLineSeparator()
                    .addLeftAlign().addContent("TOTAL Count: ").addRightAlign().addContent(totalCount)
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign().addContent("Cash Amount: ").addRightAlign().addContent(formatCurrency(batchResponse.CashAmount))
                    .addLineSeparator()
                    .addLeftAlign().addContent("Credit Amount: ").addRightAlign().addContent(formatCurrency(batchResponse.CreditAmount))
                    .addLineSeparator()
                    .addLeftAlign().addContent("Debit Amount: ").addRightAlign().addContent(formatCurrency(batchResponse.DebitAmount))
                    .addLineSeparator()
                    .addLeftAlign().addContent("Gift Amount: ").addRightAlign().addContent(formatCurrency(batchResponse.GiftAmount))
                    .addLineSeparator()
                    .addLeftAlign().addContent("EBT Amount: ").addRightAlign().addContent(formatCurrency(batchResponse.EBTAmount))
                    .addLineSeparator()
                    .addLeftAlign().addContent("CHECK Amount: ").addRightAlign().addContent(formatCurrency(batchResponse.CHECKAmount))
                    .addLineSeparator()
                    .addLeftAlign().addContent("TOTAL Amount: ").addRightAlign().addContent(totalAmount)
                    .addHeader()
                    .addLineSeparator()
                    .addLineSeparator()

            if (!batchResponse.HostTraceNum.isEmpty()) {
                fmtr.addLineSeparator().addLineSeparator().addLeftAlign().addContent("HostTraceNum: ").addContent(batchResponse.HostTraceNum)
            }

            manageRequest.PrintData = fmtr.build()

            posLink.ManageRequest = manageRequest

            val result = posLink.ProcessTrans()

            if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                if (posLink.ManageResponse == null) {
                    throw Error("FAILED TO PRINT")
                }
            } else {
                throw Error("FAILED TO PRINT")
            }
        } catch (e: Exception) {
            throw Error("FAILED TO PRINT")
        }

    }

    private fun printTransactionAsync() {
        try {
            val posLink = GetPosLink()

            val transType = "REPRINT"
            val manageRequest = ManageRequest()
            manageRequest.TransType = manageRequest.ParseTransType(transType)
            manageRequest.ECRRefNum = ECRREFNUM
            manageRequest.LastReceipt = "1"

            posLink.ManageRequest = manageRequest

            val result = posLink.ProcessTrans()

            if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                if (posLink.ManageResponse == null) {
                    throw Error("FAILED TO PRINT")
                }
            } else {
                throw Error("FAILED TO PRINT")
            }
        } catch (e: Exception) {
            throw Error("FAILED TO PRINT")
        }

    }

    private fun editTransactionAsync(amount: String, transId: String, RESULT: MethodChannel.Result): Runnable {
        return r{

            try {
                val posLink = GetPosLink()

                val transType = "ADJUST"
                val paymentRequest = PaymentRequest()
                paymentRequest.TransType = paymentRequest.ParseTransType(transType)
                paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT")
                paymentRequest.ECRRefNum = ECRREFNUM
                paymentRequest.OrigRefNum = transId

                val amountFmt = amount.replace(".", "")
                paymentRequest.Amount = amountFmt

                posLink.PaymentRequest = paymentRequest

                val result = posLink.ProcessTrans()

                if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                    if (posLink.PaymentResponse != null && posLink.PaymentResponse.ResultTxt == "OK") {

                        runOnUiThread {
                            val GSON = Gson()
                            RESULT.success(GSON.toJson(ChannelReturnResponse("OK", "Success")))
                        }
                    } else {
                        errorOnUiThread("TRANSACTION DECLINED", RESULT)
                    }
                } else {
                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                errorOnUiThread("TRANSACTION DECLINED", RESULT)
            }
        }
    }

    fun testPrint() {
        val custom = "TEST PRINT"

        POSLinkPrinter.getInstance(applicationContext).print(custom, -1, object : POSLinkPrinter.PrintListener {
            override fun onSuccess() {}

            override fun onError(processResult: ProcessResult) {}
        })
    }

    fun errorOnUiThread(text: String, RESULT: MethodChannel.Result) {
        runOnUiThread {
            val GSON = Gson()
            RESULT.success(GSON.toJson(ChannelReturnResponse("ERROR", text)))
        }
    }

    class ChannelReturnResponse(internal var RETURN_CODE: String, internal var RETURN_MSG: String)

    private fun formatCurrency(amount: String): String {
        var amount = amount
        val nf = NumberFormat.getCurrencyInstance()
        if (amount.isEmpty()) {
            amount = "000"
        }
        return nf.format(BigDecimal(amount).movePointLeft(2))
    }

    private fun arraySumToCurrency(arrToSum: ArrayList<*>): String {
        var tmp = 0
        for (i in arrToSum.indices) {
            val `val` = arrToSum[i].toString()
            if (!`val`.isEmpty()) {
                val a = Integer.parseInt(`val`)
                tmp += a
            }
        }

        return formatCurrency(tmp.toString())
    }

    private fun arraySum(arrToSum: ArrayList<*>): String {
        var tmp = 0
        for (i in arrToSum.indices) {
            val `val` = arrToSum[i].toString()
            if (!`val`.isEmpty()) {
                val a = Integer.parseInt(`val`)
                tmp += a
            }
        }

        return tmp.toString()
    }

    fun r(f: () -> Unit): Runnable = Runnable { f() }

}
