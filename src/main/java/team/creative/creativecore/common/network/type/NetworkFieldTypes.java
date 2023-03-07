//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.creative.creativecore.common.network.type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.DataInput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.math.vec.Vec1f;
import team.creative.creativecore.common.util.math.vec.Vec2d;
import team.creative.creativecore.common.util.math.vec.Vec2f;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.registry.exception.RegistryException;
import team.creative.creativecore.common.util.text.AdvancedComponent;
import team.creative.creativecore.common.util.type.Bunch;

public class NetworkFieldTypes {
    private static final Gson GSON = new Gson();
    private static final List<NetworkFieldTypeSpecial> specialParsers = new ArrayList();
    private static final HashMap<Class, NetworkFieldTypeClass> parsers = new HashMap();

    public NetworkFieldTypes() {
    }

    public static <T> void register(NetworkFieldTypeClass<T> parser, Class<T> classType) {
        parsers.put(classType, parser);
    }

    public static <T> void register(NetworkFieldTypeClass<T> parser, Class<? extends T>... classType) {
        Class[] var2 = classType;
        int var3 = classType.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Class<? extends T> clazz = var2[var4];
            parsers.put(clazz, parser);
        }

    }

    public static <T> void register(NetworkFieldTypeSpecial parser) {
        specialParsers.add(parser);
    }

    public static NetworkFieldType get(Field field) {
        return get(field.getType(), field.getGenericType());
    }

    public static <T> NetworkFieldType<T> get(Class<T> classType) {
        try {
            NetworkFieldType parser = (NetworkFieldType)parsers.get(classType);
            if (parser != null) {
                return parser;
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        throw new RuntimeException("No field type found for " + classType.getSimpleName());
    }

    public static NetworkFieldType get(Class classType, Type genericType) {
        try {
            NetworkFieldType parser = (NetworkFieldType)parsers.get(classType);
            if (parser != null) {
                return parser;
            }

            for(int i = 0; i < specialParsers.size(); ++i) {
                if (((NetworkFieldTypeSpecial)specialParsers.get(i)).predicate.test(classType, genericType)) {
                    return (NetworkFieldType)specialParsers.get(i);
                }
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        throw new RuntimeException("No field type found for " + classType.getSimpleName());
    }

    public static <T> void write(Class<T> clazz, T object, FriendlyByteBuf buffer) {
        get(clazz).write(object, clazz, (Type)null, buffer);
    }

    public static <T> void writeMany(Class<T> clazz, Bunch<T> bunch, FriendlyByteBuf buffer) {
        buffer.writeInt(bunch.size());
        NetworkFieldType<T> type = get(clazz);
        for (T t : bunch)
            type.write(t, clazz, null, buffer);
    }

    public static <T> void writeMany(Class<T> clazz, Collection<T> collection, FriendlyByteBuf buffer) {
        buffer.writeInt(collection.size());
        NetworkFieldType<T> type = get(clazz);
        for (T t : collection)
            type.write(t, clazz, null, buffer);
    }

    public static <T> void writeMany(Class<T> clazz, T[] collection, FriendlyByteBuf buffer) {
        buffer.writeInt(collection.length);
        NetworkFieldType<T> type = get(clazz);
        for (T t : collection)
            type.write(t, clazz, null, buffer);
    }

    public static <T> T read(Class<T> clazz, FriendlyByteBuf buffer) {
        return get(clazz).read(clazz, (Type)null, buffer);
    }

    public static <T> Iterable<T> readMany(Class<T> clazz, FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        NetworkFieldType<T> type = get(clazz);

        return () -> new Iterator<T>() {

            int index = 0;

            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public T next() {
                index++;
                return type.read(clazz, null, buffer);
            }

        };
    }

    static {
        register(new NetworkFieldTypeClass<Boolean>() {
            protected void writeContent(Boolean content, FriendlyByteBuf buffer) {
                buffer.writeBoolean(content);
            }

            protected Boolean readContent(FriendlyByteBuf buffer) {
                return buffer.readBoolean();
            }
        }, Boolean.TYPE, Boolean.class);
        register(new NetworkFieldTypeClass<Byte>() {
            protected void writeContent(Byte content, FriendlyByteBuf buffer) {
                buffer.writeByte(content);
            }

            protected Byte readContent(FriendlyByteBuf buffer) {
                return buffer.readByte();
            }
        }, Byte.TYPE, Byte.class);
        register(new NetworkFieldTypeClass<Short>() {
            protected void writeContent(Short content, FriendlyByteBuf buffer) {
                buffer.writeShort(content);
            }

            protected Short readContent(FriendlyByteBuf buffer) {
                return buffer.readShort();
            }
        }, Short.TYPE, Short.class);
        register(new NetworkFieldTypeClass<Integer>() {
            protected void writeContent(Integer content, FriendlyByteBuf buffer) {
                buffer.writeInt(content);
            }

            protected Integer readContent(FriendlyByteBuf buffer) {
                return buffer.readInt();
            }
        }, Integer.TYPE, Integer.class);
        register(new NetworkFieldTypeClass<Long>() {
            protected void writeContent(Long content, FriendlyByteBuf buffer) {
                buffer.writeLong(content);
            }

            protected Long readContent(FriendlyByteBuf buffer) {
                return buffer.readLong();
            }
        }, Long.TYPE, Long.class);
        register(new NetworkFieldTypeClass<Float>() {
            protected void writeContent(Float content, FriendlyByteBuf buffer) {
                buffer.writeFloat(content);
            }

            protected Float readContent(FriendlyByteBuf buffer) {
                return buffer.readFloat();
            }
        }, Float.TYPE, Float.class);
        register(new NetworkFieldTypeClass<Double>() {
            protected void writeContent(Double content, FriendlyByteBuf buffer) {
                buffer.writeDouble(content);
            }

            protected Double readContent(FriendlyByteBuf buffer) {
                return buffer.readDouble();
            }
        }, Double.TYPE, Double.class);
        register(new NetworkFieldTypeClass<BlockPos>() {
            protected void writeContent(BlockPos content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content);
            }

            protected BlockPos readContent(FriendlyByteBuf buffer) {
                return buffer.readBlockPos();
            }
        }, BlockPos.class);
        register(new NetworkFieldTypeClass<String>() {
            protected void writeContent(String content, FriendlyByteBuf buffer) {
                buffer.writeUtf(content);
            }

            protected String readContent(FriendlyByteBuf buffer) {
                return buffer.readUtf(32767);
            }
        }, String.class);
        register(new NetworkFieldTypeClass<Component>() {
            protected void writeContent(Component content, FriendlyByteBuf buffer) {
                buffer.writeComponent(content);
            }

            protected Component readContent(FriendlyByteBuf buffer) {
                return buffer.readComponent();
            }
        }, Component.class);
        register(new NetworkFieldTypeClass<CompoundTag>() {
            protected void writeContent(CompoundTag content, FriendlyByteBuf buffer) {
                buffer.writeNbt(content);
            }

            protected CompoundTag readContent(FriendlyByteBuf buffer) {
                return buffer.readNbt();
            }
        }, CompoundTag.class);
        register(new NetworkFieldTypeClass<ItemStack>() {
            protected void writeContent(ItemStack content, FriendlyByteBuf buffer) {
                buffer.writeItem(content);
            }

            protected ItemStack readContent(FriendlyByteBuf buffer) {
                return buffer.readItem();
            }
        }, ItemStack.class);
        register(new NetworkFieldTypeClass<ResourceLocation>() {
            protected void writeContent(ResourceLocation content, FriendlyByteBuf buffer) {
                buffer.writeResourceLocation(content);
            }

            protected ResourceLocation readContent(FriendlyByteBuf buffer) {
                return buffer.readResourceLocation();
            }
        }, ResourceLocation.class);
        register(new NetworkFieldTypeClass<BlockState>() {
            protected void writeContent(BlockState content, FriendlyByteBuf buffer) {
                buffer.writeInt(Block.getId(content));
            }

            protected BlockState readContent(FriendlyByteBuf buffer) {
                return Block.stateById(buffer.readInt());
            }
        }, BlockState.class);
        register(new NetworkFieldTypeClass<Block>() {
            protected void writeContent(Block content, FriendlyByteBuf buffer) {
                buffer.writeResourceLocation(content.getRegistryName());
            }

            protected Block readContent(FriendlyByteBuf buffer) {
                return (Block)ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
            }
        }, Block.class);
        register(new NetworkFieldTypeClass<Item>() {
            protected void writeContent(Item content, FriendlyByteBuf buffer) {
                buffer.writeResourceLocation(content.getRegistryName());
            }

            protected Item readContent(FriendlyByteBuf buffer) {
                return (Item)ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
            }
        }, Item.class);
        register(new NetworkFieldTypeClass<Vector3d>() {
            protected void writeContent(Vector3d content, FriendlyByteBuf buffer) {
                buffer.writeDouble(content.x);
                buffer.writeDouble(content.y);
                buffer.writeDouble(content.z);
            }

            protected Vector3d readContent(FriendlyByteBuf buffer) {
                return new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            }
        }, Vector3d.class);
        register(new NetworkFieldTypeClass<Vec3>() {
            protected void writeContent(Vec3 content, FriendlyByteBuf buffer) {
                buffer.writeDouble(content.x);
                buffer.writeDouble(content.y);
                buffer.writeDouble(content.z);
            }

            protected Vec3 readContent(FriendlyByteBuf buffer) {
                return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            }
        }, Vec3.class);
        register(new NetworkFieldTypeClass<Vec1d>() {
            protected void writeContent(Vec1d content, FriendlyByteBuf buffer) {
                buffer.writeDouble(content.x);
            }

            protected Vec1d readContent(FriendlyByteBuf buffer) {
                return new Vec1d(buffer.readDouble());
            }
        }, Vec1d.class);
        register(new NetworkFieldTypeClass<Vec1f>() {
            protected void writeContent(Vec1f content, FriendlyByteBuf buffer) {
                buffer.writeFloat(content.x);
            }

            protected Vec1f readContent(FriendlyByteBuf buffer) {
                return new Vec1f(buffer.readFloat());
            }
        }, Vec1f.class);
        register(new NetworkFieldTypeClass<Vec2d>() {
            protected void writeContent(Vec2d content, FriendlyByteBuf buffer) {
                buffer.writeDouble(content.x);
                buffer.writeDouble(content.y);
            }

            protected Vec2d readContent(FriendlyByteBuf buffer) {
                return new Vec2d(buffer.readDouble(), buffer.readDouble());
            }
        }, Vec2d.class);
        register(new NetworkFieldTypeClass<Vec2f>() {
            protected void writeContent(Vec2f content, FriendlyByteBuf buffer) {
                buffer.writeFloat(content.x);
                buffer.writeFloat(content.y);
            }

            protected Vec2f readContent(FriendlyByteBuf buffer) {
                return new Vec2f(buffer.readFloat(), buffer.readFloat());
            }
        }, Vec2f.class);
        register(new NetworkFieldTypeClass<Vec3d>() {
            protected void writeContent(Vec3d content, FriendlyByteBuf buffer) {
                buffer.writeDouble(content.x);
                buffer.writeDouble(content.y);
                buffer.writeDouble(content.z);
            }

            protected Vec3d readContent(FriendlyByteBuf buffer) {
                return new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            }
        }, Vec3d.class);
        register(new NetworkFieldTypeClass<Vec3f>() {
            protected void writeContent(Vec3f content, FriendlyByteBuf buffer) {
                buffer.writeFloat(content.x);
                buffer.writeFloat(content.y);
                buffer.writeFloat(content.z);
            }

            protected Vec3f readContent(FriendlyByteBuf buffer) {
                return new Vec3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
            }
        }, Vec3f.class);
        register(new NetworkFieldTypeClass<Vector3f>() {
            protected void writeContent(Vector3f content, FriendlyByteBuf buffer) {
                buffer.writeFloat(content.x());
                buffer.writeFloat(content.y());
                buffer.writeFloat(content.z());
            }

            protected Vector3f readContent(FriendlyByteBuf buffer) {
                return new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
            }
        }, Vector3f.class);
        register(new NetworkFieldTypeClass<UUID>() {
            protected void writeContent(UUID content, FriendlyByteBuf buffer) {
                buffer.writeUtf(content.toString());
            }

            protected UUID readContent(FriendlyByteBuf buffer) {
                return UUID.fromString(buffer.readUtf(32767));
            }
        }, UUID.class);
        register(new NetworkFieldTypeClass<JsonObject>() {
            protected void writeContent(JsonObject content, FriendlyByteBuf buffer) {
                buffer.writeUtf(content.toString());
            }

            protected JsonObject readContent(FriendlyByteBuf buffer) {
                return (JsonObject)NetworkFieldTypes.GSON.fromJson(buffer.readUtf(32767), JsonObject.class);
            }
        }, JsonObject.class);
        register(new NetworkFieldTypeSpecial((x, y) -> {
            return x.isArray();
        }) {
            public void write(Object content, Class classType, Type genericType, FriendlyByteBuf buffer) {
                Class subClass = classType.getComponentType();
                NetworkFieldType subParser = NetworkFieldTypes.get(subClass, (Type)null);
                int length = Array.getLength(content);
                buffer.writeInt(length);

                for(int i = 0; i < length; ++i) {
                    subParser.write(Array.get(content, i), subClass, (Type)null, buffer);
                }

            }

            public Object read(Class classType, Type genericType, FriendlyByteBuf buffer) {
                int length = buffer.readInt();
                Class subClass = classType.getComponentType();
                NetworkFieldType subParser = NetworkFieldTypes.get(subClass, (Type)null);
                Object object = Array.newInstance(subClass, length);

                for(int i = 0; i < length; ++i) {
                    Array.set(object, i, subParser.read(subClass, (Type)null, buffer));
                }

                return object;
            }
        });
        register(new NetworkFieldTypeSpecial((x, y) -> {
            return x.equals(ArrayList.class) || x.equals(List.class);
        }) {
            public void write(Object content, Class classType, Type genericType, FriendlyByteBuf buffer) {
                if (!(genericType instanceof ParameterizedType)) {
                    throw new RuntimeException("Missing generic type");
                } else {
                    Type[] types = ((ParameterizedType)genericType).getActualTypeArguments();
                    if (types.length != 1) {
                        throw new RuntimeException("Invalid generic type");
                    } else {
                        Class subClass = (Class)((ParameterizedType)genericType).getActualTypeArguments()[0];
                        Type subType = types[0];
                        NetworkFieldType subParser = NetworkFieldTypes.get(subClass, subType);
                        if (subParser == null) {
                            throw new RuntimeException("Invalid class type " + classType.getComponentType().getName());
                        } else {
                            int length = ((List)content).size();
                            buffer.writeInt(length);

                            for(int i = 0; i < length; ++i) {
                                subParser.write(((List)content).get(i), subClass, subType, buffer);
                            }

                        }
                    }
                }
            }

            public Object read(Class classType, Type genericType, FriendlyByteBuf buffer) {
                if (!(genericType instanceof ParameterizedType)) {
                    throw new RuntimeException("Missing generic type");
                } else {
                    Type[] types = ((ParameterizedType)genericType).getActualTypeArguments();
                    if (types.length != 1) {
                        throw new RuntimeException("Invalid generic type");
                    } else {
                        Class subClass = (Class)((ParameterizedType)genericType).getActualTypeArguments()[0];
                        Type subType = types[0];
                        NetworkFieldType subParser = NetworkFieldTypes.get(subClass, subType);
                        if (subParser == null) {
                            throw new RuntimeException("Invalid class type " + classType.getComponentType().getName());
                        } else {
                            int length = buffer.readInt();
                            List list = new ArrayList(length);

                            for(int j = 0; j < length; ++j) {
                                list.add(subParser.read(subClass, subType, buffer));
                            }

                            return list;
                        }
                    }
                }
            }
        });
        register(new NetworkFieldTypeSpecial((x, y) -> {
            return x.isEnum();
        }) {
            public void write(Object content, Class classType, Type genericType, FriendlyByteBuf buffer) {
                buffer.writeEnum((Enum)content);
            }

            public Object read(Class classType, Type genericType, FriendlyByteBuf buffer) {
                return buffer.readEnum(classType);
            }
        });
        register(new NetworkFieldTypeClass<Filter>() {
            protected void writeContent(Filter content, FriendlyByteBuf buffer) {
                try {
                    buffer.writeNbt(Filter.SERIALIZER.write(content));
                } catch (RegistryException var4) {
                    var4.printStackTrace();
                }

            }

            protected Filter readContent(FriendlyByteBuf buffer) {
                try {
                    return Filter.SERIALIZER.read(buffer.readAnySizeNbt());
                } catch (RegistryException var3) {
                    var3.printStackTrace();
                    return Filter.or(new Filter[0]);
                }
            }
        }, Filter.class);
        register(new NetworkFieldTypeClass<>() {
            protected void writeContent(BiFilter content, FriendlyByteBuf buffer) {
                try {
                    buffer.writeNbt(BiFilter.SERIALIZER.write(content));
                } catch (RegistryException var4) {
                    var4.printStackTrace();
                }

            }

            protected BiFilter readContent(FriendlyByteBuf buffer) {
                try {
                    return BiFilter.SERIALIZER.read(buffer.readAnySizeNbt());
                } catch (RegistryException var3) {
                    var3.printStackTrace();
                    return BiFilter.or(new BiFilter[0]);
                }
            }
        }, BiFilter.class);
        register(new NetworkFieldTypeClass<>() {
            private static final Gson GSON = Util.make(() -> {
                GsonBuilder gsonbuilder = new GsonBuilder();
                gsonbuilder.disableHtmlEscaping();
                gsonbuilder.registerTypeHierarchyAdapter(Component.class, new AdvancedComponent.Serializer());
                gsonbuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
                gsonbuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
                return gsonbuilder.create();
            });

            protected void writeContent(Component content, FriendlyByteBuf buffer) {
                buffer.writeUtf(GSON.toJson(content));
            }

            protected Component readContent(FriendlyByteBuf buffer) {
                return GsonHelper.fromJson(GSON, buffer.readUtf(), MutableComponent.class, false);
            }
        }, Component.class);
        register(new NetworkFieldTypeClass<>() {
            protected void writeContent(Tag content, FriendlyByteBuf buffer) {
                buffer.writeByte(content.getId());
                if (content.getId() != 0) {
                    try {
                        content.write(new ByteBufOutputStream(buffer));
                    } catch (IOException var4) {
                    }
                }

            }

            protected Tag readContent(FriendlyByteBuf buffer) {
                DataInput in = new ByteBufInputStream(buffer);

                try {
                    byte b0 = in.readByte();
                    return b0 == 0 ? EndTag.INSTANCE : TagTypes.getType(b0).load(in, 0, NbtAccounter.UNLIMITED);
                } catch (IOException var5) {
                    CrashReport crashreport = CrashReport.forThrowable(var5, "Loading NBT data");
                    crashreport.addCategory("NBT Tag");
                    throw new ReportedException(crashreport);
                }
            }
        }, Tag.class);
    }
}
