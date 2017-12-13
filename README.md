# Tài liệu tham khảo (quan trọng)
-   [Cling Tutorial](http://4thline.org/projects/cling/core/manual/cling-core-manual.xhtml#chapter.GettingStarted)
# Giải thích code của taohansamu
- Đăng kí hàm listen các devices
```java
    // Add a listener for device registration events
    upnpService.getRegistry().addListener(
            createRegistryListener(upnpService, devices)
    );
    // createRegistryListener là method khai báo các xử lí khi tìm thấy các upnp device và xử lí khi remove device

```
- có một mảng là devices để lưu các device tìm thấy. Mỗi khi device được tìm thấy, nó sẽ được thêm vào mảng devices (device được tìm thấy trong method `remoteDeviceAdded()` - được khai báo trong `createRegistryListener`);
```java
public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

                Service switchPower;
                if ((switchPower = device.findService(serviceId)) != null) {

                    System.out.println("Service discovered: " + switchPower);
                    System.out.println("Tim duoc thiet bi co url: " + device.getIdentity().getDescriptorURL().toString());
//                    device.getIdentity().get
                    devices.add(device);

//                    executeAction(upnpService, switchPower);

                }

            }
```
- hàm `executeAction` sẽ sử dụng `switchPowerService` của device để điều khiển device. Trong trường hợp này là set trạng thái bật, tắt thiết bị.
```java
//Example device tivi:
executeAction(upnpService, tivi.findService(serviceId), f.getDeviceSettingOfModeByName("2","tivi"));
 void executeAction(UpnpService upnpService, Service switchPowerService,Boolean target){
     ActionInvocation setTargetInvocation =
                new SetTargetActionInvocation(switchPowerService, target);
     upnpService.getControlPoint().execute( 
                    new ActionCallback(setTargetInvocation) {
                    @Override
                    public void success(ActionInvocation invocation) {
                        //xử lí khi setTargetInvocation thành công
                        ....
                    }
                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        //Xử lí khi setTargetInvocation thất bại
                        ....
                    }
                })
 }
class SetTargetActionInvocation extends ActionInvocation {

        SetTargetActionInvocation(Service service, Boolean target) {
            super(service.getAction("SetTarget"));
            try {

                // Throws InvalidValueException if the value is of wrong type
                setInput("NewTargetValue", target);

            } catch (InvalidValueException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }
        }
    }
```