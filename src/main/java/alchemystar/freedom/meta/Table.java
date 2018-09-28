package alchemystar.freedom.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alchemystar.freedom.access.Cursor;
import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.index.Index;
import alchemystar.freedom.optimizer.Optimizer;
import alchemystar.freedom.store.fs.FStore;

/**
 * Table
 *
 * @Author lizhuyang
 */
public class Table {
    // table名称
    private String name;
    // relation包含的元组描述
    private Attribute[] attributes;
    // 属性map
    private Map<String, Integer> attributesMap;
    // 主键属性
    private Attribute primaryAttribute;
    // Relation对应的FilePath
    private String tablePath;
    // Relation对应的metaPath
    private String metaPath;
    // 装载具体数据信息
    private FStore tableStore;
    // 装载原信息
    private FStore metaStore;
    // 主键索引,聚簇索引
    private BaseIndex clusterIndex;
    // second索引 二级索引
    private List<BaseIndex> secondIndexes = new ArrayList<BaseIndex>();

    private Optimizer optimizer;

    public Table() {
        optimizer = new Optimizer(this);
    }

    public Cursor searchEqual(IndexEntry entry) {
        // choose index by entry
        Index chooseIndex = optimizer.chooseIndex(entry);
        return chooseIndex.searchEqual(entry);
    }

    public Cursor searchRange(IndexEntry lowKey, IndexEntry upKey) {
        // choose index by entry
        Index chooseIndex = optimizer.chooseIndex(lowKey);
        return chooseIndex.searchRange(lowKey, upKey);
    }

    // CRUD
    public void insert(IndexEntry entry) {
        // 插入聚集索引
        clusterIndex.insert(entry, true);
        // 二级索引的插入
        for (BaseIndex secondIndex : secondIndexes) {
            secondIndex.insert(entry, false);
        }
    }

    public int getAttributeIndex(String name) {
        return attributesMap.get(name);
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
        attributesMap = new HashMap<String, Integer>();
        for (int i = 0; i < attributes.length; i++) {
            attributesMap.put(attributes[i].getName(), i);
            if (attributes[i].isPrimaryKey()) {
                primaryAttribute = attributes[i];
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BaseIndex getClusterIndex() {
        return clusterIndex;
    }

    public void setClusterIndex(BaseIndex clusterIndex) {
        this.clusterIndex = clusterIndex;
    }

    public List<BaseIndex> getSecondIndexes() {
        return secondIndexes;
    }

    public void setSecondIndexes(List<BaseIndex> secondIndexes) {
        this.secondIndexes = secondIndexes;
    }

    public Attribute getPrimaryAttribute() {
        return primaryAttribute;
    }

    public void setPrimaryAttribute(Attribute primaryAttribute) {
        this.primaryAttribute = primaryAttribute;
    }

    // todo 先不考虑持久化
    public void loadFromDisk() {
        // 先不考虑持久化
    }

    public void flushToDisk() {
        clusterIndex.flushToDisk();
        for (BaseIndex baseIndex : secondIndexes) {
            baseIndex.flushToDisk();
        }
    }
}
