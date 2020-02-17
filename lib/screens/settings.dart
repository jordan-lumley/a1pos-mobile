import 'dart:async';
import 'dart:convert';

import 'package:a1pos/components/appbarwidget.dart';
import 'package:a1pos/models/CommSetting.dart';
import 'package:a1pos/services/logger.dart';
import 'package:a1pos/services/settings.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:intl/intl.dart';

class SettingsScreen extends StatefulWidget {
  SettingsScreen({Key key, this.title}) : super(key: key);

  final String title;

  @override
  SettingsScreenState createState() => SettingsScreenState();
}

class SettingsScreenState extends State<SettingsScreen> {
  CommSetting commSettings;

  var password = kReleaseMode ? "duck basketball" : "b";
  var settingsPlatformService = new SettingsPlatformService();

  var passwordController = TextEditingController();

  var commTypeController = TextEditingController();
  var timeOutController = TextEditingController();

  var autoBatchController = TextEditingController();

  var isAuthenticated = false;
  var isLoadingSettings = false;

  var currentTabTitle = 'System Settings';

  var navBarEnabled = false;
  var tipScreenEnabled = false;

  // var autoBatchStatus = false;

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

    navBarEnabled = systemSettingsResponse["isNavigationBarEnabled"];
    tipScreenEnabled = systemSettingsResponse["tipEnabled"];
    // var status = false;

    // if (systemSettingsResponse["autoBatchStatus"] != null) {
    //   if (systemSettingsResponse["autoBatchStatus"] != "false") {
    //     status = true;
    //   }
    // }

    // //  status == null
    // //       ? false
    // //       : systemSettingsResponse["autoBatchStatus"];
    // autoBatchStatus = status;
    // autoBatchController.text = systemSettingsResponse["autoBatchTime"];

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

  void toggleTipScreenShown(bool value) async {
    var response = await settingsPlatformService.setTipShownSetting(value);
    var settingsResponseJson = jsonDecode(response);
    if (settingsResponseJson["RETURN_CODE"] == "OK") {
      setState(() {
        tipScreenEnabled = value;
      });
    }
  }

  // void toggleAutoBatch(bool value) async {
  //   if (value) {
  //     var time = await settingsService.getAutoBatchTime();
  //     startAutoBatchTimer(time);
  //   } else {
  //     stopAutoBatchTimer();
  //   }

  //   await settingsPlatformService.setAutoBatchStatus(value);

  //   setState(() {
  //     autoBatchStatus = value;
  //   });
  // }

  Future<Null> selectTimePicker(BuildContext context) async {
    final TimeOfDay picked = await showTimePicker(
      context: context,
      initialTime: TimeOfDay(hour: 10, minute: 47),
      builder: (BuildContext context, Widget child) {
        return MediaQuery(
          data: MediaQuery.of(context).copyWith(alwaysUse24HourFormat: false),
          child: child,
        );
      },
    );
    if (picked != null) {
      var dt = formatTimeOfDay(picked);

      autoBatchController.text = dt;

      SystemChannels.textInput.invokeMethod('TextInput.hide');
    }
  }

  String formatTimeOfDay(TimeOfDay tod) {
    final now = new DateTime.now();
    final dt = DateTime(now.year, now.month, now.day, tod.hour, tod.minute);
    final format = DateFormat.jm();
    return format.format(dt);
  }

  // Future<void> viewLogs() async {
  //   try {
  //     var log = Logger.logFile;

  //     var logsArray = log.readAsLinesSync();

  //     if (logsArray != null) {
  //       var children = logsArray
  //           .map(
  //             (f) => Text(
  //               f,
  //               style: TextStyle(fontSize: 12),
  //             ),
  //           )
  //           .toList();

  //       showDialog(
  //         context: context,
  //         builder: (BuildContext context) {
  //           return AlertDialog(
  //             title: Text("Logs"),
  //             content: Container(
  //               width: MediaQuery.of(context).size.width,
  //               height: MediaQuery.of(context).size.height,
  //               child: ListView(
  //                 children: List.from(children),
  //               ),
  //             ),
  //             actions: <Widget>[
  //               MaterialButton(
  //                 color: Colors.teal,
  //                 onPressed: () {
  //                   Navigator.pop(context);
  //                 },
  //                 child: Text('close'),
  //               )
  //             ],
  //           );
  //         },
  //       );
  //     } else {
  //       Fluttertoast.showToast(
  //           msg: "No Logs!",
  //           toastLength: Toast.LENGTH_SHORT,
  //           gravity: ToastGravity.BOTTOM,
  //           backgroundColor: Colors.grey[600],
  //           textColor: Colors.white,
  //           fontSize: 16.0);
  //     }
  //   } catch (err) {
  //     print(err);
  //   }
  // }

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
                                  Divider(
                                    height: 30,
                                  ),
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.start,
                                    children: <Widget>[
                                      Text("Tips Enabled"),
                                      Switch(
                                        onChanged: (bool value) {
                                          toggleTipScreenShown(value);
                                        },
                                        value: tipScreenEnabled,
                                      ),
                                    ],
                                  ),
                                  Divider(
                                    height: 30,
                                  ),
                                  // Row(
                                  //   mainAxisAlignment: MainAxisAlignment.start,
                                  //   children: <Widget>[
                                  //     Text("Auto Batch"),
                                  //     Switch(
                                  //       onChanged: (bool value) {
                                  //         toggleAutoBatch(value);
                                  //       },
                                  //       value: autoBatchStatus,
                                  //     ),
                                  //   ],
                                  // ),

                                  // TextFormField(
                                  //   onTap: () {
                                  //     selectTimePicker(context);
                                  //   },
                                  //   readOnly: true,
                                  //   controller: autoBatchController,
                                  //   enabled: autoBatchStatus,
                                  //   autofocus: false,
                                  //   decoration: InputDecoration(
                                  //     labelText: "Auto Batch Time",
                                  //   ),
                                  // ),
                                  // Text(""),
                                  // Text(""),
                                  // Divider(
                                  //   height: 30,
                                  // ),
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
                                      // Padding(
                                      //   padding: EdgeInsets.all(10),
                                      //   child: MaterialButton(
                                      //     onPressed: () async {
                                      //       await viewLogs();
                                      //     },
                                      //     color: Colors.teal,
                                      //     child: Icon(
                                      //       Icons.view_list,
                                      //       color: Colors.white,
                                      //     ),
                                      //   ),
                                      // ),
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
