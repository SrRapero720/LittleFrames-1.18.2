package me.srrapero720.creativecore.client.render.face;

import java.util.List;

import team.creative.creativecore.common.util.math.geo.VectorFan;

public abstract class FRenderBoxFace {
    
    public static final FRenderBoxFace RENDER = new FRenderBoxFaceStatic() {
        
        @Override
        public boolean shouldRender() {
            return true;
        }
    };
    
    public static final FRenderBoxFace NOT_RENDER = new FRenderBoxFaceStatic() {
        
        @Override
        public boolean shouldRender() {
            return false;
        }
    };
    
    public abstract boolean shouldRender();
    
    public abstract boolean hasCachedFans();
    
    public abstract List<VectorFan> getCachedFans();
    
    public abstract float getScale();
    
    private static abstract class FRenderBoxFaceStatic extends FRenderBoxFace {
        
        @Override
        public boolean hasCachedFans() {
            return false;
        }
        
        @Override
        public List<VectorFan> getCachedFans() {
            return null;
        }
        
        @Override
        public float getScale() {
            return 1;
        }
        
    }
}