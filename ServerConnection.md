## MySQL基本操作

- 打开数据库
    ```powershell
     mysql -u root -p
    ```
    
- 退出数据库
    ```sql
       EXIT;
    ```
    
- 查看现有用户
    ```sql
     SELECT user, host FROM mysql.user;
    ```
    
- 查看所有数据库
    ```sql
         SHOW DATABASES;
    ```
    
- 查看某一个数据库
    ```sql
         USE vcampus;
         SHOW TABLES;
    ```  
    
- 创建数据库并导入数据
    ```sql
     CREATE DATABASE mydatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
     USE mydatabase;
     SOURCE D:/path/to/mydb.sql;
    ```
    
- 删除数据库
    ```sql
     DROP DATABASE 数据库名;
    ```
    
## 连接MySQL数据库

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
     CREATE USER 'appuser'@'%' IDENTIFIED WITH mysql_native_password BY 'password';
     GRANT ALL PRIVILEGES ON mydb.* TO 'appuser'@'%';
     FLUSH PRIVILEGES;

     -- 安全起见，禁用 root 远程（可选但强烈建议）
     -- UPDATE mysql.user SET host='localhost' WHERE user='root' AND host='%';
     -- FLUSH PRIVILEGES;
    ```

3. **防火墙放行并确认MYSQL正在监听**
   
   打开“Windows Defender 防火墙” → “高级设置” → “入站规则” → 新建规则
   选择端口 → TCP → 特定本地端口填入 3306 → 允许连接 → 勾选域/专用（尽量不要勾公用） → 命名“MySQL 3306”
     ```powershell
     #注意用管理员
     #新建入站规则
     New-NetFirewallRule -DisplayName "MySQL Remote Access" -Direction Inbound -Protocol TCP -LocalPort 3306 -Action Allow
     #netsh advfirewall firewall add rule name="MySQL" dir=in action=allow protocol=TCP localport=3306
     #检验规则是否有效
     Get-NetFirewallRule -DisplayName "MySQL"
     ```
   在终端输入以下命令以检验
    ```powershell
     netstat -ano | findstr :3306
    ```
    检验端口是否开放
  ```powershell
     Test-NetConnection -ComputerName 10.203.251.171 -Port 3306
  ```

5. **客户端电脑B进行连接测试**
   
   - 打开/关闭防火墙
    ```powershell
    netsh advfirewall set allprofiles state off
    netsh advfirewall set allprofiles state on
    ```

   - 局域网连接
    ```powershell
     mysql -h <服务器A的IP> -P 3306 -u root -p
     # 然后输入密码 StrongP@ssw0rd!
    ```
    若你不知道服务器地址，可在终端输入以下命令进行查看
    ```powershell
     ipconfig
    ```
    
   - VPN
     WireGuard 或 Tailscale
     
   - SSH 
