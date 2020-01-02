import 'dart:convert';

import 'package:a1pos/components/appbarwidget.dart';
import 'package:a1pos/models/CommSetting.dart';
import 'package:a1pos/services/settings.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';

class SettingsScreen extends StatefulWidget {
  SettingsScreen({Key key, this.title}) : super(key: key);

  final String title;

  @override
  SettingsScreenState createState() => SettingsScreenState();
}

class SettingsScreenState extends State<SettingsScreen> {
  CommSetting commSettings;
  // var password = "duck basketball";
  var password = "b";
  var settingsPlatformService = new SettingsPlatformService();

  var passwordController = TextEditingController();

  var commTypeController = TextEditingController();
  var timeOutController = TextEditingController();

  var isAuthenticated = false;
  var isLoadingSettings = false;

  var currentTabTitle = 'System Settings';

  var navBarEnabled = false;

  @override
  void initState() {
    super.initState();
  }

  void initSettings() async {
    setState(() {
      isLoadingSettings = true;
    });

    var systemSettingsResponse =
        await settingsPlatformService.getSystemSettings();
    var systemSettingsResponseJson = jsonDecode(systemSettingsResponse);
    var systemSettingsJson =
        jsonDecode(systemSettingsResponseJson["RETURN_MSG"]);

    navBarEnabled = systemSettingsJson["isNavigationBarEnabled"];

    var settingsResponse = await settingsPlatformService.getSettings();
    var settingsResponseJson = jsonDecode(settingsResponse);
    var settingsJson = jsonDecode(settingsResponseJson["RETURN_MSG"]);

    commTypeController.text = settingsJson["commType"];
    timeOutController.text = settingsJson["timeOut"];

    setState(() {
      isLoadingSettings = false;
    });
  }

  void saveCommSettings() async {
    var commSettingsObject = {
      "commType": commTypeController.text,
      "timeOut": timeOutController.text,
    };

    var settingsResponse = await settingsPlatformService
        .saveSettings(jsonEncode(commSettingsObject));

    var settingsResponseJson = jsonDecode(settingsResponse);
    if (settingsResponseJson["RETURN_CODE"] == "OK") {
      Fluttertoast.showToast(
          msg: "Successfully Saved!",
          toastLength: Toast.LENGTH_SHORT,
          gravity: ToastGravity.BOTTOM,
          backgroundColor: Colors.grey[600],
          textColor: Colors.white,
          fontSize: 16.0);

      initSettings();
    }
  }

  void checkPassword() {
    if (passwordController.text == password) {
      setState(() {
        isAuthenticated = true;
      });

      initSettings();
    } else {
      setState(() {
        isAuthenticated = false;
      });
      Fluttertoast.showToast(
          msg: "Failed!",
          toastLength: Toast.LENGTH_SHORT,
          gravity: ToastGravity.BOTTOM,
          backgroundColor: Colors.grey[600],
          textColor: Colors.white,
          fontSize: 16.0);
    }
  }

  void toggleNavBar(bool value) async {
    var response = await settingsPlatformService.setNavBarSetting(value);
    var settingsResponseJson = jsonDecode(response);
    if (settingsResponseJson["RETURN_CODE"] == "OK") {
      setState(() {
        navBarEnabled = value;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBarWidget(
        title: "SETTINGS",
      ),
      body: isAuthenticated
          ? Container(
              margin: EdgeInsets.all(0),
              child: isLoadingSettings
                  ? Center(child: CircularProgressIndicator())
                  : DefaultTabController(
                      length: 2,
                      child: Scaffold(
                        appBar: AppBar(
                          title: Text(currentTabTitle),
                          bottom: TabBar(
                            onTap: (int i) {
                              var nextTabTitle;
                              switch (i) {
                                case 0:
                                  nextTabTitle = "System Settings";
                                  break;
                                case 1:
                                  nextTabTitle = "Communication Settings";
                                  break;
                                default:
                              }
                              setState(() {
                                currentTabTitle = nextTabTitle;
                              });
                            },
                            tabs: [
                              Tab(
                                icon: Icon(Icons.phone_android),
                              ),
                              Tab(
                                icon: Icon(Icons.network_check),
                              ),
                            ],
                          ),
                        ),
                        body: TabBarView(
                          children: <Widget>[
                            Container(
                              padding: EdgeInsets.all(15),
                              child: Column(
                                children: <Widget>[
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.start,
                                    children: <Widget>[
                                      Text("Navigation Buttons Enabled"),
                                      Switch(
                                        onChanged: (bool value) {
                                          toggleNavBar(value);
                                        },
                                        value: navBarEnabled,
                                      ),
                                    ],
                                  ),
                                  Divider(),
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.start,
                                    children: <Widget>[
                                      Text("Quick Actions:"),
                                    ],
                                  ),
                                  Row(
                                    children: <Widget>[
                                      Padding(
                                        padding: EdgeInsets.all(10),
                                        child: MaterialButton(
                                          onPressed: () {
                                            SystemChannels.platform
                                                .invokeMethod(
                                                    'SystemNavigator.pop');
                                          },
                                          color: Colors.teal,
                                          child: Icon(
                                            Icons.home,
                                            color: Colors.white,
                                          ),
                                        ),
                                      ),
                                      Padding(
                                        padding: EdgeInsets.all(10),
                                        child: MaterialButton(
                                          onPressed: () async {
                                            await settingsPlatformService
                                                .testPrint();
                                          },
                                          color: Colors.teal,
                                          child: Icon(
                                            Icons.print,
                                            color: Colors.white,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            ),
                            Container(
                              padding: EdgeInsets.all(15),
                              child: Row(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: <Widget>[
                                  Expanded(
                                    child: ListView(
                                      children: getCommSettingsInputs(),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
            )
          : Padding(
              padding: const EdgeInsets.all(25.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  Expanded(
                    flex: 3,
                    child: Icon(Icons.settings, size: 80, color: Colors.teal),
                  ),
                  Expanded(
                    flex: 7,
                    child: TextField(
                      onSubmitted: (s) {
                        checkPassword();
                      },
                      obscureText: true,
                      controller: passwordController,
                      style: TextStyle(
                        fontSize: 25.0,
                        color: Colors.blueAccent,
                      ),
                      decoration: InputDecoration(
                        contentPadding:
                            EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
                        prefixIcon: Icon(Icons.lock),
                        suffixIcon: MaterialButton(
                          onPressed: () {
                            checkPassword();
                          },
                          child: Icon(Icons.arrow_forward, size: 28),
                        ),
                        hintText: "Password",
                        border: OutlineInputBorder(
                            borderSide: BorderSide(
                                color: Colors.blueAccent, width: 32.0),
                            borderRadius: BorderRadius.zero),
                      ),
                    ),
                  ),
                ],
              ),
            ),
    );
  }

  List<Widget> getCommSettingsInputs() {
    var inputs = new Map<String, TextEditingController>();
    inputs.putIfAbsent("Comm Type", () => commTypeController);
    inputs.putIfAbsent("Time Out", () => timeOutController);

    var inputsList = new List.generate(
      inputs.length,
      (index) => TextFormField(
        enabled: inputs.keys.toList()[index] != "Comm Type",
        controller: inputs.values.toList()[index],
        decoration: InputDecoration(
          labelText: inputs.keys.toList()[index],
        ),
      ),
    );
    return inputsList;
  }
}
