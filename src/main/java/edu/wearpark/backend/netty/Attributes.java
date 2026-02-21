package edu.wearpark.backend.netty;

import edu.wearpark.backend.domain.Device;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class Attributes {
    static public final AttributeKey<Device> DEVICE = AttributeKey.valueOf("DEVICE");
}
