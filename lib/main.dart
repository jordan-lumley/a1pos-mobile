import 'package:a1pos/screens/settlement.dart';
import 'package:a1pos/screens/dashboard.dart';
import 'package:a1pos/screens/settings.dart';
import 'package:a1pos/screens/terminal.dart';
import 'package:flutter/material.dart';

void main() => runApp(A1POS());

class A1POS extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'A1POS',
      theme: ThemeData(
        primarySwatch: Colors.teal,
        fontFamily: 'LatoLight',
      ),
      home: TerminalScreen(),
      onGenerateRoute: (RouteSettings settings) {
        switch (settings.name) {
          case '/dashboard':
            return MaterialPageRoute(builder: (context) => DashboardScreen());
            break;
          case '/terminal':
            return MaterialPageRoute(builder: (context) => TerminalScreen());
            break;
          case '/settings':
            return MaterialPageRoute(builder: (context) => SettingsScreen());
            break;
          case '/settlements':
            return MaterialPageRoute(builder: (context) => SettlementScreen());
            break;
        }
      },
    );
  }
}
