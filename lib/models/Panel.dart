import 'package:flutter/material.dart';

class Panel {
  String panelText;
  Icon icon;
  String route;

  Panel(String panelText, Icon icon, String route) {
    this.panelText = panelText;
    this.icon = icon;
    this.route = route;
  }
}
