package ru.yandex.javacource.aldukhov.schedule.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter out, Duration duration) throws IOException {
        out.value(duration.toString());
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        return Duration.parse(in.nextString());
    }
}
