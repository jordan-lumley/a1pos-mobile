import 'dart:async';

import 'package:a1pos/screens/settlement.dart';
import 'package:a1pos/screens/dashboard.dart';
import 'package:a1pos/screens/settings.dart';
import 'package:a1pos/screens/terminal.dart';
import 'package:a1pos/services/logger.dart';
import 'package:a1pos/services/settings.dart';
import 'package:flutter/material.dart';

Timer autoBatchTimer;
SettingsPlatformService settingsService = SettingsPlatformService();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Logger.initialize();
  // settingsService.getAutoBatchStatus().then((status) {
  //   var needsStarted = false;
  //   if (status != null) {
  //     if (status != "false") {
  //       needsStarted = true;
  //     }
  //   }

  //   if (needsStarted) {
  //     settingsService.getAutoBatchTime().then((time) {
  //       startAutoBatchTimer(time);
  //     });
  //   } else {
  //     if (autoBatchTimer != null) {
  //       stopAutoBatchTimer();
  //     }
  //   }
  // });

  runApp(A1POS());
}

// void startAutoBatchTimer(time) {
//   autoBatchTimer = Timer.periodic(Duration(seconds: 5), (v) async {
//     var now = DateTime.now();
//     if (SettingsPlatformService.autoBatchTime != null) {
//       if (SettingsPlatformService.autoBatchTime == now) {
//         print('blah');
//       }
//     }
//   });
// }

// void stopAutoBatchTimer() {
//   autoBatchTimer.cancel();
// }

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
