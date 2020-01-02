import 'package:a1pos/components/dialogs.dart';
import 'package:flutter/material.dart';

class TenderButton extends StatefulWidget {
  final String text;
  final Function f;
  final Icon icon;
  final bool isEnabled;

  TenderButton(this.text, this.f, this.icon, this.isEnabled);

  @override
  _TenderButtonState createState() => _TenderButtonState();
}

class _TenderButtonState extends State<TenderButton> {
  var dialogs = new Dialogs();

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.all(3),
      child: Container(
        height: double.infinity,
        child: MaterialButton(
          color: Colors.teal,
          disabledColor: Colors.black12,
          splashColor: Colors.white,
          onPressed: this.widget.isEnabled
              ? () async {
                  return await this.widget.f(this.widget.text);
                }
              : null,
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              this.widget.icon,
              Padding(
                padding: EdgeInsets.all(15),
                child: Text(
                  this.widget.text,
                  style: TextStyle(
                      fontWeight: FontWeight.bold, color: Colors.white),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
