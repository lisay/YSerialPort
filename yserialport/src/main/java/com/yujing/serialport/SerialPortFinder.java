/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yujing.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

/**
 * 查找串口设备
 */
public class SerialPortFinder {
    private static final String TAG = "SerialPort";
    private Vector<Driver> mDrivers = null;

    Vector<Driver> getDrivers() throws IOException {
        if (mDrivers == null) {
            mDrivers = new Vector<>();
            try (LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"))) {
                String l;
                while ((l = r.readLine()) != null) {
                    // 由于驱动程序名可能包含空格，我们不使用split（）提取驱动程序名
                    // 修复: 不再使用固定长度0x15(21字节)来提取驱动名称
                    String[] w = l.split(" +");
                    if ((w.length >= 5) && (w[w.length - 1].equals("serial"))) {
                        // 驱动名称是第一个字段
                        String driverName = w[0];
                        String deviceRoot = w[w.length - 4];
                        Log.d(TAG, "Found new driver " + driverName + " on " + deviceRoot);
                        mDrivers.add(new Driver(driverName, deviceRoot));
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, "无法读取 /proc/tty/drivers，将使用直接扫描方式", e);
                throw e;
            }
        }
        return mDrivers;
    }

    /**
     * 获取全部串口设备
     *
     * @return String[]
     */
    public String[] getAllDevices() {
        Vector<String> devices = new Vector<>();
        Iterator<Driver> itDriver;
        try {
            itDriver = getDrivers().iterator();
            while (itDriver.hasNext()) {
                Driver driver = itDriver.next();
                for (File file : driver.getDevices()) {
                    String device = file.getName();
                    String value = String.format("%s (%s)", device, driver.getName());
                    devices.add(value);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "无法从 /proc/tty/drivers 获取串口，尝试直接扫描", e);
            // 备用方案：直接扫描/dev目录查找串口设备
            Vector<String> paths = scanDevicesDirectly();
            for (String path : paths) {
                String device = new File(path).getName();
                devices.add(device + " (直接扫描)");
            }
        }
        
        // 如果通过驱动方式没找到设备，也尝试直接扫描
        if (devices.isEmpty()) {
            Log.i(TAG, "未找到串口设备，尝试直接扫描 /dev 目录");
            Vector<String> paths = scanDevicesDirectly();
            for (String path : paths) {
                String device = new File(path).getName();
                devices.add(device + " (直接扫描)");
            }
        }
        
        return devices.toArray(new String[0]);
    }

    /**
     * 获取全部设备路径
     *
     * @return String[]
     */
    public String[] getAllDevicesPath() {
        Vector<String> devices = new Vector<>();
        Iterator<Driver> itDriver;
        try {
            itDriver = getDrivers().iterator();
            while (itDriver.hasNext()) {
                Driver driver = itDriver.next();
                for (File file : driver.getDevices()) {
                    String device = file.getAbsolutePath();
                    devices.add(device);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "无法从 /proc/tty/drivers 获取串口，尝试直接扫描", e);
            // 备用方案：直接扫描/dev目录查找串口设备
            devices = scanDevicesDirectly();
        }
        
        // 如果通过驱动方式没找到设备，也尝试直接扫描
        if (devices.isEmpty()) {
            Log.i(TAG, "未找到串口设备，尝试直接扫描 /dev 目录");
            devices = scanDevicesDirectly();
        }
        
        return devices.toArray(new String[0]);
    }

    /**
     * 直接扫描/dev目录查找常见的串口设备
     * 这是当/proc/tty/drivers不可用时的备用方案
     *
     * @return 设备路径列表
     */
    private Vector<String> scanDevicesDirectly() {
        Vector<String> devices = new Vector<>();
        File dev = new File("/dev");
        File[] files = dev.listFiles();
        
        if (files != null) {
            // 常见的串口设备模式
            String[] patterns = {"ttyS", "ttyUSB", "ttyACM", "ttyAMA", "rfcomm", "ttyO"};
            
            for (File file : files) {
                String name = file.getName();
                // 检查文件名是否匹配常见串口模式
                for (String pattern : patterns) {
                    if (name.startsWith(pattern)) {
                        String path = file.getAbsolutePath();
                        devices.add(path);
                        Log.d(TAG, "直接扫描找到设备: " + path);
                        break;
                    }
                }
            }
        } else {
            Log.e(TAG, "无法访问 /dev 目录");
        }
        
        return devices;
    }

    /**
     * 设备
     */
    public static class Driver {
        private String mDriverName;
        private String mDeviceRoot;
        Vector<File> mDevices = null;

        public Driver(String name, String root) {
            mDriverName = name;
            mDeviceRoot = root;
        }

        public Vector<File> getDevices() {
            if (mDevices == null) {
                mDevices = new Vector<>();
                File dev = new File("/dev");
                File[] files = dev.listFiles();
                if (files != null) {
                    int i;
                    for (i = 0; i < files.length; i++) {
                        if (files[i].getAbsolutePath().startsWith(mDeviceRoot)) {
                            Log.d(TAG, "Found new device: " + files[i]);
                            mDevices.add(files[i]);
                        }
                    }
                }
            }
            return mDevices;
        }

        public String getName() {
            return mDriverName;
        }
    }

}
