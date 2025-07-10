package com.xiaohunao.heaven_destiny_moment.common.moment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MomentHistoryManager {
    private static final int MAX_HISTORY_PER_TYPE = 5;

    private final Map<MomentType<?>, LinkedHashSet<MomentRunningRecord>> momentHistoryMap =
        new ConcurrentHashMap<>();
    
    /**
     * 添加新的历史记录
     * @param instance Moment实例
     * @return 创建的记录，如果已存在则返回null
     */
    public MomentRunningRecord addHistory(MomentInstance instance) {
        MomentType<?> type = instance.getType();
        UUID uuid = instance.getID();
        long gameTime = instance.level.getGameTime();

        momentHistoryMap.computeIfAbsent(type, k -> new LinkedHashSet<>());
        LinkedHashSet<MomentRunningRecord> records = momentHistoryMap.get(type);
        
        // 检查是否已存在
        if (findRecordByUUID(records, uuid) != null) {
            return null;
        }

        MomentRunningRecord newRecord = new MomentRunningRecord(type, uuid, gameTime);
        records.add(newRecord);
        
        // 维护最大数量限制
        maintainMaxSize(records);
        
        return newRecord;
    }
    
    /**
     * 设置记录的结束时间
     * @param type Moment类型
     * @param uuid Moment的UUID
     * @param endTime 结束时间，如果为null则使用当前时间
     * @return 是否成功设置
     */
    public boolean setEndTime(MomentType<?> type, UUID uuid, Long endTime) {
        LinkedHashSet<MomentRunningRecord> records = momentHistoryMap.get(type);
        if (records == null) {
            return false;
        }
        
        MomentRunningRecord record = findRecordByUUID(records, uuid);
        if (record == null) {
            return false;
        }
        
        long actualEndTime = endTime != null ? endTime : System.currentTimeMillis();
        record.setEndTime(actualEndTime);
        return true;
    }
    
    /**
     * 设置记录的结束时间（使用当前时间）
     * @param instance Moment实例
     * @return 是否成功设置
     */
    public boolean finishRecord(MomentInstance instance) {
        return setEndTime(instance.getType(), instance.getID(), instance.getLevel().getGameTime());
    }
    
    /**
     * 根据UUID查找记录
     * @param type Moment类型
     * @param uuid Moment的UUID
     * @return 找到的记录，如果不存在则返回null
     */
    public MomentRunningRecord findRecord(MomentType<?> type, UUID uuid) {
        LinkedHashSet<MomentRunningRecord> records = momentHistoryMap.get(type);
        if (records == null) {
            return null;
        }
        return findRecordByUUID(records, uuid);
    }
    
    /**
     * 检查记录是否存在
     * @param type Moment类型
     * @param uuid Moment的UUID
     * @return 记录是否存在
     */
    public boolean hasRecord(MomentType<?> type, UUID uuid) {
        return findRecord(type, uuid) != null;
    }
    

    private MomentRunningRecord findRecordByUUID(Set<MomentRunningRecord> records, UUID uuid) {
        return records.stream()
                .filter(record -> record.uuid.equals(uuid))
                .findFirst()
                .orElse(null);
    }
    
    private void maintainMaxSize(LinkedHashSet<MomentRunningRecord> records) {
        while (records.size() > MAX_HISTORY_PER_TYPE) {
            Iterator<MomentRunningRecord> iterator = records.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }
    
    // 获取指定类型的历史记录
    public List<MomentRunningRecord> getHistory(MomentType<?> type) {
        return new ArrayList<>(momentHistoryMap.getOrDefault(type, new LinkedHashSet<>()));
    }
    
    // 获取指定类型的活跃记录（未结束的）
    public List<MomentRunningRecord> getActiveRecords(MomentType<?> type) {
        return getHistory(type).stream()
                .filter(record -> !record.isFinished())
                .collect(Collectors.toList());
    }
    
    // 获取所有活跃记录
    public Map<MomentType<?>, List<MomentRunningRecord>> getAllActiveRecords() {
        Map<MomentType<?>, List<MomentRunningRecord>> activeMap = new HashMap<>();
        for (Map.Entry<MomentType<?>, LinkedHashSet<MomentRunningRecord>> entry : momentHistoryMap.entrySet()) {
            List<MomentRunningRecord> activeRecords = entry.getValue().stream()
                    .filter(record -> !record.isFinished())
                    .collect(Collectors.toList());
            if (!activeRecords.isEmpty()) {
                activeMap.put(entry.getKey(), activeRecords);
            }
        }
        return activeMap;
    }

    public void clearHistory(MomentType<?> type) {
        momentHistoryMap.remove(type);
    }

    public void clearAllHistory() {
        momentHistoryMap.clear();
    }

    public boolean removeRecord(MomentType<?> type, UUID uuid) {
        LinkedHashSet<MomentRunningRecord> records = momentHistoryMap.get(type);
        if (records == null) {
            return false;
        }
        
        MomentRunningRecord toRemove = findRecordByUUID(records, uuid);
        if (toRemove != null) {
            records.remove(toRemove);
            return true;
        }
        return false;
    }

    public ListTag serializeNBT() {
        ListTag historyList = new ListTag();
        
        for (Map.Entry<MomentType<?>, LinkedHashSet<MomentRunningRecord>> entry : momentHistoryMap.entrySet()) {
            for (MomentRunningRecord record : entry.getValue()) {
                historyList.add(record.serializeNBT());
            }
        }

        return historyList;
    }

    public void deserializeNBT(ListTag history) {
        momentHistoryMap.clear();

        for (int i = 0; i < history.size(); i++) {
            try {
                CompoundTag recordTag = history.getCompound(i);
                MomentRunningRecord record = MomentRunningRecord.deserializeNBT(recordTag);

                momentHistoryMap.computeIfAbsent(record.type, k -> new LinkedHashSet<>())
                        .add(record);
            } catch (Exception e) {
                System.err.println("Failed to deserialize moment record: " + e.getMessage());
            }
        }
    }
}