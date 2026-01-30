# 站台门调试助手 (Platform Door Debug Assistant)

## 功能说明 (Features)

站台门调试助手是一个专门用于测试站台门设备的串口调试工具。它可以接收、解析和验证站台门控制命令。

The Platform Door Debug Assistant is a specialized serial port debugging tool for testing platform door equipment. It can receive, parse, and validate platform door control commands.

## 支持的命令 (Supported Commands)

系统支持以下5个固定命令：

The system supports the following 5 fixed commands:

1. **全部上升 (All Rise)**: `5A 02 7E 00 04 08 F1 03 02 01 80 A5 6D FC`
2. **全部下降 (All Descend)**: `5A 02 7E 00 04 08 F1 03 01 01 7F A5 D9 BD`
3. **全部停止 (All Stop)**: `5A 02 7E 00 04 08 F1 03 03 01 81 A5 01 FC`
4. **短编上升 (Short Rise)**: `5A 02 7E 00 04 08 D1 03 02 01 60 A5 CD B2`
5. **重联上升 (Reconnect Rise)**: `5A 02 7E 00 04 08 E1 03 02 01 70 A5 FD BA`

## CRC校验 (CRC Validation)

所有命令使用CRC-16/MODBUS校验算法，最后两个字节为CRC校验码。

All commands use CRC-16/MODBUS checksum algorithm, with the last two bytes being the CRC checksum.

### CRC-16/MODBUS 算法说明

- 初始值：0xFFFF
- 多项式：0xA001 (反向)
- 字节序：高字节在前，低字节在后

## 使用方法 (Usage)

### 1. 启动应用 (Start Application)

在主菜单选择"站台门调试助手"按钮。

Select "站台门调试助手" button from the main menu.

### 2. 配置串口 (Configure Serial Port)

1. 选择串口设备 (Select serial device)
2. 设置波特率 (Set baud rate)
3. 配置其他串口参数 (Configure other serial parameters)

### 3. 发送测试命令 (Send Test Commands)

点击预定义的按钮快速发送测试命令：

Click predefined buttons to quickly send test commands:

- 全部上升 (All Rise)
- 全部下降 (All Descend)
- 全部停止 (All Stop)
- 短编上升 (Short Rise)
- 重联上升 (Reconnect Rise)

### 4. 自定义命令 (Custom Commands)

1. 在"自定义命令"输入框输入十六进制命令
2. 系统会自动验证CRC校验码
3. 点击"发送自定义命令"按钮发送

### 5. 接收数据分析 (Receive Data Analysis)

接收到的数据会自动：
- 解析命令类型
- 验证CRC校验
- 显示详细信息
- 标记错误（如CRC校验失败）

Received data will automatically:
- Parse command type
- Validate CRC checksum
- Display detailed information
- Mark errors (e.g., CRC validation failure)

## 命令格式 (Command Format)

```
┌────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┐
│  5A    │  02    │  7E    │  00    │  04    │  08    │  XX    │  03    │  YY    │  01    │  ZZ    │  A5    │  CRC-H │  CRC-L │
├────────┴────────┴────────┴────────┴────────┴────────┼────────┴────────┴────────┴────────┼────────┴────────┼────────┴────────┤
│              固定协议头                              │      命令参数                      │  校验数据      │    CRC校验码    │
│          (Fixed Protocol Header)                    │   (Command Parameters)            │(Check Data)   │  (CRC Checksum) │
└─────────────────────────────────────────────────────┴───────────────────────────────────┴────────────────┴─────────────────┘
```

### 字段说明 (Field Description)

- **Byte 0-5**: 固定协议头 `5A 02 7E 00 04 08`
- **Byte 6**: 命令类型标识
  - `F1`: 全部操作
  - `D1`: 短编操作  
  - `E1`: 重联操作
- **Byte 7**: 固定值 `03`
- **Byte 8**: 动作类型
  - `01`: 下降
  - `02`: 上升
  - `03`: 停止
- **Byte 9**: 固定值 `01`
- **Byte 10-11**: 命令相关数据
- **Byte 12-13**: CRC-16校验码（高字节在前）

## 错误处理 (Error Handling)

系统会检测并提示以下错误：

The system detects and prompts for the following errors:

1. **数据格式错误** (Data format error): 无效的十六进制格式
2. **数据长度不足** (Insufficient data length): 命令长度小于14字节
3. **CRC校验失败** (CRC validation failed): 校验码不匹配
4. **未知命令** (Unknown command): 不在预定义命令列表中

## 技术实现 (Technical Implementation)

### 文件结构 (File Structure)

```
app/src/main/java/com/yujing/chuankou/
├── activity/
│   └── PlatformDoorActivity.java        # 主Activity
├── utils/
│   └── PlatformDoorParser.java          # 命令解析和CRC验证工具类
└── res/layout/
    └── activity_platform_door.xml       # UI布局文件
```

### 核心类 (Core Classes)

#### PlatformDoorParser

命令解析和CRC验证的核心工具类：

Core utility class for command parsing and CRC validation:

```java
// CRC-16计算
byte[] crc = PlatformDoorParser.calculateCRC16(data);

// CRC验证
boolean valid = PlatformDoorParser.verifyCRC(dataWithCRC);

// 命令解析
PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);
```

#### PlatformDoorActivity

主界面Activity，负责：
- 串口通信
- 用户界面交互
- 数据显示和日志记录

Main Activity responsible for:
- Serial communication
- User interface interaction
- Data display and logging

## 单元测试 (Unit Tests)

项目包含完整的单元测试，验证CRC算法的正确性：

The project includes comprehensive unit tests to verify CRC algorithm correctness:

```bash
# 运行单元测试
./gradlew test
```

测试覆盖：
- CRC-16计算正确性
- 所有预定义命令的CRC验证
- 错误数据的检测

Test coverage:
- CRC-16 calculation correctness
- CRC validation for all predefined commands
- Error data detection

## 注意事项 (Notes)

1. 使用前请确保正确配置串口参数
2. 确保设备权限已授予应用
3. 建议在实际设备上测试，模拟器可能不支持串口功能

## 版本历史 (Version History)

### v1.0.0 (2026-01-30)
- 初始版本
- 支持5个预定义命令
- 实现CRC-16/MODBUS校验
- 自定义命令支持
- 实时数据解析和验证
