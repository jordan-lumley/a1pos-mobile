// import 'package:battery/battery.dart';
import 'dart:async';

import 'package:battery/battery.dart';
import 'package:flutter/material.dart';

class AppBarWidget extends StatefulWidget implements PreferredSizeWidget {
  AppBarWidget({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _AppBarWidgetState createState() => _AppBarWidgetState();
  @override
  Size get preferredSize => Size.fromHeight(50.0);
}

class _AppBarWidgetState extends State<AppBarWidget> {
  Battery _battery = Battery();
  StreamSubscription<BatteryState> _batteryStateSubscription;

  var _batteryLevel;

  @override
  void initState() {
    super.initState();

    _battery.batteryLevel.then((level) {
      setState(() {
        _batteryLevel = level;
      });
    });

    _batteryStateSubscription =
        _battery.onBatteryStateChanged.listen((BatteryState state) {
      _battery.batteryLevel.then((level) {
        setState(() {
          _batteryLevel = level;
        });
      });
    });
  }

  @override
  void dispose() {
    super.dispose();

    _batteryStateSubscription.cancel();
    _batteryStateSubscription = null;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Padding(
          padding: EdgeInsets.fromLTRB(0, 10, 0, 10),
          child: Row(
            children: <Widget>[
              Image(
                image:
                    ExactAssetImage('assets/images/a1logo_blk.png', scale: 6),
              ),
              Padding(
                padding: EdgeInsets.fromLTRB(5, 0, 0, 0),
                child: Text(
                  this.widget.title.toUpperCase(),
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
        ),
        actions: <Widget>[
          Center(
            child: Icon(
              Icons.battery_std,
              size: 18,
            ),
          ),
          Center(
            child: Text(_batteryLevel.toString() + "%"),
          ),
          MaterialButton(
            onPressed: () {
              Navigator.pushReplacementNamed(context, "/dashboard");
            },
            child: Icon(
              Icons.home,
              color: Colors.white,
            ),
          ),
        ],
      ),
    );
  }
}
