package com.yujing.chuankou.utils;

import com.yujing.utils.YConvert;

/**
 * Platform Door Command Parser
 * 站台门命令解析器
 * 
 * @author yujing
 */
public class PlatformDoorParser {
    
    // Command types
    public static final int CMD_ALL_RISE = 1;      // 全部上升
    public static final int CMD_ALL_DESCEND = 2;   // 全部下降
    public static final int CMD_ALL_STOP = 3;      // 全部停止
    public static final int CMD_SHORT_RISE = 4;    // 短编上升
    public static final int CMD_RECONNECT_RISE = 5; // 重联上升
    public static final int CMD_UNKNOWN = 0;       // 未知命令
    
    /**
     * Parse result class
     */
    public static class ParseResult {
        public boolean valid;           // CRC校验是否通过
        public int commandType;         // 命令类型
        public String commandName;      // 命令名称
        public String errorMessage;     // 错误信息
        public String hexString;        // 原始十六进制字符串
        
        public ParseResult(boolean valid, int commandType, String commandName, String errorMessage, String hexString) {
            this.valid = valid;
            this.commandType = commandType;
            this.commandName = commandName;
            this.errorMessage = errorMessage;
            this.hexString = hexString;
        }
    }
    
    /**
     * Calculate CRC-16/MODBUS checksum
     * 计算CRC-16/MODBUS校验码
     * 
     * @param data byte array (without CRC bytes)
     * @return CRC value as 2 bytes [high byte, low byte] as stored in the command
     */
    public static byte[] calculateCRC16(byte[] data) {
        int crc = 0xFFFF;
        
        for (byte b : data) {
            crc ^= (b & 0xFF);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= 0xA001;
                } else {
                    crc >>= 1;
                }
            }
        }
        
        // Return as [high byte, low byte] to match command format
        return new byte[]{(byte) ((crc >> 8) & 0xFF), (byte) (crc & 0xFF)};
    }
    
    /**
     * Verify CRC checksum
     * 验证CRC校验
     * 
     * @param data complete data with CRC
     * @return true if CRC is valid
     */
    public static boolean verifyCRC(byte[] data) {
        if (data == null || data.length < 3) {
            return false;
        }
        
        // Extract data without CRC (all bytes except last 2)
        byte[] dataWithoutCRC = new byte[data.length - 2];
        System.arraycopy(data, 0, dataWithoutCRC, 0, data.length - 2);
        
        // Calculate expected CRC
        byte[] calculatedCRC = calculateCRC16(dataWithoutCRC);
        
        // Compare with actual CRC (last 2 bytes)
        return calculatedCRC[0] == data[data.length - 2] && 
               calculatedCRC[1] == data[data.length - 1];
    }
    
    /**
     * Parse platform door command
     * 解析站台门命令
     * 
     * @param hexString hex string of command (e.g., "5A027E000408F1030201 80A56DFC")
     * @return ParseResult object
     */
    public static ParseResult parseCommand(String hexString) {
        // Remove spaces
        hexString = hexString.replace(" ", "").toUpperCase();
        
        // Convert to bytes
        byte[] data;
        try {
            data = YConvert.hexStringToByte(hexString);
        } catch (Exception e) {
            return new ParseResult(false, CMD_UNKNOWN, "未知命令", 
                "数据格式错误: " + e.getMessage(), hexString);
        }
        
        // Verify minimum length
        if (data.length < 14) {
            return new ParseResult(false, CMD_UNKNOWN, "未知命令", 
                "数据长度不足", hexString);
        }
        
        // Verify CRC
        boolean crcValid = verifyCRC(data);
        if (!crcValid) {
            return new ParseResult(false, CMD_UNKNOWN, "未知命令", 
                "CRC校验失败", hexString);
        }
        
        // Identify command type based on byte 6 and byte 8
        int commandType = CMD_UNKNOWN;
        String commandName = "未知命令";
        
        // Check command pattern
        // All commands start with: 5A 02 7E 00 04 08
        if (data[0] == (byte)0x5A && data[1] == (byte)0x02 && 
            data[2] == (byte)0x7E && data[3] == (byte)0x00 &&
            data[4] == (byte)0x04 && data[5] == (byte)0x08) {
            
            byte byte6 = data[6];  // F1/D1/E1
            byte byte8 = data[8];  // 02/01/03
            
            // F1 03 02 = All rise
            if (byte6 == (byte)0xF1 && data[7] == (byte)0x03 && byte8 == (byte)0x02) {
                commandType = CMD_ALL_RISE;
                commandName = "全部上升";
            }
            // F1 03 01 = All descend
            else if (byte6 == (byte)0xF1 && data[7] == (byte)0x03 && byte8 == (byte)0x01) {
                commandType = CMD_ALL_DESCEND;
                commandName = "全部下降";
            }
            // F1 03 03 = All stop
            else if (byte6 == (byte)0xF1 && data[7] == (byte)0x03 && byte8 == (byte)0x03) {
                commandType = CMD_ALL_STOP;
                commandName = "全部停止";
            }
            // D1 03 02 = Short rise
            else if (byte6 == (byte)0xD1 && data[7] == (byte)0x03 && byte8 == (byte)0x02) {
                commandType = CMD_SHORT_RISE;
                commandName = "短编上升";
            }
            // E1 03 02 = Reconnect rise
            else if (byte6 == (byte)0xE1 && data[7] == (byte)0x03 && byte8 == (byte)0x02) {
                commandType = CMD_RECONNECT_RISE;
                commandName = "重联上升";
            }
        }
        
        return new ParseResult(crcValid, commandType, commandName, null, hexString);
    }
    
    /**
     * Get predefined test commands
     * 获取预定义的测试命令
     * 
     * @return array of test command hex strings
     */
    public static String[] getTestCommands() {
        return new String[]{
            "5A027E000408F1030201 80A56DFC",  // 全部上升
            "5A027E000408F1030101 7FA5D9BD",  // 全部下降
            "5A027E000408F1030301 81A501FC",  // 全部停止
            "5A027E000408D1030201 60A5CDB2",  // 短编上升
            "5A027E000408E1030201 70A5FDBA"   // 重联上升
        };
    }
    
    /**
     * Get command names for test commands
     * 
     * @return array of command names
     */
    public static String[] getCommandNames() {
        return new String[]{
            "全部上升",
            "全部下降",
            "全部停止",
            "短编上升",
            "重联上升"
        };
    }
}
