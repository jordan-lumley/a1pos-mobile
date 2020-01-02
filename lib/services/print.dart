import 'package:flutter/services.dart';

class PrintPlatformService {
  static const platform = const MethodChannel('flutter.a1pos.com.channel');

  Future<Null> print() async {
    final bytes = await rootBundle.load('assets/x.bmp');
    final list = bytes.buffer.asUint8List();
    await platform.invokeMethod('print', list);
  }
}
