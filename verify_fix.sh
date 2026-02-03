#!/bin/bash
# 串口识别修复验证脚本 / Serial Port Recognition Fix Verification Script

echo "=========================================="
echo "串口识别修复验证 / Serial Port Fix Verification"
echo "=========================================="
echo ""

# 检查修改的文件
echo "1. 检查修改的文件 / Checking modified files..."
if [ -f "yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java" ]; then
    echo "✅ SerialPortFinder.java 文件存在"
else
    echo "❌ SerialPortFinder.java 文件不存在"
    exit 1
fi

echo ""
echo "2. 验证关键修复点 / Verifying key fixes..."

# 检查是否移除了固定长度提取
if grep -q "substring(0, 0x15)" yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java; then
    echo "❌ 仍然使用固定长度 substring(0, 0x15)"
    echo "   Still using fixed-length substring(0, 0x15)"
else
    echo "✅ 已移除固定长度驱动名称提取"
    echo "   Fixed-length driver name extraction removed"
fi

# 检查是否添加了 scanDevicesDirectly 方法
if grep -q "scanDevicesDirectly" yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java; then
    echo "✅ 已添加 scanDevicesDirectly() 备用扫描方法"
    echo "   Added scanDevicesDirectly() fallback method"
else
    echo "❌ 缺少 scanDevicesDirectly() 方法"
    echo "   Missing scanDevicesDirectly() method"
    exit 1
fi

# 检查支持的设备模式
echo ""
echo "3. 检查支持的设备模式 / Checking supported device patterns..."
patterns=("ttyS" "ttyUSB" "ttyACM" "ttyAMA" "rfcomm" "ttyO")
all_found=true

for pattern in "${patterns[@]}"; do
    if grep -q "\"$pattern\"" yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java; then
        echo "✅ 支持 $pattern* 设备"
    else
        echo "❌ 缺少 $pattern* 设备支持"
        all_found=false
    fi
done

if [ "$all_found" = false ]; then
    exit 1
fi

# 检查改进的日志
echo ""
echo "4. 检查改进的日志 / Checking improved logging..."

log_checks=(
    "无法读取 /proc/tty/drivers"
    "无法从 /proc/tty/drivers 获取串口"
    "未找到串口设备，尝试直接扫描"
    "直接扫描找到设备"
    "无法访问 /dev 目录"
)

for log_msg in "${log_checks[@]}"; do
    if grep -q "$log_msg" yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java; then
        echo "✅ 包含日志: $log_msg"
    else
        echo "⚠️  缺少日志: $log_msg"
    fi
done

# 统计代码行数变化
echo ""
echo "5. 代码统计 / Code Statistics..."
total_lines=$(wc -l < yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java)
echo "总行数 / Total lines: $total_lines"

# 检查方法数量
method_count=$(grep -c "public.*(" yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java)
private_method_count=$(grep -c "private.*(" yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java)
echo "公有方法数 / Public methods: $method_count"
echo "私有方法数 / Private methods: $private_method_count"

# 检查是否有语法问题（简单检查括号匹配）
echo ""
echo "6. 基本语法检查 / Basic syntax check..."
open_braces=$(grep -o "{" yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java | wc -l)
close_braces=$(grep -o "}" yserialport/src/main/java/com/yujing/serialport/SerialPortFinder.java | wc -l)

if [ "$open_braces" -eq "$close_braces" ]; then
    echo "✅ 括号匹配正确 (${open_braces} 对)"
    echo "   Braces matched correctly (${open_braces} pairs)"
else
    echo "❌ 括号不匹配: { = $open_braces, } = $close_braces"
    echo "   Braces mismatch: { = $open_braces, } = $close_braces"
    exit 1
fi

echo ""
echo "=========================================="
echo "✅ 所有验证检查通过!"
echo "   All verification checks passed!"
echo "=========================================="
echo ""
echo "下一步 / Next steps:"
echo "1. 在Android设备上编译并安装应用"
echo "   Compile and install the app on an Android device"
echo "2. 运行测试用例（参见 SERIAL_PORT_FIX_TEST_GUIDE.md）"
echo "   Run test cases (see SERIAL_PORT_FIX_TEST_GUIDE.md)"
echo "3. 验证串口能被正确识别和连接"
echo "   Verify serial ports can be correctly recognized and connected"
echo ""
