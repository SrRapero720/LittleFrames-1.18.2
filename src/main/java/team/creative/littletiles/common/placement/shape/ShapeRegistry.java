package team.creative.littletiles.common.placement.shape;

import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.common.placement.shape.type.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShapeRegistry {
    
    public static final LittleShape TILE_SHAPE = new LittleShapeTile();
    public static final LittleShape DEFAULT_SHAPE = new LittleShapeBox();
    public static final NamedHandlerRegistry<LittleShape> REGISTRY = new NamedHandlerRegistry<LittleShape>(DEFAULT_SHAPE);
    private static final HashMapList<ShapeType, String> SHAPE_TYPES = new HashMapList<>();
    private static final List<LittleShape> NO_TILE_LIST = new ArrayList<>();
    private static final List<LittleShape> PLACING_LIST = new ArrayList<>();
    
    public static Collection<LittleShape> notTileShapes() {
        return NO_TILE_LIST;
    }
    
    public static Collection<LittleShape> placingShapes() {
        return PLACING_LIST;
    }
    
    public static LittleShape registerShape(String id, LittleShape shape, ShapeType type) {
        REGISTRY.register(id, shape);
        SHAPE_TYPES.add(type, id);
        if (type != ShapeType.DEFAULT_SELECTOR)
            NO_TILE_LIST.add(shape);
        if (type == ShapeType.SELECTOR || type == ShapeType.SHAPE || type == ShapeType.DEFAULT_SELECTOR)
            PLACING_LIST.add(shape);
        return shape;
    }
    
    public static LittleShape get(String name) {
        return REGISTRY.get(name);
    }
    
    static {
        registerShape("tile", TILE_SHAPE, ShapeType.DEFAULT_SELECTOR);
        registerShape("type", new LittleShapeType(), ShapeType.SELECTOR);
        registerShape("box", DEFAULT_SHAPE, ShapeType.SHAPE);
        registerShape("connected", new LittleShapeConnected(), ShapeType.SELECTOR);
        
        registerShape("polygon", new LittleShapePolygon(), ShapeType.SHAPE);
        
    }
    
    public static enum ShapeType {
        
        DEFAULT_SELECTOR,
        SELECTOR,
        SHAPE
        
    }
    
}
