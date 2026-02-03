package org.swyp.linkit.global.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalTime;

public class EndTimeDeserializer extends JsonDeserializer<LocalTime> {

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String timeStr = p.getText();

        // 24:00을 00:00으로 변환
        if ("24:00".equals(timeStr)) {
            return LocalTime.MIDNIGHT;
        }

        return LocalTime.parse(timeStr);
    }
}