package com.yujing.chuankou.activity;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.yujing.chuankou.R;
import com.yujing.chuankou.base.KBaseActivity;
import com.yujing.chuankou.config.Config;
import com.yujing.chuankou.databinding.ActivityPlatformDoorBinding;
import com.yujing.chuankou.utils.PlatformDoorParser;
import com.yujing.chuankou.utils.Setting;
import com.yujing.utils.YConvert;
import com.yujing.utils.YLog;
import com.yujing.utils.YToast;
import com.yujing.yserialport.DataListener;
import com.yujing.yserialport.ThreadMode;
import com.yujing.yserialport.YSerialPort;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Platform Door Debug Assistant
 * 站台门调试助手
 *
 * @author yujing
 */
public class PlatformDoorActivity extends KBaseActivity<ActivityPlatformDoorBinding> {
    YSerialPort ySerialPort;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[HH:mm:ss.SSS]", Locale.getDefault());

    public PlatformDoorActivity() {
        super(R.layout.activity_platform_door);
    }

    @Override
    protected void init() {
        //退出
        binding.rlBack.setOnClickListener(v -> finish());
        //清空接收数据
        binding.tvClearReceive.setOnClickListener(v -> binding.tvReceive.setText(""));
        //清空发送日志
        binding.tvClearSend.setOnClickListener(v -> binding.tvSend.setText(""));

        //初始化串口
        ySerialPort = new YSerialPort(this, Config.getDevice(), Config.getBaudRate());
        //添加监听
        ySerialPort.addDataListener(dataListener);
        ySerialPort.setThreadMode(ThreadMode.MAIN);//设置回调线程为主线程
        if (Config.getDevice() != null && Config.getBaudRate() != null)
            ySerialPort.start();

        //设置
        Setting.setting(this, binding.includeSet, () -> {
            if (Config.getDevice() != null && Config.getBaudRate() != null)
                ySerialPort.reStart(Config.getDevice(), Config.getBaudRate());
            binding.tvReceive.setText("");
            binding.tvSend.setText("");
        });

        //测试命令按钮
        String[] testCommands = PlatformDoorParser.getTestCommands();
        binding.btnAllRise.setOnClickListener(v -> sendCommand(testCommands[0], "全部上升"));
        binding.btnAllDescend.setOnClickListener(v -> sendCommand(testCommands[1], "全部下降"));
        binding.btnAllStop.setOnClickListener(v -> sendCommand(testCommands[2], "全部停止"));
        binding.btnShortRise.setOnClickListener(v -> sendCommand(testCommands[3], "短编上升"));
        binding.btnReconnectRise.setOnClickListener(v -> sendCommand(testCommands[4], "重联上升"));

        //自定义命令发送
        binding.btnSendCustom.setOnClickListener(v -> sendCustomCommand());
    }

    private void sendCommand(String hexCommand, String commandName) {
        String hexString = hexCommand.replace(" ", "");
        YLog.i("发送命令：" + commandName + " - " + hexString);

        try {
            byte[] bytes = YConvert.hexStringToByte(hexString);
            ySerialPort.send(bytes);

            //显示发送日志
            if (binding.tvSend.getText().toString().length() > 10000)
                binding.tvSend.setText(binding.tvSend.getText().toString().substring(0, 2000));
            binding.tvSend.setText(simpleDateFormat.format(new Date()) + " 发送[" + commandName + "]：" + hexString + "\n" + binding.tvSend.getText().toString());
        } catch (Exception e) {
            YToast.show("发送失败：" + e.getMessage());
            YLog.e("发送命令失败", e);
        }
    }

    private void sendCustomCommand() {
        String hexString = binding.etCustomCommand.getText().toString().replace("\n", "").replace(" ", "");
        if (hexString.isEmpty()) {
            YToast.show("请输入命令！");
            return;
        }

        //先验证命令
        PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);
        if (!result.valid) {
            YToast.show("命令验证失败：" + result.errorMessage);
            return;
        }

        YLog.i("发送自定义命令：" + hexString);

        try {
            byte[] bytes = YConvert.hexStringToByte(hexString);
            ySerialPort.send(bytes);

            //显示发送日志
            if (binding.tvSend.getText().toString().length() > 10000)
                binding.tvSend.setText(binding.tvSend.getText().toString().substring(0, 2000));
            binding.tvSend.setText(simpleDateFormat.format(new Date()) + " 发送[" + result.commandName + "]：" + hexString + "\n" + binding.tvSend.getText().toString());
        } catch (Exception e) {
            YToast.show("发送失败：" + e.getMessage());
            YLog.e("发送命令失败", e);
        }
    }

    DataListener dataListener = (hexString, bytes) -> {
        //解析接收到的数据
        PlatformDoorParser.ParseResult result = PlatformDoorParser.parseCommand(hexString);

        //构建显示文本
        StringBuilder sb = new StringBuilder();
        sb.append(simpleDateFormat.format(new Date())).append(" 接收：").append(hexString).append("\n");
        sb.append("  ├─ 命令：").append(result.commandName).append("\n");
        sb.append("  ├─ CRC校验：");

        String displayText = sb.toString();
        int crcStatusStart = displayText.length();
        
        String statusText;
        int statusColor;
        if (result.valid) {
            statusText = "通过 ✓";
            statusColor = 0xFF4CAF50;  // Green - 正常数据
        } else {
            statusText = "失败 ✗";
            statusColor = 0xFFF44336;  // Red - 异常数据
        }
        
        displayText += statusText + "\n";
        int crcStatusEnd = crcStatusStart + statusText.length();
        
        // Show error message if present (either CRC failed or unknown command)
        if (result.errorMessage != null) {
            displayText += "  └─ 错误：" + result.errorMessage + "\n";
        } else if (result.valid && result.commandType != PlatformDoorParser.CMD_UNKNOWN) {
            // Known command with valid CRC - show success
            displayText += "  └─ 状态：解析成功\n";
        }
        displayText += "\n";

        //使用SpannableString为整个数据块添加颜色（异常数据标记为红色，正常数据标记为绿色）
        SpannableString spannableString = new SpannableString(displayText);
        spannableString.setSpan(new ForegroundColorSpan(statusColor),
                0, displayText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //显示接收数据
        if (binding.tvReceive.getText().toString().length() > 10000)
            binding.tvReceive.setText(binding.tvReceive.getText().toString().substring(0, 2000));
        binding.tvReceive.setText(spannableString + binding.tvReceive.getText().toString());
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        ySerialPort.onDestroy();
    }
}
