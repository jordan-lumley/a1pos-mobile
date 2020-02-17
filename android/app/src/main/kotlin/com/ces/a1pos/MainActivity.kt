package com.ces.a1pos

import android.os.Bundle

import android.view.WindowManager
import androidx.annotation.NonNull;
import com.google.gson.Gson
import com.pax.poslink.*
import com.pax.poslink.formManage.ShowDialog
import com.pax.poslink.formManage.ShowDialogRequest
import com.pax.poslink.peripheries.MiscSettings
import com.pax.poslink.peripheries.POSLinkPrinter
import com.pax.poslink.peripheries.ProcessResult
import com.pax.poslink.poslink.POSLinkCreator
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import org.json.JSONObject
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import java.math.BigDecimal
import java.text.NumberFormat

import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : FlutterActivity() {
    private val ECRREFNUM = "1"
    private val RUNNING_POSLINKS = ArrayList<PosLink>()
    private var SETTINGINIFILE: String? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
                "INIT" -> {
                    if (methodCall.arguments != null) {
                        try{
                            val argsArr = methodCall.arguments<ArrayList<*>>()
                            val logDirectory = argsArr[0].toString()
                            val logFilePath = argsArr[1].toString()

                            val file = File(logFilePath)

                            Logger.LOGFILE = file
                            Logger.LOGDIRECTORY = logDirectory
                        }catch(ex: java.lang.Exception){
                            println(ex)
                        }

                        result.success("Ok")
                    }
                }
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
                "SET_TIP_SHOWN_SETTINGS" -> if (methodCall.arguments != null) {
                    val tipStatus = methodCall.arguments.toString()
                    val tipSettingsTask = setTipEnabledAsync(tipStatus, result)
                    Thread(tipSettingsTask).start()
                }
                "TEST_PRINT" -> testPrint()
            }
        }
    }

    private fun init() {
        SETTINGINIFILE = applicationContext.filesDir.absolutePath + "/" + Settings.FILENAME
        Settings.BROADPOSLOGSETTINGINIPATH = context.filesDir.path.toString() + "/logs"
        Settings.A1POSSETTINGINIFILE = context.filesDir.path.toString() + "/A1POSSystemSettings.ini"


        val commSetting = Settings.getCommSettingFromFile(SETTINGINIFILE.toString())

//        if(commSetting.type.isEmpty()){
//            commSetting.type = CommSetting.AIDL
//        }
//
//        if(commSetting.timeOut.isEmpty()){
//            commSetting.timeOut = "60000"
//        }

        //        commSetting.setSerialPort("COM1");
        //        commSetting.setBaudRate("9600");
        //        commSetting.setDestIP("172.16.20.15");
        //        commSetting.setDestPort("10009");
        //        commSetting.setMacAddr("");
        //        commSetting.setEnableProxy(false);

        Settings.saveCommSettingToFile(SETTINGINIFILE.toString(), commSetting)

        LogSetting.setOutputPath(Settings.BROADPOSLOGSETTINGINIPATH)

        LogSetting.setLevel(LogSetting.LOGLEVEL.DEBUG)
        LogSetting.setLogMode(true)

        Settings.initSystemSettingsFile()
    }

    private fun GetPosLink(): PosLink {
        val posLink = POSLinkCreator.createPoslink(applicationContext)
        posLink.appDataFolder = applicationContext.filesDir.absolutePath

        val cSet = Settings.getCommSettingFromFile(SETTINGINIFILE.toString())
        posLink.SetCommSetting(cSet)

        return posLink
    }

    private fun setNavBarSettingsAsync(status: String, RESULT: MethodChannel.Result): Runnable {
        return r {
            try {
                MiscSettings.setNavigationBarEnable(applicationContext, java.lang.Boolean.parseBoolean(status))

                runOnUiThread {
                    val GSON = Gson()
                    RESULT.success(GSON.toJson(ChannelReturnResponse("OK", "SUCCESS")))
                }
            } catch (e: Exception) {
                Logger.error("setNavBarSettingsAsync() ${e.message!!}")

                errorOnUiThread("FAILED TO LOAD SETTINGS FILE", RESULT)
            }
        }
    }

    private fun setTipEnabledAsync(status: String, RESULT: MethodChannel.Result): Runnable {
        return r {
            try {
                var a1posSetting = Settings.A1posSetting()
                a1posSetting.TipEnabled = status.toBoolean()

                Settings.writeSettingsFile(a1posSetting)

                runOnUiThread {
                    val GSON = Gson()
                    RESULT.success(GSON.toJson(ChannelReturnResponse("OK", "SUCCESS")))
                }
            } catch (e: Exception) {
                Logger.error("setTipEnabledAsync() ${e.message!!}")

                errorOnUiThread("FAILED TO LOAD SETTINGS FILE", RESULT)
            }
        }
    }


    private fun getSystemSettings(RESULT: MethodChannel.Result): Runnable {
        return r {
            try {
                val isNavigationBarEnabled = MiscSettings.isNavigationBarEnable(applicationContext)

                val a1posSetting = Settings.readSettingsFile()

                val cSetAsJson = JSONObject()
                cSetAsJson.put("isNavigationBarEnabled", isNavigationBarEnabled)
                cSetAsJson.put("tipEnabled", a1posSetting.TipEnabled)


                runOnUiThread {
                    val GSON = Gson()
                    RESULT.success(GSON.toJson(ChannelReturnResponse("OK", cSetAsJson.toString())))
                }
            } catch (e: Exception) {
                Logger.error("getSystemSettings() ${e.message!!}")

                errorOnUiThread("FAILED TO LOAD SETTINGS FILE", RESULT)
            }
        }
    }

    private fun getCommSettingsAsync(RESULT: MethodChannel.Result): Runnable {
        return r {
            try {
                val settingIniFile = applicationContext.filesDir.absolutePath + "/" + Settings.FILENAME

                val cSet = Settings.getCommSettingFromFile(settingIniFile)

                val cSetAsJson = JSONObject()
                cSetAsJson.put("timeOut", cSet.getTimeOut())
                cSetAsJson.put("commType", cSet.getType())

                runOnUiThread {
                    val GSON = Gson()
                    RESULT.success(GSON.toJson(ChannelReturnResponse("OK", cSetAsJson.toString())))
                }
            } catch (e: Exception) {
                Logger.error("getCommSettingsAsync() ${e.message!!}")

                errorOnUiThread("FAILED TO LOAD SETTINGS FILE", RESULT)
            }
        }
    }

    private fun saveCommSettingsAsync(commSettingsObject: String, RESULT: MethodChannel.Result): Runnable {
        return r {
            try {
                val jsonObject = JSONObject(commSettingsObject)
                val timeOut = jsonObject.getString("timeOut")

                val cSet = CommSetting()
                cSet.timeOut = timeOut

                Settings.saveCommSettingToFile(SETTINGINIFILE.toString(), cSet)

                runOnUiThread {
                    val GSON = Gson()
                    RESULT.success(GSON.toJson(ChannelReturnResponse("OK", GSON.toJson(cSet))))
                }
            } catch (e: Exception) {
                Logger.error("saveCommSettingsAsync() ${e.message!!}")

                errorOnUiThread("FAILED TO LOAD SETTINGS FILE", RESULT)
            }
        }
    }

    private fun processTransactionAsync(amount: String, RESULT: MethodChannel.Result): Runnable {
        return r {
            try {
                val posLink = GetPosLink()

                var tipRequestValue = "0"
                val a1posSettingTipEnabled = Settings.A1POSSETTINGS.TipEnabled
                if (a1posSettingTipEnabled!!) {
                    tipRequestValue = "1"
                }

                val transType = "SALE"
                val paymentRequest = PaymentRequest()
                paymentRequest.Amount = amount.replace(".", "")
                paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT")
                paymentRequest.TransType = paymentRequest.ParseTransType(transType)
                paymentRequest.ECRRefNum = ECRREFNUM
                paymentRequest.ExtData = "<TipRequest>$tipRequestValue</TipRequest> " +
                        "<CPMode>1</CPMode>" +
                        "<SignatureCapture>1</SignatureCapture>" +
                        "<GetSign>1</GetSign>"

                posLink.PaymentRequest = paymentRequest

                val result = posLink.ProcessTrans()

                if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                    if (posLink.PaymentResponse != null && posLink.PaymentResponse.ResultTxt == "OK") {
                        val paymentReceiptData = JSONObject()
                        paymentReceiptData.put("ApprovedAmount", posLink.PaymentResponse.ApprovedAmount)
                        paymentReceiptData.put("CardType", posLink.PaymentResponse.CardType)
                        paymentReceiptData.put("TransactionType", transType)

                        printTransactionAsync(posLink.PaymentResponse, transType)

                        runOnUiThread {
                            val GSON = Gson()
                            RESULT.success(GSON.toJson(ChannelReturnResponse("OK", "Success")))
                        }
                    } else {
                        Logger.info("processTransactionAsync() ${posLink.PaymentResponse.Message}" )
                        Logger.info("processTransactionAsync() ${posLink.PaymentResponse.HostResponse}" )

                        errorOnUiThread("TRANSACTION DECLINED", RESULT)
                    }
                } else {
                    Logger.info("processTransactionAsync() ${posLink.PaymentResponse.Message}" )
                    Logger.info("processTransactionAsync() ${posLink.PaymentResponse.HostResponse}" )

                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                Logger.error("processTransactionAsync() ${e.message!!}")

                errorOnUiThread("TRANSACTION DECLINED", RESULT)
            }
        }
    }

//    private fun getLogs(): ArrayList<String> {
//        var linesOfText = ArrayList<String>()
//
//        try {
//            val directory = File(Settings)
//            val files = directory.listFiles()
//
//
//            for (file in files) {
//                linesOfText.add("-------------------- START OF ${file.name}--------------------")
//                val myReader = Scanner(file)
//                while (myReader.hasNextLine()) {
//                    val data = myReader.nextLine()
//                    linesOfText.add(data)
//                }
//                myReader.close()
//
//                linesOfText.add("-------------------- END OF ${file.name}--------------------")
//            }
//
//            return linesOfText
//        } catch (ex: Exception) {
//            Logger.error(ex.message!!)
//        }
//
//        linesOfText.add("failed to get logs")
//
//        return linesOfText
//    }

    private fun refundTransactionAsync(amount: String, RESULT: MethodChannel.Result): Runnable {
        return r {
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

                        printTransactionAsync(posLink.PaymentResponse, transType)

                        runOnUiThread {
                            val GSON = Gson()
                            RESULT.success(GSON.toJson(ChannelReturnResponse("OK", "Success")))
                        }
                    } else {
                        Logger.info("refundTransactionAsync() ${posLink.PaymentResponse.Message}" )
                        Logger.info("refundTransactionAsync() ${posLink.PaymentResponse.HostResponse}" )

                        errorOnUiThread("TRANSACTION DECLINED", RESULT)
                    }
                } else {
                    Logger.info(posLink.PaymentResponse.Message)
                    Logger.info(posLink.PaymentResponse.HostResponse)

                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                Logger.error("refundTransactionAsync() ${e.message!!}")

                errorOnUiThread("TRANSACTION DECLINED", RESULT)
            }
        }
    }

    private fun getTransactionsSummaryAsync(RESULT: MethodChannel.Result): Runnable {
        return r {
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
                    Logger.info("getTransactionsSummaryAsync() ${posLink.ReportResponse.HostResponse}" )
                    Logger.info("getTransactionsSummaryAsync() ${posLink.ReportResponse.Message}" )

                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                Logger.error("getTransactionsSummaryAsync() ${e.message!!}")

                errorOnUiThread("TRANSACTION DECLINED", RESULT)
            }
        }
    }

    private fun getTransactionsDetailsAsync(pageIndex: Int, pageSize: Int, RESULT: MethodChannel.Result): Runnable {
        return r {
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
                } else {
                    Logger.info("getTransactionsDetailsAsync() ${posLink.ReportResponse.HostResponse}" )
                    Logger.info("getTransactionsDetailsAsync() ${posLink.ReportResponse.Message}" )

                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                Logger.error("getTransactionsDetailsAsync() ${e.message!!}")

                errorOnUiThread("TRANSACTION DECLINED", RESULT)
            }
        }
    }

    private fun closeBatchAsync(RESULT: MethodChannel.Result): Runnable {
        return r {
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
                    Logger.info("closeBatchAsync() ${posLink.BatchResponse.Message}" )
                    Logger.info("closeBatchAsync() ${posLink.BatchResponse.HostResponse}" )

                    errorOnUiThread("FAILED TO CLOSE BATCH", RESULT)
                }
            } catch (e: Exception) {
                Logger.error("closeBatchAsync() ${e.message!!}")

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
            l.add(batchResponse.CreditAmount)
            l.add(batchResponse.DebitAmount)

            val totalAmount = arraySumToCurrency(l)

            val c = ArrayList<String>()
            c.add(batchResponse.CreditCount)
            c.add(batchResponse.DebitCount)

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
                    .addLeftAlign().addContent("Debit Count: ").addRightAlign().addContent(if (batchResponse.DebitCount.isEmpty()) "0" else batchResponse.DebitCount)
                    .addLineSeparator()
                    .addLeftAlign().addContent("TOTAL Count: ").addRightAlign().addContent(totalCount)
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign().addContent("Credit Amount: ").addRightAlign().addContent(formatCurrency(batchResponse.CreditAmount))
                    .addLineSeparator()
                    .addLeftAlign().addContent("Debit Amount: ").addRightAlign().addContent(formatCurrency(batchResponse.DebitAmount))
                    .addLineSeparator()
                    .addLeftAlign().addContent("TOTAL Amount: ").addRightAlign().addContent(totalAmount)
                    .addHeader()
                    .addLineSeparator()
                    .addLineSeparator()

            if (batchResponse.HostTraceNum.isNotEmpty()) {
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
                Logger.info("printBatchAsync() ${posLink.ManageResponse.AuthorizationResult}" )
                Logger.info("printBatchAsync() ${posLink.ManageResponse.ResultTxt}" )

                throw Error("FAILED TO PRINT")
            }
        } catch (e: Exception) {
            Logger.error("printBatchAsync() ${e.message!!}")

            throw Error("FAILED TO PRINT")
        }

    }

    private fun printTransactionAsync(paymentResponse: PaymentResponse, origTransType: String, isFromDialog: Boolean= false) {
        try {
            val posLink = GetPosLink()

            val transType = "PRINTER"
            val manageRequest = ManageRequest()
            manageRequest.TransType = manageRequest.ParseTransType(transType)
            manageRequest.PrintCopy = "1"

            manageRequest.PrintData = getPrintData(paymentResponse, origTransType)

            posLink.ManageRequest = manageRequest

            val result = posLink.ProcessTrans()

            if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                if (posLink.ManageResponse == null) {
                    throw Error("FAILED TO PRINT")
                } else {
                    if(!isFromDialog){
                        val showDialogRequest = ShowDialogRequest()
                        showDialogRequest.setTitle("Print Customer Receipt?")
                        showDialogRequest.setButton1("Yes")
                        showDialogRequest.setButton2("No")

                        val result = ShowDialog.showDialog(this, showDialogRequest, posLink.GetCommSetting())
                        if (result.buttonNum == "1") {
                            printTransactionAsync(paymentResponse, origTransType, true)
                        }else{
                            return
                        }
                    }
                }
            } else {
                Logger.info("printTransactionAsync() ${posLink.ManageResponse.AuthorizationResult}" )
                Logger.info("printTransactionAsync() ${posLink.ManageResponse.ResultTxt}" )

                throw Error("FAILED TO PRINT")
            }
        } catch (e: Exception) {
            Logger.error("printTransactionAsync() ${e.message!!}")

            throw Error("FAILED TO PRINT")
        }
    }

    private fun getPrintData(paymentResponse: PaymentResponse, origTransType: String): String {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

        val fmtr = POSLinkPrinter.PrintDataFormatter()

        val extDataDoc = convertStringToXMLDocument(paymentResponse.ExtData)

        val validApprovedAmount = paymentResponse.ApprovedAmount.toBigDecimal().movePointLeft(2)
        var validTipAmount = BigDecimal.ZERO

        var tip: String
        if (extDataDoc != null) {
            tip = extDataDoc!!.getElementsByTagName("TipAmount").item(0).textContent

            validTipAmount = tip.toBigDecimal().movePointLeft(2)
        }

        val approvedAmount = currencyFormatter.format(validApprovedAmount)

        val a1posSetting = Settings.readSettingsFile()

        val validSubTotal = validApprovedAmount - validTipAmount

        val subTotal = currencyFormatter.format(validSubTotal)

        val tipAmount = currencyFormatter.format(validTipAmount)



        if(origTransType != "RETURN"){
            if (!a1posSetting.TipEnabled!!) {
                fmtr.addHeader()
                        .addLineSeparator()
                        .addLeftAlign()
                        .addDate()
                        .addRightAlign()
                        .addTime()
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent(origTransType)
                        .addLineSeparator()
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Transaction #:")
                        .addRightAlign()
                        .addContent(paymentResponse.RefNum)
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Card Type:")
                        .addRightAlign()
                        .addContent(paymentResponse.CardType)
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Account:")
                        .addRightAlign()
                        .addContent("******${paymentResponse.BogusAccountNum}")
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Amount:")
                        .addRightAlign()
                        .addContent(subTotal)
                        .addLineSeparator()
                        .addCenterAlign()
                        .addContent("------------------------------------------------")
                        .addLineSeparator()
                        .addLeftAlign()
                        .addBigFont()
                        .addContent("Total:")
                        .addRightAlign()
                        .addBigFont()
                        .addContent(approvedAmount)
                        .addLineSeparator()
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Ref. NO:")
                        .addRightAlign()
                        .addContent(paymentResponse.HostCode)
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Response:")
                        .addRightAlign()
                        .addContent(paymentResponse.Message)
                        .addLineSeparator()
                        .addLineSeparator()
                        .addContent("\\\$eSig")
                        .addLineSeparator()
                        .addLineSeparator()
                        .addContent("X_____________________________")
                        .addLineSeparator()
                        .addLineSeparator()
                        .addDisclaimer()
                        .addLineSeparator()
                        .addLineSeparator()
                        .addLineSeparator()
            } else {
                fmtr.addHeader()
                        .addLineSeparator()
                        .addLeftAlign()
                        .addDate()
                        .addRightAlign()
                        .addTime()
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent(origTransType)
                        .addLineSeparator()
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Transaction #:")
                        .addRightAlign()
                        .addContent(paymentResponse.RefNum)
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Card Type:")
                        .addRightAlign()
                        .addContent(paymentResponse.CardType)
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Account:")
                        .addRightAlign()
                        .addContent("******${paymentResponse.BogusAccountNum}")
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Amount:")
                        .addRightAlign()
                        .addContent(subTotal)
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Tip:")
                        .addRightAlign()
                        .addContent(tipAmount)
                        .addLineSeparator()
                        .addCenterAlign()
                        .addContent("------------------------------------------------")
                        .addLineSeparator()
                        .addLeftAlign()
                        .addBigFont()
                        .addContent("Total:")
                        .addRightAlign()
                        .addBigFont()
                        .addContent(approvedAmount)
                        .addLineSeparator()
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Ref. NO:")
                        .addRightAlign()
                        .addContent(paymentResponse.HostCode)
                        .addLineSeparator()
                        .addLeftAlign()
                        .addContent("Response:")
                        .addRightAlign()
                        .addContent(paymentResponse.Message)
                        .addLineSeparator()
                        .addLineSeparator()
                        .addContent("\\\$eSig")
                        .addLineSeparator()
                        .addLineSeparator()
                        .addContent("X_____________________________")
                        .addLineSeparator()
                        .addLineSeparator()
                        .addDisclaimer()
                        .addLineSeparator()
                        .addLineSeparator()
                        .addLineSeparator()
            }
        }else{
            fmtr.addHeader()
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign()
                    .addDate()
                    .addRightAlign()
                    .addTime()
                    .addLineSeparator()
                    .addLeftAlign()
                    .addContent(origTransType)
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign()
                    .addContent("Transaction #:")
                    .addRightAlign()
                    .addContent(paymentResponse.RefNum)
                    .addLineSeparator()
                    .addLeftAlign()
                    .addContent("Card Type:")
                    .addRightAlign()
                    .addContent(paymentResponse.CardType)
                    .addLineSeparator()
                    .addLeftAlign()
                    .addContent("Account:")
                    .addRightAlign()
                    .addContent("******${paymentResponse.BogusAccountNum}")
                    .addLineSeparator()
                    .addLeftAlign()
                    .addContent("Amount:")
                    .addRightAlign()
                    .addContent(subTotal)
                    .addLineSeparator()
                    .addCenterAlign()
                    .addContent("------------------------------------------------")
                    .addLineSeparator()
                    .addLeftAlign()
                    .addBigFont()
                    .addContent("Total:")
                    .addRightAlign()
                    .addBigFont()
                    .addContent(approvedAmount)
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign()
                    .addContent("Ref. NO:")
                    .addRightAlign()
                    .addContent(paymentResponse.HostCode)
                    .addLineSeparator()
                    .addLeftAlign()
                    .addContent("Response:")
                    .addRightAlign()
                    .addContent(paymentResponse.Message)
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLineSeparator()
        }


        return fmtr.build()
    }

//    private fun printTransactionAsync() {
//        try {
//            val posLink = GetPosLink()
//
//            val transType = "PRINTER"
//            val manageRequest = ManageRequest()
//            manageRequest.TransType = manageRequest.ParseTransType(transType)
//            manageRequest.PrintData = "\$eSig"
////            manageRequest.ECRRefNum = ECRREFNUM
////            manageRequest.LastReceipt = "1"
////            manageRequest.Sig
//
//            posLink.ManageRequest = manageRequest
//
//            val result = posLink.ProcessTrans()
//
//            if (result.Code == ProcessTransResult.ProcessTransResultCode.OK) {
//                if (posLink.ManageResponse == null) {
//                    throw Error("FAILED TO PRINT")
//                }
//            } else {
//                throw Error("FAILED TO PRINT")
//            }
//        } catch (e: Exception) {
//            throw Error("FAILED TO PRINT")
//        }
//    }

    private fun editTransactionAsync(amount: String, transId: String, RESULT: MethodChannel.Result): Runnable {
        return r {

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
                        Logger.info("editTransactionAsync() ${posLink.PaymentResponse.Message}" )
                        Logger.info("editTransactionAsync() ${posLink.PaymentResponse.HostResponse}" )

                        errorOnUiThread("TRANSACTION DECLINED", RESULT)
                    }
                } else {
                    Logger.info("editTransactionAsync() ${posLink.PaymentResponse.Message}" )
                    Logger.info("editTransactionAsync() ${posLink.PaymentResponse.HostResponse}" )

                    errorOnUiThread("TRANSACTION DECLINED", RESULT)
                }
            } catch (e: Exception) {
                Logger.error("editTransactionAsync() ${e.message!!}")

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
            val value = arrToSum[i].toString()
            if (value.isNotEmpty()) {
                val a = Integer.parseInt(value)
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

    private fun r(f: () -> Unit): Runnable = Runnable { f() }

    private fun convertStringToXMLDocument(xmlString: String): org.w3c.dom.Document? {
        val xmlStringWithRoot = "<root>$xmlString</root>"
        val factory = DocumentBuilderFactory.newInstance()

        var builder: DocumentBuilder?
        try {
            builder = factory.newDocumentBuilder()

            return builder!!.parse(InputSource(StringReader(xmlStringWithRoot)))
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.error(e.message!!)
        }

        return null
    }
}
