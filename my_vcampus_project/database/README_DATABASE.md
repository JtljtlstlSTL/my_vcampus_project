# 虚拟校园系统数据库文件说明

## 📁 文件结构

```
database/
├── init.sql                  # 原始数据库脚本（保留作为参考）
├── init_new.sql              # 基础表结构脚本
├── library_data.sql          # 图书管理模块数据初始化脚本
├── library_functions.sql     # 图书管理模块完整业务逻辑
└── README_DATABASE.md        # 本说明文档
```

## 🚀 使用步骤

### 1. 创建数据库表结构
```bash
# 运行基础表结构创建脚本
mysql -u root -p < init_new.sql
```

### 2. 插入图书管理数据
```bash
# 运行数据初始化脚本
mysql -u root -p < library_data.sql
```

### 3. 创建图书管理业务逻辑
```bash
# 运行业务逻辑创建脚本（包含视图、存储过程、触发器、索引）
mysql -u root -p < library_functions.sql
```

### 一键执行（推荐）
```bash
# 按顺序执行所有脚本
mysql -u root -p < init_new.sql
mysql -u root -p < library_data.sql
mysql -u root -p < library_functions.sql
```

## 📋 文件详细说明

### init_new.sql
- **用途**：创建虚拟校园系统的完整数据库表结构
- **内容**：
  - 用户管理相关表（tblUser, tblStudent, tblStaff）
  - 一卡通相关表（tblCard, tblCard_trans）
  - 教务管理相关表（tblCourse, tblSection, tblEnrollment, tblEdu_evaluate）
  - 图书管理相关表（tblBook, tblBook_trans, tblBookCategory, tblBorrowRule）
  - 商品管理相关表（tblProduct, tblProduct_trans）
  - 视图、存储过程、触发器、索引

### library_data.sql
- **用途**：图书管理模块的初始数据
- **内容**：
  - 中图法22个基本大类分类数据
  - 借阅规则配置数据
  - 示例图书数据（涵盖多个分类）
  - 数据统计和完成提示

## 🎯 图书管理模块特性

### 中图法分类体系
- **A类**：马克思主义、列宁主义、毛泽东思想、邓小平理论
- **B类**：哲学、宗教
- **C类**：社会科学总论
- **D类**：政治、法律
- **E类**：军事
- **F类**：经济
- **G类**：文化、科学、教育、体育
- **H类**：语言、文字
- **I类**：文学
- **J类**：艺术
- **K类**：历史、地理
- **N类**：自然科学总论
- **O类**：数理科学和化学
- **P类**：天文学、地球科学
- **Q类**：生物科学
- **R类**：医药、卫生
- **S类**：农业科学
- **T类**：工业技术
- **U类**：交通运输
- **V类**：航空、航天
- **X类**：环境科学、安全科学
- **Z类**：综合性图书

### 借阅规则配置
- **学生**：最多借5本，30天，可续借2次，每次延长15天
- **教职工**：最多借8本，45天，可续借3次，每次延长20天
- **管理员**：最多借10本，60天，可续借5次，每次延长30天

### 示例图书数据
包含30本示例图书，涵盖以下分类：
- T类（工业技术）：Java、Python、Spring Boot、算法等计算机类图书
- I类（文学）：百年孤独、红楼梦、活着等文学类图书
- K类（历史、地理）：史记、资治通鉴等历史类图书
- J类（艺术）：艺术的故事、中国绘画史等艺术类图书
- N类（自然科学总论）：时间简史、宇宙的琴弦等科学类图书
- A类（马克思主义）：马克思主义基本原理概论等
- B类（哲学、宗教）：西方哲学史、论语等
- F类（经济）：经济学原理、国富论等
- H类（语言、文字）：现代汉语、古代汉语等
- G类（文化、科学、教育、体育）：教育学原理、图书馆学概论等
- D类（政治、法律）：宪法学、法理学等
- O类（数理科学和化学）：高等数学、线性代数等
- R类（医药、卫生）：内科学、中医基础理论等

## 🔧 维护说明

### 添加新图书
```sql
INSERT INTO tblBook (isbn, Title, Author, Publisher, Publish_date, Category, Location, Total_qty, Avail_qty, Status) 
VALUES ('ISBN号', '书名', '作者', '出版社', '出版日期', '分类代码', '馆藏位置', 总数量, 可借数量, 'IN_LIBRARY');
```

### 修改借阅规则
```sql
UPDATE tblBorrowRule 
SET max_borrow_count = 新数量, max_borrow_days = 新天数 
WHERE user_type = 'student';
```

### 添加新分类
```sql
INSERT INTO tblBookCategory (category_code, category_name, description, sort_order) 
VALUES ('新代码', '新分类名', '分类描述', 排序号);
```

## ⚠️ 注意事项

1. **执行顺序**：必须先运行 `init_new.sql` 创建表结构，再运行 `library_data.sql` 插入数据
2. **数据备份**：在生产环境中，建议在执行前备份现有数据
3. **权限要求**：需要数据库的 CREATE、INSERT、UPDATE 权限
4. **字符集**：确保数据库使用 utf8mb4 字符集以支持中文

## 📞 技术支持

如有问题，请参考项目文档或联系开发团队。
