import 'package:flutter/material.dart';

class NumPadButton extends StatefulWidget {
  final String text;
  final Function f;

  NumPadButton(this.text, this.f);

  @override
  _NumPadButtonState createState() => _NumPadButtonState();
}

class _NumPadButtonState extends State<NumPadButton> {
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.all(5),
      child: Container(
        height: double.infinity,
        child: MaterialButton(
          splashColor: Colors.teal,
          onPressed: () {
            this.widget.f(this.widget.text);
          },
          child: Text(
            this.widget.text,
            style: TextStyle(fontSize: 25),
          ),
        ),
      ),
    );
  }
}
