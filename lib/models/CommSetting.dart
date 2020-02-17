class CommSetting {
  String timeOut;
  String serialPort;
  String commType;
  String baudRate;
  String ipAddr;
  String port;
  String macAddr;

  CommSetting(this.commType, this.timeOut, this.serialPort, this.baudRate,
      this.ipAddr, this.port, this.macAddr);
}
