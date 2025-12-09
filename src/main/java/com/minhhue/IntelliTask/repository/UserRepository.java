package com.minhhue.IntelliTask.repository;

import com.minhhue.IntelliTask.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;//hơp đồng của jpa
import org.springframework.stereotype.Repository;//annotation dể đánh dấu đây là 1 bean reponsitory

//--Interface--
//gợi nói vs spring hãy quản lý nó để truy xuất data
//đây là các các lớp giống các câu lệnh truy xuất thay vì viết lệnh truy vấn nó sử dụng các hàm như findbyID()..
@Repository
//định nghĩa lớp interface dùng EXTENd để kế thừa JPA(ĐỂ dễ truy xuất data) <user,integer> làm việc với bảng user và id của user là id có type Integer
public interface UserRepository extends JpaRepository<User,Integer>{

 //chưa cần viết gì thêm 
  User findByUsername(String username);
 //crud đều được auto cc 
    
}
