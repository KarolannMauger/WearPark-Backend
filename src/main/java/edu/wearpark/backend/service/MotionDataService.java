package edu.wearpark.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wearpark.backend.ErrorCode;
import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.MotionEntryResponse;
import edu.wearpark.backend.exception.AppException;
import edu.wearpark.backend.repository.MotionEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class MotionDataService {
    ObjectMapper mapper = new ObjectMapper();
    @Autowired
    MotionEntryRepository motionEntryRepo;
    public byte[] parseJsonNode(JsonNode node) {
        if(!node.isArray()) throw new AppException(ErrorCode.BAD_REQUEST_BODY);
        List<Float> floats = mapper.convertValue(
                node,
                new TypeReference<List<Float>>() {}
        );
        ByteBuffer buffer = ByteBuffer.allocate(floats.size() * Float.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (Float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }
    public void pushData(User user, MotionEntry motionEntry) {
        motionEntry.setUserId(user.getId());
        motionEntryRepo.save(motionEntry);
    }
    public MotionEntryResponse parseMotionEntry(MotionEntry entry) {
        var encoder = Base64.getEncoder();
        var data = MotionEntryResponse.MotionDataResponse.builder()
                .ax(encoder.encodeToString(entry.getData().getAx()))
                .ay(encoder.encodeToString(entry.getData().getAy()))
                .az(encoder.encodeToString(entry.getData().getAz()))
                .gx(encoder.encodeToString(entry.getData().getGx()))
                .gy(encoder.encodeToString(entry.getData().getGy()))
                .gz(encoder.encodeToString(entry.getData().getGz()))
                .build();
        return MotionEntryResponse.builder()
                .id(entry.getId().toHexString())
                .start(entry.getStart())
                .end(entry.getEnd())
                .data(data)
                .build();
    }
    public List<MotionEntryResponse> getMotionEntries(User user, Instant start, Instant end) {
        var entries = motionEntryRepo.findBetween(user.getId(), start, end);
        return entries.stream().map(this::parseMotionEntry).toList();
    }
    public Optional<MotionEntryResponse> getLatestMotionEntry(User user) {
        return motionEntryRepo.findLatest(user.getId()).map(this::parseMotionEntry);
    }
}
