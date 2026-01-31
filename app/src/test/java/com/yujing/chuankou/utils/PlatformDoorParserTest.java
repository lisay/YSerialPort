package com.yujing.chuankou.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for PlatformDoorParser
 */
public class PlatformDoorParserTest {

    @Test
    public void testCRC16Calculation() {
        // Test command: 全部上升 5A 02 7E 00 04 08 F1 03 02 01 80 A5
        // Expected CRC: 6D FC
        byte[] data = new byte[]{
            (byte)0x5A, (byte)0x02, (byte)0x7E, (byte)0x00, 
            (byte)0x04, (byte)0x08, (byte)0xF1, (byte)0x03, 
            (byte)0x02, (byte)0x01, (byte)0x80, (byte)0xA5
        };
        
        byte[] crc = PlatformDoorParser.calculateCRC16(data);
        assertEquals("High byte should be 0x6D", (byte)0x6D, crc[0]);
        assertEquals("Low byte should be 0xFC", (byte)0xFC, crc[1]);
    }

    @Test
    public void testVerifyCRC_AllRise() {
        // 全部上升 5A 02 7E 00 04 08 F1 03 02 01 80 A5 6D FC
        byte[] data = new byte[]{
            (byte)0x5A, (byte)0x02, (byte)0x7E, (byte)0x00, 
            (byte)0x04, (byte)0x08, (byte)0xF1, (byte)0x03, 
            (byte)0x02, (byte)0x01, (byte)0x80, (byte)0xA5,
            (byte)0x6D, (byte)0xFC
        };
        
        assertTrue("CRC should be valid", PlatformDoorParser.verifyCRC(data));
    }

    @Test
    public void testVerifyCRC_AllDescend() {
        // 全部下降 5A 02 7E 00 04 08 F1 03 01 01 7F A5 D9 BD
        byte[] data = new byte[]{
            (byte)0x5A, (byte)0x02, (byte)0x7E, (byte)0x00, 
            (byte)0x04, (byte)0x08, (byte)0xF1, (byte)0x03, 
            (byte)0x01, (byte)0x01, (byte)0x7F, (byte)0xA5,
            (byte)0xD9, (byte)0xBD
        };
        
        assertTrue("CRC should be valid", PlatformDoorParser.verifyCRC(data));
    }

    @Test
    public void testVerifyCRC_AllStop() {
        // 全部停止 5A 02 7E 00 04 08 F1 03 03 01 81 A5 01 FC
        byte[] data = new byte[]{
            (byte)0x5A, (byte)0x02, (byte)0x7E, (byte)0x00, 
            (byte)0x04, (byte)0x08, (byte)0xF1, (byte)0x03, 
            (byte)0x03, (byte)0x01, (byte)0x81, (byte)0xA5,
            (byte)0x01, (byte)0xFC
        };
        
        assertTrue("CRC should be valid", PlatformDoorParser.verifyCRC(data));
    }

    @Test
    public void testVerifyCRC_ShortRise() {
        // 短编上升 5A 02 7E 00 04 08 D1 03 02 01 60 A5 CD B2
        byte[] data = new byte[]{
            (byte)0x5A, (byte)0x02, (byte)0x7E, (byte)0x00, 
            (byte)0x04, (byte)0x08, (byte)0xD1, (byte)0x03, 
            (byte)0x02, (byte)0x01, (byte)0x60, (byte)0xA5,
            (byte)0xCD, (byte)0xB2
        };
        
        assertTrue("CRC should be valid", PlatformDoorParser.verifyCRC(data));
    }

    @Test
    public void testVerifyCRC_ReconnectRise() {
        // 重联上升 5A 02 7E 00 04 08 E1 03 02 01 70 A5 FD BA
        byte[] data = new byte[]{
            (byte)0x5A, (byte)0x02, (byte)0x7E, (byte)0x00, 
            (byte)0x04, (byte)0x08, (byte)0xE1, (byte)0x03, 
            (byte)0x02, (byte)0x01, (byte)0x70, (byte)0xA5,
            (byte)0xFD, (byte)0xBA
        };
        
        assertTrue("CRC should be valid", PlatformDoorParser.verifyCRC(data));
    }

    @Test
    public void testVerifyCRC_InvalidCRC() {
        // Invalid CRC (last two bytes are wrong)
        byte[] data = new byte[]{
            (byte)0x5A, (byte)0x02, (byte)0x7E, (byte)0x00, 
            (byte)0x04, (byte)0x08, (byte)0xF1, (byte)0x03, 
            (byte)0x02, (byte)0x01, (byte)0x80, (byte)0xA5,
            (byte)0x00, (byte)0x00  // Wrong CRC
        };
        
        assertFalse("CRC should be invalid", PlatformDoorParser.verifyCRC(data));
    }

    @Test
    public void testParseCommand_AllRise() {
        String hexString = "5A027E000408F1030201 80A56DFC";
        PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);
        
        assertTrue("Should be valid", result.valid);
        assertEquals("Should be CMD_ALL_RISE", PlatformDoorParser.CMD_ALL_RISE, result.commandType);
        assertEquals("Command name should be '全部上升'", "全部上升", result.commandName);
        assertNull("Error message should be null", result.errorMessage);
    }

    @Test
    public void testParseCommand_AllDescend() {
        String hexString = "5A027E000408F1030101 7FA5D9BD";
        PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);
        
        assertTrue("Should be valid", result.valid);
        assertEquals("Should be CMD_ALL_DESCEND", PlatformDoorParser.CMD_ALL_DESCEND, result.commandType);
        assertEquals("Command name should be '全部下降'", "全部下降", result.commandName);
    }

    @Test
    public void testParseCommand_AllStop() {
        String hexString = "5A027E000408F1030301 81A501FC";
        PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);
        
        assertTrue("Should be valid", result.valid);
        assertEquals("Should be CMD_ALL_STOP", PlatformDoorParser.CMD_ALL_STOP, result.commandType);
        assertEquals("Command name should be '全部停止'", "全部停止", result.commandName);
    }

    @Test
    public void testParseCommand_ShortRise() {
        String hexString = "5A027E000408D1030201 60A5CDB2";
        PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);
        
        assertTrue("Should be valid", result.valid);
        assertEquals("Should be CMD_SHORT_RISE", PlatformDoorParser.CMD_SHORT_RISE, result.commandType);
        assertEquals("Command name should be '短编上升'", "短编上升", result.commandName);
    }

    @Test
    public void testParseCommand_ReconnectRise() {
        String hexString = "5A027E000408E1030201 70A5FDBA";
        PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);
        
        assertTrue("Should be valid", result.valid);
        assertEquals("Should be CMD_RECONNECT_RISE", PlatformDoorParser.CMD_RECONNECT_RISE, result.commandType);
        assertEquals("Command name should be '重联上升'", "重联上升", result.commandName);
    }

    @Test
    public void testParseCommand_InvalidCRC() {
        String hexString = "5A027E000408F1030201 80A50000"; // Wrong CRC
        PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);
        
        assertFalse("Should be invalid", result.valid);
        assertEquals("Should be CMD_UNKNOWN", PlatformDoorParser.CMD_UNKNOWN, result.commandType);
        assertEquals("Error message should indicate CRC failure", "CRC校验失败", result.errorMessage);
    }

    @Test
    public void testParseCommand_UnknownCommandWithValidCRC() {
        // Valid CRC but unknown command pattern (changed byte 6 from F1 to AA)
        // Calculate correct CRC for: 5A 02 7E 00 04 08 AA 03 02 01 80 A5
        String hexString = "5A027E000408AA03020180A546F1";
        PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);
        
        assertTrue("CRC should be valid", result.valid);
        assertEquals("Should be CMD_UNKNOWN", PlatformDoorParser.CMD_UNKNOWN, result.commandType);
        assertEquals("Command name should be '未知命令'", "未知命令", result.commandName);
        assertNotNull("Error message should not be null", result.errorMessage);
        assertEquals("Error message should indicate unknown command", "CRC校验通过但不是已知命令", result.errorMessage);
    }
}
