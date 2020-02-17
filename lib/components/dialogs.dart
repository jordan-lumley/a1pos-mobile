import 'package:a1pos/services/payment.dart';
import 'package:a1pos/services/print.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_masked_text/flutter_masked_text.dart';

class Dialogs {
  var printPlatformService = new PrintPlatformService();
  var paymentPlatformService = new PaymentPlatformService();

  information(BuildContext context, String title, String description) {
    return showDialog(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(title),
          content: Text(description),
          actions: <Widget>[
            GestureDetector(
              child: Text('asdf'),
              onTap: () {
                Navigator.pop(context);
              },
            )
          ],
        );
      },
    );
  }

  confirm(BuildContext context, Function f, String title, String description) {
    return showDialog(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(title),
          content: Text(description),
          actions: <Widget>[
            MaterialButton(
              child: Text(
                'Proceed',
                style: TextStyle(
                  fontSize: 18,
                  color: Colors.white,
                ),
              ),
              color: Colors.teal,
              onPressed: () {
                f();
              },
            ),
            MaterialButton(
              child: Text(
                'Cancel',
                style: TextStyle(fontSize: 18),
              ),
              onPressed: () {
                Navigator.pop(context);
              },
            )
          ],
        );
      },
    );
  }

  transactionAdjustment(
      BuildContext context,
      MoneyMaskedTextController controller,
      Function f,
      String title,
      String description) {
    return showDialog(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(title),
          content: Row(
            children: <Widget>[
              Text(description + ":"),
              Expanded(
                child: TextField(
                  keyboardType: TextInputType.number,
                  controller: controller,
                  decoration: InputDecoration(
                    contentPadding: EdgeInsets.fromLTRB(10, 0, 10, 2),
                  ),
                ),
              ),
            ],
          ),
          actions: <Widget>[
            MaterialButton(
              child: Text(
                'Proceed',
                style: TextStyle(
                  fontSize: 18,
                  color: Colors.white,
                ),
              ),
              color: Colors.teal,
              onPressed: () {
                f();
                controller.text = "0.00";
              },
            ),
            MaterialButton(
              child: Text(
                'Cancel',
                style: TextStyle(fontSize: 18),
              ),
              onPressed: () {
                controller.text = "0.00";
                Navigator.pop(context);
              },
            )
          ],
        );
      },
    );
  }

  waiting(BuildContext context, String title, String description) {
    return showDialog(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(title),
          content: Text(description),
          actions: <Widget>[
            MaterialButton(
              child: Text('Print'),
              onPressed: () async {
                Navigator.pop(context);
              },
            ),
            MaterialButton(
              child: Text('EMV'),
              onPressed: () async {
                Navigator.pop(context);
              },
            )
          ],
        );
      },
    );
  }

  loading(BuildContext context) {
    return showDialog(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Center(
            child: Text('Loading, please wait...'),
          ),
          content: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              CircularProgressIndicator(),
            ],
          ),
          actions: <Widget>[],
        );
      },
    );
  }

  success(BuildContext context) {
    return showDialog(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Center(
            child: Text(
              'Success',
              style: TextStyle(fontSize: 28),
            ),
          ),
          content: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              Icon(Icons.check, size: 60),
            ],
          ),
          actions: <Widget>[
            MaterialButton(
              child: Text(
                'Ok',
                style: TextStyle(fontSize: 20),
              ),
              onPressed: () async {
                Navigator.pop(context);
              },
            )
          ],
        );
      },
    );
  }

  failure(BuildContext context, String text) {
    return showDialog(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Center(
            child: Text(
              text,
              style: TextStyle(fontSize: 18),
            ),
          ),
          content: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              Icon(Icons.error_outline, size: 40),
            ],
          ),
          actions: <Widget>[
            MaterialButton(
              child: Text(
                'Ok',
                style: TextStyle(fontSize: 20),
              ),
              onPressed: () async {
                Navigator.pop(context);
              },
            )
          ],
        );
      },
    );
  }
}
