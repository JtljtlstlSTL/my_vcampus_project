1. **在服务器电脑A开启远程访问**
   
   - 找到你安装MYSQL的目录\MySQL\MySQL Server 8.0\my.ini；
   - 在[mysqld]段确保
    ```ini
     # 注释掉或改成 0.0.0.0
     # bind-address = 127.0.0.1
     bind-address = 0.0.0.0
     # 确保没有启用 skip-networking
     # skip-networking
    ```

  - 此为将数据库改为检测全网卡
  - 注意若你之前打开了数据库则需重启数据库

2. **创建专用数据库与远程用户**

    ```sql
     -- 登录后执行（把 mydb、appuser、密码、网段按需替换）
     CREATE DATABASE mydb CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
    ```
   若已有数据库则可跳过该步骤
    ```sql
     -- 仅允许局域网某网段访问（例：192.168.1.*）
     CREATE USER 'appuser'@'192.168.1.%' IDENTIFIED BY 'StrongP@ssw0rd!';
     GRANT ALL PRIVILEGES ON mydb.* TO 'appuser'@'192.168.1.%';
     FLUSH PRIVILEGES;

     -- 安全起见，禁用 root 远程（可选但强烈建议）
     -- UPDATE mysql.user SET host='localhost' WHERE user='root' AND host='%';
     -- FLUSH PRIVILEGES;
    ```

3. **防火墙放行并确认MYSQL正在监听**
   
   打开“Windows Defender 防火墙” → “高级设置” → “入站规则” → 新建规则
   选择端口 → TCP → 特定本地端口填入 3306 → 允许连接 → 勾选域/专用（尽量不要勾公用） → 命名“MySQL 3306”
   在终端输入以下命令以检验
    ```powershell
     netstat -ano | findstr :3306
    ```
4. **客户端电脑B进行连接测试**

   - 局域网连接
    ```powershell
     mysql -h <服务器A的IP> -P 3306 -u appuser -p
     # 然后输入密码 StrongP@ssw0rd!
    ```
    若你不知道服务器地址，可在终端输入以下命令进行查看
    ```powershell
     ipconfig
    ```
    
   - VPN
   - SSH 
