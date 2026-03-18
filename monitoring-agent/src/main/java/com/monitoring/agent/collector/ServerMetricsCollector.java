package com.monitoring.agent.collector;

import com.monitoring.agent.config.AgentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.VirtualMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器性能指标采集器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerMetricsCollector {

    private final AgentProperties agentProperties;
    private final SystemInfo systemInfo;

    @Autowired
    public ServerMetricsCollector(AgentProperties agentProperties) {
        this.agentProperties = agentProperties;
        this.systemInfo = new SystemInfo();
    }

    /**
     * 采集服务器性能指标
     */
    public Map<String, Object> collect() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            HardwareAbstractionLayer hal = systemInfo.getHardware();
            OperatingSystem os = systemInfo.getOperatingSystem();

            // CPU信息
            metrics.putAll(collectCpuMetrics(hal.getProcessor()));
            
            // 内存信息
            metrics.putAll(collectMemoryMetrics(hal.getMemory()));
            
            // 磁盘信息
            metrics.putAll(collectDiskMetrics(os.getFileSystem()));
            
            // 负载信息
            metrics.putAll(collectLoadMetrics(hal.getProcessor()));
            
            // 进程数
            metrics.put("processCount", os.getProcessCount());

            log.debug("服务器性能指标采集完成");
        } catch (Exception e) {
            log.error("采集服务器性能指标失败", e);
        }

        return metrics;
    }

    /**
     * 采集CPU指标
     */
    private Map<String, Object> collectCpuMetrics(CentralProcessor processor) {
        Map<String, Object> cpuMetrics = new HashMap<>();
        
        long[] oldTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] newTicks = processor.getSystemCpuLoadTicks();
        
        long user = newTicks[CentralProcessor.TickType.USER.getIndex()] 
                   - oldTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = newTicks[CentralProcessor.TickType.NICE.getIndex()] 
                   - oldTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = newTicks[CentralProcessor.TickType.SYSTEM.getIndex()] 
                  - oldTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = newTicks[CentralProcessor.TickType.IDLE.getIndex()] 
                   - oldTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = newTicks[CentralProcessor.TickType.IOWAIT.getIndex()] 
                     - oldTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = newTicks[CentralProcessor.TickType.IRQ.getIndex()] 
                  - oldTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = newTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()] 
                      - oldTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = newTicks[CentralProcessor.TickType.STEAL.getIndex()] 
                    - oldTicks[CentralProcessor.TickType.STEAL.getIndex()];

        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        double cpuUsage = totalCpu > 0 ? 
            (totalCpu - idle) * 100.0 / totalCpu : 0.0;

        cpuMetrics.put("cpuUsage", BigDecimal.valueOf(cpuUsage).setScale(2, RoundingMode.HALF_UP));
        
        return cpuMetrics;
    }

    /**
     * 采集内存指标
     */
    private Map<String, Object> collectMemoryMetrics(GlobalMemory memory) {
        Map<String, Object> memoryMetrics = new HashMap<>();
        
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;
        
        double memoryUsage = total > 0 ? used * 100.0 / total : 0.0;
        
        memoryMetrics.put("memoryTotal", BigDecimal.valueOf(total / 1024.0 / 1024.0).setScale(2, RoundingMode.HALF_UP));
        memoryMetrics.put("memoryUsed", BigDecimal.valueOf(used / 1024.0 / 1024.0).setScale(2, RoundingMode.HALF_UP));
        memoryMetrics.put("memoryUsage", BigDecimal.valueOf(memoryUsage).setScale(2, RoundingMode.HALF_UP));
        
        return memoryMetrics;
    }

    /**
     * 采集磁盘指标
     */
    private Map<String, Object> collectDiskMetrics(FileSystem fileSystem) {
        Map<String, Object> diskMetrics = new HashMap<>();
        
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        long totalSpace = 0;
        long usableSpace = 0;
        
        for (OSFileStore fileStore : fileStores) {
            totalSpace += fileStore.getTotalSpace();
            usableSpace += fileStore.getUsableSpace();
        }
        
        long usedSpace = totalSpace - usableSpace;
        double diskUsage = totalSpace > 0 ? usedSpace * 100.0 / totalSpace : 0.0;
        
        diskMetrics.put("diskTotal", BigDecimal.valueOf(totalSpace / 1024.0 / 1024.0 / 1024.0).setScale(2, RoundingMode.HALF_UP));
        diskMetrics.put("diskUsed", BigDecimal.valueOf(usedSpace / 1024.0 / 1024.0 / 1024.0).setScale(2, RoundingMode.HALF_UP));
        diskMetrics.put("diskUsage", BigDecimal.valueOf(diskUsage).setScale(2, RoundingMode.HALF_UP));
        
        return diskMetrics;
    }

    /**
     * 采集负载指标
     */
    private Map<String, Object> collectLoadMetrics(CentralProcessor processor) {
        Map<String, Object> loadMetrics = new HashMap<>();
        
        double[] loadAverage = processor.getSystemLoadAverage(3);
        
        loadMetrics.put("loadAverage1", BigDecimal.valueOf(loadAverage[0]).setScale(2, RoundingMode.HALF_UP));
        loadMetrics.put("loadAverage5", BigDecimal.valueOf(loadAverage[1]).setScale(2, RoundingMode.HALF_UP));
        loadMetrics.put("loadAverage15", BigDecimal.valueOf(loadAverage[2]).setScale(2, RoundingMode.HALF_UP));
        
        return loadMetrics;
    }

    /**
     * 获取操作系统信息
     */
    public Map<String, String> getOsInfo() {
        Map<String, String> osInfo = new HashMap<>();
        OperatingSystem os = systemInfo.getOperatingSystem();
        
        osInfo.put("osType", os.getFamily());
        osInfo.put("osVersion", os.getVersionInfo().getVersion());
        osInfo.put("cpuCores", String.valueOf(systemInfo.getHardware().getProcessor().getLogicalProcessorCount()));
        osInfo.put("memorySize", String.valueOf(systemInfo.getHardware().getMemory().getTotal() / 1024 / 1024));
        
        return osInfo;
    }
}
