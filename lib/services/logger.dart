import 'dart:io';

import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';

class Logger {
  static const platform = const MethodChannel('flutter.a1pos.com.channel');

  static final Logger _singleton = new Logger._internal();

  static File logFile;
  static Directory logDirectory;

  factory Logger() {
    return _singleton;
  }

  Logger._internal();

  static Future<void> initialize() async {
    try {
      var directory = await getExternalStorageDirectory();

      logDirectory = Directory("${directory.path}/logs");
      if (!await logDirectory.exists()) {
        await logDirectory.create();
      }

      await createAndInitLogFile();
    } catch (err) {
      print(err);
    }
  }

  static Future<void> createAndInitLogFile() async {
    var logFileName =
        "${DateTime.now().year}${DateTime.now().month}${DateTime.now().day}.txt";

    logFile = File("${logDirectory.path}/$logFileName");

    if (!await logFile.exists()) {
      await logFile.create();
    }

    await logFile.writeAsString(
        "********START OF main() APPLICATION ${DateTime.now()}********\n");

    await platform.invokeMethod("INIT", [logDirectory.path, logFile.path]);
  }

  static Future<void> debug(contents) async {
    var str = "-----------DEBUG ${DateTime.now()}----------- \n" +
        contents +
        "\n" +
        "------------END DEBUG---------------\n";
    await logFile.writeAsString(str);
  }

  static Future<void> error(contents) async {
    var str = "-----------ERROR ${DateTime.now()}----------- \n" +
        contents +
        "\n" +
        "------------END ERROR---------------\n";
    await logFile.writeAsString(str);
  }

  static Future<void> info(contents) async {
    var str = "-----------INFO ${DateTime.now()}----------- \n" +
        contents +
        "\n" +
        "------------END INFO---------------\n";
    await logFile.writeAsString(str);
  }
}
