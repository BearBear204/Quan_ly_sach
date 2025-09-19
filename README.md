<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
     QUẢN LÝ THƯ VIỆN
</h2>
<div align="center">
    <p align="center">
        <img alt="AIoTLab Logo" width="170" src="https://github.com/user-attachments/assets/711a2cd8-7eb4-4dae-9d90-12c0a0a208a2" />
        <img alt="AIoTLab Logo" width="180" src="https://github.com/user-attachments/assets/dc2ef2b8-9a70-4cfa-9b4b-f6c2f25f1660" />
        <img alt="DaiNam University Logo" width="200" src="https://github.com/user-attachments/assets/77fe0fd1-2e55-4032-be3c-b1a705a1b574" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)
</div>

---

## 📖 1. Giới thiệu hệ thống

Hệ thống quản lý thư viện mạng sử dụng giao thức TCP cho phép server gửi dữ liệu sách và lịch sử mượn/trả đến nhiều client theo thời gian thực.

* **Server**: Quản lý cơ sở dữ liệu SQLite, nhận các yêu cầu từ client (đăng nhập, đăng ký, mượn, trả sách, thêm/sửa/xóa sách), xử lý và gửi dữ liệu về client tương ứng.
* **Client**: Giao diện người dùng (GUI) hiển thị danh sách sách, lịch sử mượn/trả và các thông báo phản hồi từ server. Cho phép mượn/trả sách, quản lý tài khoản, xem danh sách và lịch sử.
* **Lưu trữ dữ liệu**: Dữ liệu người dùng, sách và lịch sử mượn/trả được lưu trên SQLite database (`library.db`).

**Các chức năng chính:**

### 🖥️ Chức năng của Server

* Quản lý người dùng: Xử lý đăng nhập/đăng ký với phân quyền admin/user.
* Quản lý sách: Thêm, sửa, xóa, liệt kê sách.
* Quản lý mượn/trả: Theo dõi trạng thái sách và lịch sử mượn/trả.
* Giao tiếp TCP: Nhận lệnh từ client, xử lý và gửi dữ liệu/response kịp thời.
* GUI: Hiển thị log các hoạt động của server (đăng nhập, đăng ký, mượn/trả sách, lỗi…).

### 💻 Chức năng của Client

* Đăng nhập/đăng ký: Quản lý tài khoản user/admin.
* Xem danh sách sách: Hiển thị danh sách đầy đủ với số lượng và số lượng đang mượn.
* Mượn/trả sách: Gửi yêu cầu mượn/trả tới server, tự động reload danh sách.
* Quản lý lịch sử: Hiển thị lịch sử mượn/trả của user hoặc toàn bộ cho admin.
* GUI trực quan: Các bảng hiển thị sách và lịch sử, các nút thao tác dễ sử dụng.

### 🌐 Chức năng hệ thống

* **Giao thức TCP**: Sử dụng `Socket` và `ServerSocket` cho kết nối client-server.
* **Đa luồng**: Mỗi client chạy trên một luồng riêng, server không bị block khi nhiều client kết nối.
* **Cơ sở dữ liệu SQLite**: Lưu trữ sách, người dùng và lịch sử mượn/trả.
* **Cập nhật thời gian thực**: Client tự reload danh sách sách và lịch sử khi có thay đổi.
* **Xử lý lỗi**: Thông báo lỗi đăng nhập, đăng ký, mượn/trả sách qua GUI.

---

## 🔧 2. Công nghệ sử dụng

* **Java Core & Multithreading**: Sử dụng Thread để lắng nghe nhiều client cùng lúc và xử lý lệnh TCP.
* **Java Swing**: Xây dựng giao diện người dùng cho client và server.
* **Java Sockets (TCP)**: Giao tiếp giữa client và server thông qua `Socket` và `ServerSocket`.
* **SQLite**: Lưu trữ dữ liệu sách, người dùng và lịch sử mượn/trả.
* **File I/O**: Lưu log (tùy chỉnh).

Hỗ trợ:

* `java.net` & `java.io`: Kết nối mạng, đọc/ghi dữ liệu.
* `javax.swing` & `javax.swing.table`: Tạo bảng hiển thị danh sách sách và lịch sử mượn/trả.

---


## 🚀 3. Hình ảnh các chức năng

<p align="center">
  <img src="https://github.com/user-attachments/assets/18a54ea4-9c5d-4728-b93f-94cd8189f7e6" width="700"/>
  <br>
  <b>Hình 1: Giao diện Đăng nhập / Đăng ký</b>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/7d03cd23-1c5d-4f0b-af9d-5ae225154168" width="700"/>
  <br>
  <b>Hình 2: Giao diện Client admin hiển thị danh sách sách và lịch sử mượn/trả</b>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/dc495584-8982-4afe-b3e3-7e7b227e5434" width="700"/>
  <br>
  <b>Hình 3: Giao diện Client user hiển thị danh sách sách và lịch sử mượn/trả cá nhân</b>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/cd649e42-8c22-44a6-a2d1-d3ff2dce342b" width="700"/>
  <br>
  <b>Hình 4a: Thông báo lỗi khi đăng nhập/đăng ký thất bại</b>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/7dbecb58-9ab1-49e3-8cf7-9d206be3a818" width="700"/>
  <br>
  <b>Hình 4b: Thông báo lỗi khi mượn/trả sách không thành công</b>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/1864ee01-ae83-4804-baf0-82dd82a8d54e" width="700"/>
  <br>
  <b>Hình 5a: Giao diện màn hình thêm sách</b>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/88c44590-7c1f-4057-b924-4e7f05625692" width="700"/>
  <br>
  <b>Hình 5b: Giao diện màn hình sửa sách</b>
</p>

---

## 📝 4. Hướng dẫn cài đặt và sử dụng

### 🔧 Yêu cầu hệ thống

* Java Development Kit (JDK) 8 trở lên
* Hệ điều hành: Windows
* IDE: Eclipse
* Bộ nhớ: tối thiểu 512MB RAM
* Dung lượng: \~20MB cho mã nguồn và file thực thi

### 📦 Cài đặt và triển khai

#### Bước 1: Chuẩn bị môi trường

* Kiểm tra Java:

```bash
java -version
javac -version
```

* Tải mã nguồn: Thư mục `LibrarySystem` chứa các file:

  * `server.java`
  * `client.java`
  * `BookDAO.java`
  * `UserDAO.java`
  * `BorrowDAO.java`
  * `DBConnection.java`

#### Bước 2: Biên dịch mã nguồn

```bash
javac library/*.java
```

#### Bước 3: Chạy ứng dụng

* **Khởi động Server**

```bash
java library.server
```

Server sẽ lắng nghe các kết nối TCP trên port 2000.

* **Khởi động Client**

```bash
java library.client
```

Mở terminal/IDE mới cho mỗi client. Client sẽ kết nối tới server và hiển thị giao diện quản lý sách.

---

### 🚀 Sử dụng ứng dụng

1. **Server**

   * Khởi động server để lắng nghe kết nối client.
   * Theo dõi log đăng nhập/đăng ký và mượn/trả sách.

2. **Client**

   * Đăng nhập/đăng ký tài khoản user hoặc admin.
   * Admin: thêm/sửa/xóa sách, xem toàn bộ lịch sử mượn/trả.
   * User: xem danh sách sách, mượn/trả sách, xem lịch sử cá nhân.

---

## 📚 5. Thông tin liên hệ

* Họ tên: Vũ Đức Anh
* Lớp: CNTT 16-01
* Email: [anhvuduc9204@gmail.com](mailto:anhvuduc9204@gmail.com)

© 2025 AIoTLab – Faculty of Information Technology, DaiNam University. All rights reserved.
