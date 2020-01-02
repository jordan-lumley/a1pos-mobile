import 'package:flutter/material.dart';

class AppBarWidget extends StatelessWidget implements PreferredSizeWidget {
  AppBarWidget({Key key, this.title}) : super(key: key);

  final String title;

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
                  title.toUpperCase(),
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
        ),
        actions: <Widget>[
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

  @override
  Size get preferredSize => Size.fromHeight(50.0);
}
