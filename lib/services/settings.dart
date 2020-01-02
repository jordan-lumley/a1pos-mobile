import 'package:flutter/services.dart';

class SettingsPlatformService {
  static const platform = const MethodChannel('flutter.a1pos.com.channel');
  Future<String> getSettings() async {
    return await platform.invokeMethod('GET_COMM_SETTINGS');
  }

  Future<String> getSystemSettings() async {
    return await platform.invokeMethod('GET_SYSTEM_SETTINGS');
  }

  Future<String> saveSettings(object) async {
    return await platform.invokeMethod('SAVE_COMM_SETTINGS', object);
  }

  Future<String> setStatusBarSetting(value) async {
    return await platform.invokeMethod('SET_STATUS_BAR_SETTINGS', value);
  }

  Future<String> setNavBarSetting(value) async {
    return await platform.invokeMethod('SET_NAV_SETTINGS', value);
  }

  Future<String> testPrint() async {
    return await platform.invokeMethod('TEST_PRINT');
  }
}
