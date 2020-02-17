import 'package:a1pos/models/Panel.dart';
import 'package:flutter/material.dart';

class DashboardScreen extends StatefulWidget {
  DashboardScreen({Key key, this.title}) : super(key: key);

  final String title;

  @override
  DashboardScreenState createState() => DashboardScreenState();
}

class DashboardScreenState extends State<DashboardScreen> {
  @override
  Widget build(BuildContext context) {
    var panels = new List<Widget>();

    panels.add(createPanel(context,
        new Panel("Terminal", Icon(Icons.receipt, size: 40), "/terminal")));
    panels.add(createPanel(
        context,
        new Panel("Settlements", Icon(Icons.attach_money, size: 40),
            "/settlements")));
    panels.add(createPanel(context,
        new Panel("Settings", Icon(Icons.settings, size: 40), "/settings")));

    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          image: DecorationImage(
            image: ExactAssetImage('assets/images/bg.jpg'),
            fit: BoxFit.cover,
          ),
        ),
        child: Column(
          children: <Widget>[
            Expanded(
              flex: 2,
              child: Container(
                child: Padding(
                  padding: EdgeInsets.all(40),
                  child: Image(
                    image: AssetImage('assets/images/a1logo.png'),
                  ),
                ),
              ),
            ),
            Expanded(
              flex: 8,
              child: GridView.count(
                padding: EdgeInsets.all(15),
                mainAxisSpacing: 10,
                crossAxisSpacing: 10,
                crossAxisCount: 3,
                children: panels,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

Widget createPanel(BuildContext context, Panel panel) {
  return MaterialButton(
    splashColor: Colors.green,
    color: Colors.white70,
    child: Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: <Widget>[
        panel.icon,
        Text(''),
        Text(
          panel.panelText,
          style: TextStyle(
            fontSize: 12,
          ),
        ),
      ],
    ),
    onPressed: () {
      Navigator.pushReplacementNamed(context, panel.route);
    },
  );
}
