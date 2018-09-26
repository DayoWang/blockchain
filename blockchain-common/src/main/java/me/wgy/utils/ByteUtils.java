package me.wgy.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;

/**
 * 字节数组工具类
 *
 * @author wgy
 * @date 2018/9/14
 */
public class ByteUtils {

  /**
   * 将多个字节数组合并成一个字节数组
   */
  public static byte[] merge(byte[]... bytes) {
    Stream<Byte> stream = Stream.of();
    for (byte[] b : bytes) {
      stream = Stream.concat(stream, Arrays.stream(ArrayUtils.toObject(b)));
    }
    return ArrayUtils.toPrimitive(stream.toArray(Byte[]::new));
  }

  /**
   * long 类型转 byte[]
   */
  public static byte[] toBytes(long val) {
    return ByteBuffer.allocate(Long.BYTES).putLong(val).array();
  }
}
