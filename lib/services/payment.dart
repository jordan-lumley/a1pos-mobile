import 'package:flutter/services.dart';

class PaymentPlatformService {
  static const platform = const MethodChannel('flutter.a1pos.com.channel');
  Future<String> refund(String amount) async {
    return await platform.invokeMethod('REFUND', amount);
  }

  Future<String> sale(String amount) async {
    return await platform.invokeMethod('SALE', amount);
  }

  Future<String> printLastReceipt() async {
    return await platform.invokeMethod('PRINT_LAST_TRANS');
  }
}
