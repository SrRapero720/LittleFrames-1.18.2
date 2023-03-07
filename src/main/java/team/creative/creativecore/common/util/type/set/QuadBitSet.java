//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.creative.creativecore.common.util.type.set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;

import java.util.Arrays;

public class QuadBitSet {
    private static final int CHUNK_SIZE = 8;
    private long[][] chunks;
    private int minChunkX;
    private int minChunkY;
    private boolean empty = true;
    private int count = 0;

    private static int chunkIndex(int coord) {
        return coord < 0 ? (int)Math.floor((double)coord / 8.0) : coord / 8;
    }

    private static int index(int x, int y) {
        return x * 8 + y;
    }

    public QuadBitSet() {
    }

    public void load(CompoundTag nbt) {
        if (!nbt.contains("info")) {
            clearIncludingSize();
            return;
        }

        int[] info = nbt.getIntArray("info");
        if (info.length != 3)
            throw new IllegalArgumentException("Data is not valid " + nbt);
        this.count = info[0];
        this.minChunkX = info[1];
        this.minChunkY = info[2];
        ListTag list = nbt.getList("data", Tag.TAG_LONG_ARRAY);
        this.chunks = new long[list.size()][];
        for (int i = 0; i < list.size(); i++)
            this.chunks[i] = list.getLongArray(i);
    }

    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        if (count == 0) return nbt;
        nbt.putIntArray("info", new int[] { count, minChunkX, minChunkY });
        ListTag list = new ListTag();
        for (int i = 0; i < chunks.length; i++)
            list.add(new LongArrayTag(Arrays.copyOf(chunks[i], chunks[i].length)));
        nbt.put("data", list);
        return nbt;
    }

    private void init(int x, int y) {
        int chunkX = chunkIndex(x);
        int chunkY = chunkIndex(y);
        this.minChunkX = chunkX;
        this.minChunkY = chunkY;
        this.chunks = new long[1][1];
        this.empty = true;
    }

    private void ensureCapacity(int x, int y) {
        if (this.empty) {
            this.init(x, y);
        } else {
            int chunkX = chunkIndex(x);
            int chunkY = chunkIndex(y);
            int additional;
            if (chunkX >= this.minChunkX) {
                if (chunkX >= this.minChunkX + this.chunks.length) {
                    this.chunks = (long[][])Arrays.copyOf(this.chunks, this.chunks.length + chunkX - (this.minChunkX + this.chunks.length) + 1);
                }
            } else {
                additional = this.minChunkX - chunkX;
                long[][] newChunks = new long[additional + this.chunks.length][];

                for(int i = 0; i < this.chunks.length; ++i) {
                    newChunks[additional + i] = this.chunks[i];
                }

                this.chunks = newChunks;
            }

            if (chunkY < this.minChunkY) {
                additional = this.minChunkY - chunkY;

                for(int xIndex = 0; xIndex < this.chunks.length; ++xIndex) {
                    long[] yChunks = this.chunks[xIndex];
                    long[] newChunks = new long[additional + yChunks.length];

                    for(int i = 0; i < yChunks.length; ++i) {
                        newChunks[additional + i] = yChunks[i];
                    }

                    this.chunks[xIndex] = newChunks;
                }
            } else {
                additional = chunkX - this.minChunkX;
                if (this.chunks[additional] == null || chunkY >= this.chunks[additional].length) {
                    this.chunks[additional] = Arrays.copyOf(this.chunks[additional], this.chunks[additional].length + chunkY - (this.minChunkY + this.chunks[additional].length) + 1);
                }
            }
        }

    }

    public void flip(int x, int y) {
        this.ensureCapacity(x, y);
        int chunkX = chunkIndex(x);
        int chunkY = chunkIndex(y);
        int inChunkX = x % 8;
        int inChunkY = y % 8;
        int xOffset = chunkX - this.minChunkX;
        int yOffset = chunkY - this.minChunkY;
        long[] var10000 = this.chunks[xOffset];
        var10000[yOffset] ^= 1L << index(inChunkX, inChunkY);
    }

    public void set(int x, int y) {
        this.ensureCapacity(x, y);
        int chunkX = chunkIndex(x);
        int chunkY = chunkIndex(y);
        int inChunkX = x % 8;
        int inChunkY = y % 8;
        int xOffset = chunkX - this.minChunkX;
        int yOffset = chunkY - this.minChunkY;
        long[] var10000 = this.chunks[xOffset];
        var10000[yOffset] |= 1L << index(inChunkX, inChunkY);
    }

    public void set(int x, int y, boolean value) {
        if (value) {
            this.set(x, y);
        } else {
            this.clear(x, y);
        }

    }

    public void clear(int x, int y) {
        this.ensureCapacity(x, y);
        int chunkX = chunkIndex(x);
        int chunkY = chunkIndex(y);
        int inChunkX = x % 8;
        int inChunkY = y % 8;
        int xOffset = chunkX - this.minChunkX;
        int yOffset = chunkY - this.minChunkY;
        long[] var10000 = this.chunks[xOffset];
        var10000[yOffset] &= ~(1L << index(inChunkX, inChunkY));
    }

    public void clear() {
        this.empty = true;
        this.chunks = null;
        this.minChunkX = 0;
        this.minChunkY = 0;
    }

    public boolean get(int x, int y) {
        int chunkX = chunkIndex(x);
        if (this.chunks == null) {
            return false;
        } else if (chunkX >= this.minChunkX && chunkX < this.minChunkX + this.chunks.length) {
            int xOffset = chunkX - this.minChunkX;
            int chunkY = chunkIndex(y);
            if (chunkY >= this.minChunkY && chunkY < this.minChunkY + this.chunks[xOffset].length) {
                int inChunkX = x % 8;
                int inChunkY = y % 8;
                int yOffset = chunkY - this.minChunkY;
                return (this.chunks[xOffset][yOffset] & 1L << index(inChunkX, inChunkY)) != 0L;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder("{");
        boolean first = true;
        if (this.chunks != null) {
            for(int i = 0; i < this.chunks.length; ++i) {
                for(int j = 0; j < this.chunks[i].length; ++j) {
                    long word = this.chunks[i][j];
                    if (word != 0L) {
                        if (first) {
                            first = false;
                        } else {
                            result.append(", ");
                        }

                        for(int k = 0; k < 64; ++k) {
                            long data = word & (long)(-1 << k);
                            if (data != 0L) {
                                int x = (this.minChunkX + i) * 8 + k / 8;
                                int y = (this.minChunkY + j) * 8 + k % 8;
                                result.append("(" + x + ", " + y + ")");
                            }
                        }
                    }
                }
            }
        }

        result.append("}");
        return result.toString();
    }

    public void clearIncludingSize() {
        count = 0;
        chunks = null;
        minChunkX = 0;
        minChunkY = 0;
    }
}
