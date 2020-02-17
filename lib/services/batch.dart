import 'package:flutter/services.dart';

class BatchPlatformService {
  static const platform = const MethodChannel('flutter.a1pos.com.channel');
  Future<String> getTransactions(int pageIndex, int pageSize) async {
    return await platform.invokeMethod(
        'TRANSACTIONS_DETAILS', [pageIndex.toString(), pageSize.toString()]);
  }

  Future<String> getTransactionSummary() async {
    return await platform.invokeMethod('TRANSACTIONS_SUMMARY');
  }

  Future<String> closeBatch() async {
    return await platform.invokeMethod('BATCHCLOSE');
  }

  Future<String> adjustTransaction(String amount, String transId) async {
    return await platform.invokeMethod('ADJUST', [amount, transId]);
  }
}
