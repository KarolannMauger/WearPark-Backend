package edu.wearpark.backend.netty;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.util.MotionDataListWrapper;
import edu.wearpark.backend.util.MotionDataWrapper;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.w3c.dom.Attr;

import java.time.Instant;
import java.util.List;

public class Attributes {
    static public final AttributeKey<Device> DEVICE = AttributeKey.valueOf("DEVICE");
    static public final AttributeKey<Instant> TIMESTAMP = AttributeKey.valueOf("TIMESTAMP");
    static public final AttributeKey<Instant> LAST_ENTRY = AttributeKey.valueOf("LAST_ENTRY");
    static public final AttributeKey<MotionDataListWrapper> DATA_LIST = AttributeKey.valueOf("DATA_LIST");
}
