# Platform Door Debug Assistant - Manual Test Guide
# 站台门调试助手 - 手动测试指南

## Prerequisites (前提条件)

1. Android device with serial port support (支持串口的安卓设备)
2. Serial port device configured (串口设备已配置)
3. YSerialPort app installed (已安装YSerialPort应用)

## Test Cases (测试用例)

### Test 1: Launch Platform Door Activity
### 测试1：启动站台门调试助手

**Steps (步骤):**
1. Open YSerialPort app
2. Click "站台门调试助手" button on main screen
3. Verify the Platform Door Activity opens

**Expected Result (预期结果):**
- Platform Door Activity UI displays correctly
- Shows test command buttons
- Shows custom command input field
- Shows receive and send log areas

---

### Test 2: Send Predefined Commands
### 测试2：发送预定义命令

**Steps (步骤):**
1. Configure serial port (device and baud rate)
2. Click "全部上升" (All Rise) button
3. Verify command is sent and displayed in send log
4. Repeat for other commands:
   - 全部下降 (All Descend)
   - 全部停止 (All Stop)
   - 短编上升 (Short Rise)
   - 重联上升 (Reconnect Rise)

**Expected Result (预期结果):**
- Each command is sent successfully
- Send log shows timestamp and command hex string
- Command name is displayed correctly

---

### Test 3: Receive and Parse Valid Commands
### 测试3：接收并解析有效命令

**Steps (步骤):**
1. Send or receive one of the valid test commands
2. Check the receive log

**Expected Result (预期结果):**
- Command is parsed correctly
- Command name is identified (e.g., "全部上升")
- CRC validation shows "通过 ✓" in green color
- Hex string is displayed

**Test Data (测试数据):**
```
5A027E000408F1030201 80A56DFC  -> Should parse as "全部上升" with CRC pass
5A027E000408F1030101 7FA5D9BD  -> Should parse as "全部下降" with CRC pass
5A027E000408F1030301 81A501FC  -> Should parse as "全部停止" with CRC pass
5A027E000408D1030201 60A5CDB2  -> Should parse as "短编上升" with CRC pass
5A027E000408E1030201 70A5FDBA  -> Should parse as "重联上升" with CRC pass
```

---

### Test 4: Receive Invalid CRC Commands
### 测试4：接收CRC错误的命令

**Steps (步骤):**
1. Send a command with wrong CRC bytes
2. Check the receive log

**Expected Result (预期结果):**
- Command is marked as "未知命令"
- CRC validation shows "失败 ✗" in red color
- Error message shows "CRC校验失败"

**Test Data (测试数据):**
```
5A027E000408F1030201 80A50000  -> Wrong CRC, should fail validation
5A027E000408F1030101 7FA51234  -> Wrong CRC, should fail validation
```

---

### Test 5: Send Custom Commands
### 测试5：发送自定义命令

**Steps (步骤):**
1. Enter a valid command in custom command field: `5A027E000408F1030201 80A56DFC`
2. Click "发送自定义命令" button
3. Verify command is validated and sent

**Expected Result (预期结果):**
- Command passes CRC validation
- Command is sent successfully
- Send log shows the command

---

### Test 6: Custom Command with Invalid CRC
### 测试6：发送CRC错误的自定义命令

**Steps (步骤):**
1. Enter command with wrong CRC: `5A027E000408F1030201 80A50000`
2. Click "发送自定义命令" button

**Expected Result (预期结果):**
- Toast message shows validation error: "命令验证失败：CRC校验失败"
- Command is NOT sent

---

### Test 7: Custom Command with Invalid Format
### 测试7：发送格式错误的命令

**Steps (步骤):**
1. Enter invalid hex string: `GGHHII`
2. Click "发送自定义命令" button

**Expected Result (预期结果):**
- Toast message shows format error
- Command is NOT sent

---

### Test 8: Clear Logs
### 测试8：清空日志

**Steps (步骤):**
1. Send some commands to populate logs
2. Click "清空" (Clear) button above receive log
3. Click "清空" (Clear) button above send log

**Expected Result (预期结果):**
- Receive log is cleared
- Send log is cleared

---

### Test 9: Serial Port Configuration
### 测试9：串口配置

**Steps (步骤):**
1. Click settings icon/area
2. Change serial port device
3. Change baud rate
4. Verify connection restarts

**Expected Result (预期结果):**
- Serial port reconnects with new settings
- Logs are cleared
- Can send and receive with new settings

---

## CRC Calculation Verification (CRC计算验证)

Run unit tests to verify CRC algorithm:
```bash
cd /home/runner/work/YSerialPort/YSerialPort
./gradlew test
```

All tests in `PlatformDoorParserTest` should pass.

---

## Notes (注意事项)

1. This test guide assumes a physical device or serial port loopback for testing
2. UI screenshots should be taken during manual testing
3. All 5 predefined commands have been verified with CRC calculation
4. The CRC-16/MODBUS algorithm has been validated with standalone Java program
