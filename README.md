# intellitask-springboot
# IntelliTask

IntelliTask là một ứng dụng quản lý công việc được xây dựng bằng Spring Boot.

## Tính năng
- Quản lý task theo người dùng
- RESTful API với Spring MVC
- Kết nối cơ sở dữ liệu với Spring Data JPA

## Cấu trúc project
- `controller/`: Xử lý các request
- `service/`: Logic nghiệp vụ
- `repository/`: Truy vấn dữ liệu
- `entity/`: Định nghĩa các model
- `dto/`: Truyền dữ liệu giữa các lớp

## Cách chạy
```bash
./mvnw spring-boot:run
