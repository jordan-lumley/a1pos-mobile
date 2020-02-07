import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class SettingsPlatformService {
  static const platform = const MethodChannel('flutter.a1pos.com.channel');
  static const storage = const FlutterSecureStorage();
  static DateTime autoBatchTime;

  Future<String> getSettings() async {
    return await platform.invokeMethod('GET_COMM_SETTINGS');
  }

  Future<dynamic> getSystemSettings() async {
    var sysSettings = await platform.invokeMethod('GET_SYSTEM_SETTINGS');
    var systemSettingsResponseJson = jsonDecode(sysSettings);
    var systemSettingsJson =
        jsonDecode(systemSettingsResponseJson["RETURN_MSG"]);

    // var autoBatchTime = await getAutoBatchTime();
    // var autoBatchStatus = await getAutoBatchStatus();

    // systemSettingsJson["autoBatchTime"] = autoBatchTime;
    // systemSettingsJson["autoBatchStatus"] = autoBatchStatus;

    return systemSettingsJson;
  }

  Future<String> saveSettings(object) async {
    return await platform.invokeMethod('SAVE_COMM_SETTINGS', object);
  }

  Future<String> setStatusBarSetting(value) async {
    return await platform.invokeMethod('SET_STATUS_BAR_SETTINGS', value);
  }

  Future<String> setTipShownSetting(value) async {
    return await platform.invokeMethod('SET_TIP_SHOWN_SETTINGS', value);
  }

  Future<String> setNavBarSetting(value) async {
    return await platform.invokeMethod('SET_NAV_SETTINGS', value);
  }

  Future<String> testPrint() async {
    return await platform.invokeMethod('TEST_PRINT');
  }

  Future<String> getLogs() async {
    return await platform.invokeMethod('GET_LOGS');
  }

  // Future<String> getAutoBatchStatus() async {
  //   return await storage.read(key: "autoBatchStatus");
  // }

  // Future<bool> setAutoBatchStatus(bool status) async {
  //   try {
  //     await storage.write(key: "autoBatchStatus", value: status.toString());
  //     return true;
  //   } catch (err) {
  //     var blah = err;
  //   }

  //   return false;
  // }

  // Future<String> getAutoBatchTime() async {
  //   return await storage.read(key: "autoBatchTime");
  // }

  // Future<bool> setAutoBatchTime(DateTime t) async {
  //   try {
  //     autoBatchTime = t;
  //     await storage.write(key: "autoBatchTime", value: t.toString());
  //     return true;
  //   } catch (err) {
  //     var blah = err;
  //   }

  //   return false;
  // }
}
