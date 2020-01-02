import 'dart:convert';

import 'package:a1pos/components/appbarwidget.dart';
import 'package:a1pos/components/dialogs.dart';
import 'package:a1pos/models/Transaction.dart';
import 'package:a1pos/services/batch.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_masked_text/flutter_masked_text.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:intl/intl.dart';
import 'package:xml2json/xml2json.dart';

class SettlementScreen extends StatefulWidget {
  SettlementScreen({Key key, this.title}) : super(key: key);

  final String title;

  @override
  SettlementScreenState createState() => SettlementScreenState();
}

class SettlementScreenState extends State<SettlementScreen> {
  var PAGE_SIZE = 6;
  var PAGE_INDEX = 1;
  var fullTransactionsList = new List<Transaction>();
  var isLoading = true;
  var isLoadingMore = true;

  var controller = MoneyMaskedTextController(
      initialValue: 0.0, decimalSeparator: '.', thousandSeparator: ',');

  var xml2json = new Xml2Json();

  final batchPlatformService = new BatchPlatformService();
  final dialogs = new Dialogs();

  var totalRecords = 0;
  var transactionsTotalAmount;

  @override
  void initState() {
    super.initState();
    try {
      loadTransactionSummary();
    } catch (err) {
      Fluttertoast.showToast(
          msg: "Failed to load transactions!",
          toastLength: Toast.LENGTH_SHORT,
          gravity: ToastGravity.BOTTOM,
          backgroundColor: Colors.grey[600],
          textColor: Colors.white,
          fontSize: 16.0);
    }
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  void loadTransactionSummary() async {
    fullTransactionsList = new List<Transaction>();

    await batchPlatformService.getTransactionSummary().then((val) {
      if (val.isNotEmpty) {
        var decodedTransResp = jsonDecode(val);
        if (decodedTransResp["RETURN_CODE"] == "OK") {
          var returnMessage = jsonDecode(decodedTransResp["RETURN_MSG"]);

          transactionsTotalAmount = getTransactionsTotal(returnMessage);

          loadTransactionDetails(PAGE_INDEX, PAGE_SIZE);
        }
      } else {
        Fluttertoast.showToast(
            msg: "Failed to load transactions!",
            toastLength: Toast.LENGTH_SHORT,
            gravity: ToastGravity.BOTTOM,
            backgroundColor: Colors.grey[600],
            textColor: Colors.white,
            fontSize: 16.0);
      }
    });
  }

  void loadTransactionDetails(int pageIndex, int pageSize) async {
    var tempTransList = new List<Transaction>();

    var transactions =
        await batchPlatformService.getTransactions(pageIndex, pageSize);
    var decodedTransResp = jsonDecode(transactions);
    var returnMessage = jsonDecode(decodedTransResp["RETURN_MSG"]);

    if (List.from(returnMessage).length > 0) {
      totalRecords = int.parse(returnMessage[0]["TotalRecord"]);

      for (var item in returnMessage) {
        var transTmp = new Transaction(
            item["ApprovedAmount"],
            item["CardType"],
            item["BogusAccountNum"],
            item["PaymentType"],
            item["Timestamp"],
            item["RefNum"]);

        // var extData = item["ExtData"];

        // extData = "<xml>" + extData.toString() + "</xml>";

        // xml2json.parse(extData.toString());

        // var extDataJson = jsonDecode(xml2json.toParker());

        // var tipAmount = extDataJson["xml"]["TipAmount"];

        // if (tipAmount != "0") {
        //   transTmp.hasTip = true;
        // } else {
        //   transTmp.tipAmount = tipAmount;
        // }

        tempTransList.add(transTmp);
      }
    }

    setState(() {
      isLoading = false;
      isLoadingMore = false;
    });

    fullTransactionsList.addAll(tempTransList);
  }

  void handleBatchSettle() {
    dialogs.confirm(context, () async {
      try {
        await batchPlatformService.closeBatch().then((val) {
          var decodedJson = jsonDecode(val);
          if (decodedJson["RETURN_CODE"] == "OK") {
            Navigator.pop(context);

            PAGE_INDEX = 1;

            Fluttertoast.showToast(
                msg: "Successfully Settled!",
                toastLength: Toast.LENGTH_SHORT,
                gravity: ToastGravity.BOTTOM,
                backgroundColor: Colors.grey[600],
                textColor: Colors.white,
                fontSize: 16.0);

            setState(() {
              isLoading = true;
              totalRecords = 0;
            });

            loadTransactionSummary();
          }
        });
      } catch (ex) {
        Fluttertoast.showToast(
            msg: "Failed to settle batch! exception: ex",
            toastLength: Toast.LENGTH_SHORT,
            gravity: ToastGravity.BOTTOM,
            backgroundColor: Colors.grey[600],
            textColor: Colors.white,
            fontSize: 16.0);
      }
    }, "Settle Batch", "Are you sure you want to settle your batch?");
  }

  void handleTransactionAdjusment(String transId) {
    dialogs.transactionAdjustment(context, controller, () async {
      await batchPlatformService
          .adjustTransaction(controller.text, transId)
          .then((val) {
        var decodedJson = jsonDecode(val);
        if (decodedJson["RETURN_CODE"] == "OK") {
          Navigator.pop(context);

          Navigator.pushReplacementNamed(context, "/settlements");

          Fluttertoast.showToast(
              msg: "Successfully Adjusted!",
              toastLength: Toast.LENGTH_SHORT,
              gravity: ToastGravity.BOTTOM,
              backgroundColor: Colors.grey[600],
              textColor: Colors.white,
              fontSize: 16.0);
        }
      });
    }, "Adjust Transaction #" + transId, "Amount to adjust");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBarWidget(
        title: "SETTLEMENTS",
      ),
      body: Container(
        child: isLoading
            ? Center(
                child: CircularProgressIndicator(),
              )
            : Column(
                children: <Widget>[
                  Expanded(
                    flex: 1,
                    child: Padding(
                      padding: EdgeInsets.all(10),
                      child: Row(
                        children: <Widget>[
                          Expanded(
                            child: Text(
                              'SUMMARY',
                              style: TextStyle(
                                fontWeight: FontWeight.w900,
                                fontSize: 20,
                              ),
                            ),
                          ),
                          Expanded(
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              crossAxisAlignment: CrossAxisAlignment.end,
                              children: <Widget>[
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: <Widget>[
                                    Row(
                                      children: <Widget>[
                                        Text('Total Trans:'),
                                      ],
                                    ),
                                    Row(
                                      children: <Widget>[
                                        Text('Total Amount:'),
                                      ],
                                    ),
                                  ],
                                ),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.end,
                                  children: <Widget>[
                                    Row(
                                      children: <Widget>[
                                        Text('$totalRecords'),
                                      ],
                                    ),
                                    Row(
                                      children: <Widget>[
                                        Text('$transactionsTotalAmount'),
                                      ],
                                    ),
                                  ],
                                ),
                                // Text(
                                //     'Total Amount: $transactionsTotalAmount'),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  Expanded(
                    flex: 9,
                    child: fullTransactionsList.length > 0
                        ? ListView.builder(
                            itemCount: fullTransactionsList.length + 1,
                            itemBuilder: (BuildContext context, int index) {
                              var item;

                              if (index == (fullTransactionsList.length)) {
                                item = Padding(
                                  padding: EdgeInsets.fromLTRB(0, 0, 0, 60),
                                  child: isLoadingMore
                                      ? CupertinoActivityIndicator()
                                      : Padding(
                                          padding: EdgeInsets.all(25),
                                          child: MaterialButton(
                                            disabledColor: Colors.black12,
                                            onPressed: totalRecords > PAGE_SIZE
                                                ? () {
                                                    if ((PAGE_INDEX *
                                                            PAGE_SIZE) <
                                                        totalRecords) {
                                                      PAGE_INDEX =
                                                          PAGE_INDEX + 1;
                                                      setState(() {
                                                        isLoadingMore = true;
                                                      });

                                                      loadTransactionDetails(
                                                          PAGE_INDEX,
                                                          PAGE_SIZE);
                                                    } else {}
                                                  }
                                                : null,
                                            color: Colors.grey[100],
                                            child: Text('Load More'),
                                          ),
                                        ),
                                );
                              } else {
                                item = Container(
                                  child: getTransactionCard(
                                      fullTransactionsList[index]),
                                );
                              }

                              return item;
                            },
                          )
                        : Center(
                            child: Text('No transactions'),
                          ),
                  ),
                ],
              ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: isLoading || fullTransactionsList.length == 0
            ? null
            : handleBatchSettle,
        label: Text(
          "Settle Batch",
          style: TextStyle(
            color: Colors.white,
          ),
        ),
        backgroundColor: Colors.teal,
        icon: Icon(
          Icons.thumb_up,
          color: Colors.white,
        ),
      ),
    );
  }

  String formatCurrency(String x) {
    if (x == "0") {
      x = "000";
    }
    var formatter = new NumberFormat("#,##0.00", "en_US");

    x = x.substring(0, x.length - 2) + "." + x.substring(x.length - 2);

    var currency = formatter.format(double.parse(x)).toString();

    return '\$$currency';
  }

  String formatDate(String d) {
    d = d.substring(0, 8) + "T" + d.substring(8, d.length);
    var date = new DateFormat.Md().format(DateTime.parse(d));
    var time = new DateFormat.jm().format(DateTime.parse(d));

    var dateTime = date + " " + time;
    return dateTime;
  }

  String getTransactionsTotal(dynamic json) {
    var debitAmt = json["DebitAmount"] == "" ? 0 : json["DebitAmount"];
    var creditAmt = json["CreditAmount"] == "" ? 0 : json["CreditAmount"];
    var visaAmt = json["VisaAmount"];
    var amexAmt = json["AMEXAmount"];
    var checkAmt = json["CHECKAmount"];

    var creditAmount = formatCurrency(creditAmt);
    return creditAmount;
  }

  Widget getTransactionCard(Transaction transaction) {
    return GestureDetector(
      onTap: () {
        handleTransactionAdjusment(transaction.referenceNum);
      },
      child: Card(
        elevation: 5,
        margin: EdgeInsets.all(10),
        child: Padding(
          padding: EdgeInsets.all(25),
          child: Row(
            children: <Widget>[
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Text(
                      '${transaction.paymentType}',
                      style: TextStyle(
                          fontWeight: FontWeight.w900,
                          fontSize: 18,
                          color: transaction.paymentType == "RETURN"
                              ? Colors.red
                              : Colors.black),
                    ),
                    Text('Ref: ${transaction.referenceNum}'),
                    Text(''),
                    Text(
                      '${transaction.cardType}',
                    ),
                    Text(
                      '${transaction.accountNum}'.padLeft(6, "*"),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: <Widget>[
                    Text(
                      formatDate(transaction.date),
                      style: TextStyle(
                        fontStyle: FontStyle.italic,
                      ),
                    ),
                    Text(''),
                    Text(''),
                    Text(''),
                    Text(
                      transaction.paymentType == "RETURN"
                          ? '-' + formatCurrency(transaction.amount)
                          : formatCurrency(transaction.amount),
                      style: TextStyle(
                          fontSize: 18,
                          color: transaction.paymentType == "RETURN"
                              ? Colors.red
                              : Colors.black),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
