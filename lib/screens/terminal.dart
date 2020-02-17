import 'dart:convert';

import 'package:a1pos/components/appbarwidget.dart';
import 'package:a1pos/components/dialogs.dart';
import 'package:a1pos/components/numpadbutton.dart';
import 'package:a1pos/components/tenderbutton.dart';
import 'package:a1pos/services/payment.dart';
import 'package:flutter/material.dart';
import 'package:flutter_masked_text/flutter_masked_text.dart';

const String SCREEN_NUM_PLACHOLDER = "0.00";

class TerminalScreen extends StatefulWidget {
  @override
  TerminalScreenState createState() => TerminalScreenState();
}

class TerminalScreenState extends State<TerminalScreen> {
  final paymentPlatformService = new PaymentPlatformService();
  final controller = MoneyMaskedTextController(
      initialValue: 0.0, decimalSeparator: '.', thousandSeparator: ',');
  var dialogs = new Dialogs();

  bool isTenderButtonsEnabled = false;

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  void clearText() {
    setState(() {
      controller.text = "0.00";
      isTenderButtonsEnabled = false;
    });
  }

  void handleTransaction(String type) async {
    var tmpAmount = controller.text;

    clearText();

    dialogs.loading(context);

    switch (type.toUpperCase()) {
      case "REFUND":
        await paymentPlatformService.refund(tmpAmount).then((result) {
          Navigator.pop(context);

          var respJson = jsonDecode(result);

          if (respJson["RETURN_CODE"] == "OK") {
            dialogs.success(context);
          } else {
            dialogs.failure(context, respJson["RETURN_MSG"]);
          }
        });

        break;
      case "SALE":
        await paymentPlatformService.sale(tmpAmount).then((result) {
          Navigator.pop(context);

          var respJson = jsonDecode(result);

          if (respJson["RETURN_CODE"] == "OK") {
            dialogs.success(context);
          } else {
            dialogs.failure(context, respJson["RETURN_MSG"]);
          }
        });
        break;
    }
  }

  void onKeyPress(String val) {
    switch (val.toLowerCase()) {
      case "delete":
        if (controller.text.length > 0) {
          var lastIndex = controller.text.length - 1;
          var subStr = controller.text.substring(0, lastIndex);
          controller.text = subStr;
        }
        break;
      case "clr":
        controller.text = "";
        break;
      default:
        controller.text += val;
        break;
    }

    setState(() {
      if (controller.text.isNotEmpty && controller.text != "0.00") {
        isTenderButtonsEnabled = true;
      } else {
        controller.text = "0.00";
        isTenderButtonsEnabled = false;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBarWidget(
        title: "TERMINAL",
      ),
      body: Container(
        color: Colors.white,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Expanded(
              flex: 2,
              child: Row(
                children: <Widget>[
                  Expanded(
                    flex: 8,
                    child: Container(
                      color: Colors.white,
                      child: TextField(
                        enabled: false,
                        style: TextStyle(
                          fontSize: 40,
                        ),
                        controller: controller,
                        decoration: InputDecoration(
                          hintText: "0.00",
                          icon: Icon(
                            Icons.attach_money,
                            size: 40,
                          ),
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  Expanded(
                    flex: 2,
                    child: Container(
                      color: Colors.white,
                      alignment: Alignment.center,
                      child: GestureDetector(
                        onTap: () {
                          onKeyPress('delete');
                        },
                        child: Icon(Icons.backspace, size: 35),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            Expanded(
              flex: 5,
              child: Container(
                color: Colors.white,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Expanded(
                      flex: 1,
                      child: Container(
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Expanded(
                                flex: 2, child: NumPadButton("1", onKeyPress)),
                            Expanded(
                                flex: 2, child: NumPadButton("2", onKeyPress)),
                            Expanded(
                                flex: 2, child: NumPadButton("3", onKeyPress)),
                          ],
                        ),
                      ),
                    ),
                    NumPadDivider(),
                    Expanded(
                      flex: 1,
                      child: Container(
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Expanded(
                                flex: 2, child: NumPadButton("4", onKeyPress)),
                            Expanded(
                                flex: 2, child: NumPadButton("5", onKeyPress)),
                            Expanded(
                                flex: 2, child: NumPadButton("6", onKeyPress)),
                          ],
                        ),
                      ),
                    ),
                    NumPadDivider(),
                    Expanded(
                      flex: 1,
                      child: Container(
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Expanded(
                                flex: 2, child: NumPadButton("7", onKeyPress)),
                            Expanded(
                                flex: 2, child: NumPadButton("8", onKeyPress)),
                            Expanded(
                                flex: 2, child: NumPadButton("9", onKeyPress)),
                          ],
                        ),
                      ),
                    ),
                    NumPadDivider(),
                    Expanded(
                      flex: 1,
                      child: Container(
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Expanded(
                                flex: 1,
                                child: NumPadButton("CLR", onKeyPress)),
                            Expanded(
                                flex: 1, child: NumPadButton("0", onKeyPress)),
                            Expanded(
                                flex: 1, child: NumPadButton(".", onKeyPress)),
                          ],
                        ),
                      ),
                    ),
                    Expanded(
                      flex: 1,
                      child: Container(
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            Expanded(
                              flex: 2,
                              child: TenderButton(
                                "REFUND",
                                handleTransaction,
                                Icon(
                                  Icons.arrow_upward,
                                  size: 35,
                                  color: Colors.white,
                                ),
                                isTenderButtonsEnabled,
                              ),
                            ),
                            Expanded(
                              flex: 2,
                              child: TenderButton(
                                "SALE",
                                handleTransaction,
                                Icon(
                                  Icons.credit_card,
                                  size: 35,
                                  color: Colors.white,
                                ),
                                isTenderButtonsEnabled,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

Widget NumPadDivider() {
  return Divider(
    color: Colors.grey,
    indent: 35,
    endIndent: 35,
  );
}
