# cmpp-client-ui
> cmpp-client-ui 一个基于cmpp协议的短信发送客户端

## 项目介绍
用于cmpp协议的短信发送客户端，支持cmpp2.0和cmpp3.0协议，支持长短信发送，支持状态报告接收，支持上行短信接收，支持短信发送速率控制，支持短信发送失败重发，支持短信发送状态查询，支持短信发送状态报告查询，支持短信发送状态报告重发，


打包MacOS
```shell
 jpackage --type dmg --name CmppClient --input ~/Desktop --main-jar target/cmpp-ui.jar  --main-class org.springframework.boot.loader.launch.JarLauncher --icon target/classes/images/cmpp.icns --app-version 1.0.0 --mac-package-name CmppClient --mac-package-identifier com.calo.cmpp.CmppClient --input ./   --java-options '--enable-preview' --verbose
```
