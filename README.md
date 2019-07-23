# Tranining_Location

##. I

## II. Location-Based Service (Dịch vụ dựa trên vị trí)

### 1. Khái niệm

Dịch vụ dựa trên vị trí (LBS) là các dịch vụ được cung cấp qua thiết bị di động dựa vào vị trí của thiết bị. Một số ứng dụng LBS phổ biến như: thông tin địa phương, chỉ đường, địa điểm ưu thích, quản lý giám sát vị trí phương tiện,…

Định vị LBS giúp cho người sử dụng xác định được vị trí của đối tượng trên bản đồ số thông qua các phần mềm cài đặt trong smart phone, máy vi tính hay bản đồ trên website. Người sử dụng có thể nhận được các kết quả phân tích thông qua những dữ liệu nhận được từ tín hiệu định vị LBS.

LBS hoạt động dựa trên sự kết hợp của nhiều công nghệ: Internet –  Hệ thống thông tin địa lý (Geographic Information System) – Thiết bị di động (Mobile devices)

Phương thức hoạt động của LBS

Dựa vào vị trí và các yêu cầu của thiết bị, thông qua mạng Internet, nhà cung cấp gửi thông tin đến thiết bị của người dùng.

<img src="img/l1.gif"/>

Android cung cấp cho ứng dụng của bạn quyền truy cập vào các dịch vụ định vị được thiết bị hỗ trợ thông qua các lớp trong package android.location. Thành phần trung tâm của framework vị trí là lớp LocationManager, nơi cung cấp APIs để xác định vị trí, chứa nhiều phương thức liên quan


### 2. Class xử lý trong Android 

- **Location**: đại diện cho 1 vị trí địa lý bao gồm vĩ độ, kinh độ, nhãn thời gian(cả thời gian UTC và thời gian thực trôi qua từ khi khởi động), và có thể các thông tin khác như độ cao, tốc độ, ...

- **LocationManager**: cung cấp quyền truy cập vào các dịch vụ định vị vị trí. Các service này cho phép các ứng dụng có được các bản cập nhật định kỳ về vị trí địa lý của thiết bị hoặc kích hoạt một ứng dụng được chỉ định Intent khi thiết bị đi vào vị trí gần của một vị trí địa lý nhất định. LocationManager là lớp qua đó ta có thể truy cập dịch vụ định vị trên Android. Ta gọi ra bằng gọi **getSystemService()**

```
LocationManagerlocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

```

- **Location Provider**: nơi cung cấp dữ liệu vị trí trong Android. Có 2 loại chính:

+ GPS Location Provider: cung cấp vị trí với độ chính xác cao nhất (~ 2m - 20m). Dữ liệu được cung cấp qua vệ tinh. Tuy nhiên có 1 số hạn chế:

~ Chậm hơn network provider khi khởi tạo ban đầu. Nhưng khi xong thì dữ liệu cập nhật sẽ tương đối nhanh.

~ Sử dụng sẽ rất tốn pin. Nên tắt khi không sử dụng.

~ Thật ra là GPS sử dụng sóng radio, mà dễ bị cản bởi vật rắn. Do vậy nó sẽ không hoạt động khi bạn ở trong nhà hoặc tầng hầm.

+ Network Provider: sử dụng điểm phát Wifi hoặc cell tower để tính sấp xỉ vị trí. Độ chính xác trong khoảng 100 - 1000m. Độ chính xác của vị trí phụ thuộc vào số lượng điểm phát wifi hoặc cell tower trong khu vực. 

Còn 1 loại nữa là LocationManager.PASSIVE_PROVIDER

Có thể check sự có mặt của provider dùng code sau: 

```
isGpsEnabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

isNetworkEnabled = mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
```

- **LocationListener**: lắng nghe sự kiện, thông báo từ LocationManager khi vị trí thay đổi hay LocationProvider bị disable hoặc enable
 Các phương thức: onLocationChanged, onProviderDisabled, onProviderEnabled

### 3. Quyền truy cập + Chọn tiêu chí cho LocationProvider

### a. Quyền truy cập

- ACCESS_FINE_LOCATION: khi location provider là GPS hoặc Network hoặc cả hai

- ACCESS_COARSE_LOCATION: khi location provider Network

### b. Chọn tiêu chí cho LocationProvider

- Sử dụng lớp **Criteria**: giúp lựa chọn độ chính xác, mức độ năng lượng tiêu thụ, check tốc độ, chi phí thêm, ...

```
val criteria = Criteria()
            criteria.setAccuracy(Criteria.ACCURACY_FINE)
            criteria.setPowerRequirement(Criteria.POWER_LOW)
            criteria.setAltitudeRequired(false)
            criteria.setBearingRequired(false)
            criteria.setSpeedRequired(false)
            criteria.setCostAllowed(true)
```

### 4. Code

Dùng xong thì nên unregister listener khỏi location manager. Làm thế sẽ tiết kiệm được pin, nếu ko nó sẽ cố truy cập GPS để lấy thông tin địa chỉ chính xác/chi tiết hơn. Nếu cần thì đăng kí lại rồi hủy đi.


requestLocationUpdates():

- minTime: thời gian nhỏ nhất giữa 2 lần update (milisecond)

- minDistance: khoảng cách gần nhất giữa 2 lần update (meter)

- Criteria: như ở phần 3

- PendingIntent: Location update được nhận bởi LocationListener callback hoặc bởi broadcast intents tới 1 PendingIntent được cung cấp. Nếu được cung cấp thì location upate được gửi với key là KEY_LOCATION_CHANGED và một giá trị Location. ====> Xem tiếp sau


